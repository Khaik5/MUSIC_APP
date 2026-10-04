package com.example.musicapp.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.example.musicapp.MainActivity
import com.example.musicapp.R
import com.example.musicapp.data.model.Music
import com.example.musicapp.data.repository.MusicRepository
import com.example.musicapp.utils.MusicConstants

class MusicService : Service() {
    private val handler = Handler(Looper.getMainLooper())
    private val queue = MusicRepository.getQueue()
    private var player: MediaPlayer? = null
    private var currentIndex = -1
    private var currentSong: Music? = null
    private var prepared = false
    private var playing = false

    private val progressTask = object : Runnable {
        override fun run() {
            broadcastProgress()
            if (playing) handler.postDelayed(this, 500)
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        player = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            setOnCompletionListener { playNext() }
            setOnErrorListener { _, _, _ ->
                playing = false
                prepared = false
                broadcastPlayState()
                true
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            MusicConstants.ACTION_PLAY_SONG -> {
                intent.getStringExtra(MusicConstants.EXTRA_SONG_ID)?.let { playSong(it) }
            }
            MusicConstants.ACTION_PLAY_PAUSE -> togglePlayPause()
            MusicConstants.ACTION_NEXT -> playNext()
            MusicConstants.ACTION_PREVIOUS -> playPrevious()
            MusicConstants.ACTION_SEEK -> seekTo(
                intent.getIntExtra(MusicConstants.EXTRA_POSITION, 0)
            )
            MusicConstants.ACTION_REQUEST_STATE -> {
                broadcastAllState()
                if (currentSong == null) stopSelf(startId)
            }
        }
        return START_STICKY
    }

    private fun playSong(songId: String) {
        if(currentSong?.id==songId){
            return
        }
        val index = queue.indexOfFirst { it.id == songId }
        if (index == -1) return
        currentIndex = index
        currentSong = queue[index]
        prepared = false
        playing = false
        handler.removeCallbacks(progressTask)

        val mediaPlayer = player ?: return
        mediaPlayer.reset()
        val audioRes = currentSong?.audioRes ?: return
        val descriptor = resources.openRawResourceFd(audioRes) ?: return
        mediaPlayer.setDataSource(descriptor.fileDescriptor, descriptor.startOffset, descriptor.length)
        descriptor.close()
        mediaPlayer.setOnPreparedListener {
            prepared = true
            it.start()
            playing = true
            broadcastSongChanged()
            broadcastPlayState()
            ensureForeground()
            handler.post(progressTask)
        }
        mediaPlayer.prepareAsync()
        ensureForeground()
    }

    private fun togglePlayPause() {
        val mediaPlayer = player ?: return
        if (currentSong == null) {
            queue.firstOrNull()?.let { playSong(it.id) }
            return
        }
        if (!prepared) return
        if (mediaPlayer.isPlaying) {
            mediaPlayer.pause()
            playing = false
            handler.removeCallbacks(progressTask)
        } else {
            mediaPlayer.start()
            playing = true
            handler.post(progressTask)
        }
        broadcastPlayState()
        ensureForeground()
    }

    private fun playNext() {
        if (queue.isEmpty()) return
        val next = if (currentIndex in queue.indices) (currentIndex + 1) % queue.size else 0
        playSong(queue[next].id)
    }

    private fun playPrevious() {
        if (queue.isEmpty()) return
        val previous = if (currentIndex > 0) currentIndex - 1 else queue.lastIndex
        playSong(queue[previous].id)
    }

    private fun seekTo(positionMs: Int) {
        if (!prepared) return
        val mediaPlayer = player ?: return
        val uiDuration = currentSong?.durationMs ?: mediaPlayer.duration
        val targetUi = positionMs.coerceIn(0, uiDuration.coerceAtLeast(1))
        val targetPlayer = if (uiDuration > 0 && mediaPlayer.duration > 0) {
            (targetUi.toLong() * mediaPlayer.duration / uiDuration).toInt()
        } else targetUi
        mediaPlayer.seekTo(targetPlayer)
        broadcastProgress()
        ensureForeground()
    }

    private fun broadcastAllState() {
        currentSong?.let { broadcastSongChanged() }
        broadcastPlayState()
        broadcastProgress()
    }

    private fun broadcastSongChanged() {
        val id = currentSong?.id ?: return
        sendBroadcast(Intent(MusicConstants.ACTION_SONG_CHANGED).apply {
            setPackage(packageName)
            putExtra(MusicConstants.EXTRA_SONG_ID, id)
        })
    }

    private fun broadcastPlayState() {
        sendBroadcast(Intent(MusicConstants.ACTION_PLAY_STATE_CHANGED).apply {
            setPackage(packageName)
            putExtra(MusicConstants.EXTRA_IS_PLAYING, playing)
        })
    }

    private fun broadcastProgress() {
        val mediaPlayer = player
        val uiDuration = currentSong?.durationMs ?: 0
        val currentPosition = if (prepared && mediaPlayer != null && mediaPlayer.duration > 0 && uiDuration > 0) {
            (mediaPlayer.currentPosition.toLong() * uiDuration / mediaPlayer.duration).toInt()
        } else 0
        sendBroadcast(Intent(MusicConstants.ACTION_PROGRESS_CHANGED).apply {
            setPackage(packageName)
            putExtra(MusicConstants.EXTRA_POSITION, currentPosition)
            putExtra(MusicConstants.EXTRA_DURATION, uiDuration)
        })
    }

    private fun ensureForeground() {
        val openApp = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val previous = serviceActionPendingIntent(
            MusicConstants.ACTION_PREVIOUS,
            MusicConstants.REQUEST_CODE_PREVIOUS
        )
        val playPause = serviceActionPendingIntent(
            MusicConstants.ACTION_PLAY_PAUSE,
            MusicConstants.REQUEST_CODE_PLAY_PAUSE
        )
        val next = serviceActionPendingIntent(
            MusicConstants.ACTION_NEXT,
            MusicConstants.REQUEST_CODE_NEXT
        )
        val song = currentSong
        val notification = NotificationCompat.Builder(this, MusicConstants.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(song?.title ?: getString(R.string.app_name))
            .setContentText(song?.artist ?: getString(R.string.music_ready))
            .setContentIntent(openApp)
            .addAction(R.drawable.ic_previous_player, getString(R.string.previous), previous)
            .addAction(
                if (playing) R.drawable.ic_pause_player else R.drawable.ic_play_player,
                getString(if (playing) R.string.pause else R.string.play),
                playPause
            )
            .addAction(R.drawable.ic_next_player, getString(R.string.next), next)
            .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
        startForeground(MusicConstants.NOTIFICATION_ID, notification)
    }

    private fun serviceActionPendingIntent(action: String, requestCode: Int): PendingIntent =
        PendingIntent.getService(
            this,
            requestCode,
            Intent(this, MusicService::class.java).setAction(action),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

    private fun createNotificationChannel() {
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(
                MusicConstants.CHANNEL_ID,
                MusicConstants.CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            )
        )
    }

    override fun onDestroy() {
        handler.removeCallbacks(progressTask)
        player?.release()
        player = null
        super.onDestroy()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        if (currentSong != null) ensureForeground()
        super.onTaskRemoved(rootIntent)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
