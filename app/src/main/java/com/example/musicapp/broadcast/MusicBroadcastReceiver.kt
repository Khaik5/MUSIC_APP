package com.example.musicapp.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.musicapp.utils.MusicConstants

class MusicBroadcastReceiver(
    private val onSongChanged: (String) -> Unit = {},
    private val onPlayStateChanged: (Boolean) -> Unit = {},
    private val onProgressChanged: (positionMs: Int, durationMs: Int) -> Unit = { _, _ -> }
) : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            MusicConstants.ACTION_SONG_CHANGED -> {
                intent.getStringExtra(MusicConstants.EXTRA_SONG_ID)?.let(onSongChanged)
            }

            MusicConstants.ACTION_PLAY_STATE_CHANGED -> {
                onPlayStateChanged(
                    intent.getBooleanExtra(MusicConstants.EXTRA_IS_PLAYING, false)
                )
            }

            MusicConstants.ACTION_PROGRESS_CHANGED -> {
                onProgressChanged(
                    intent.getIntExtra(MusicConstants.EXTRA_POSITION, 0),
                    intent.getIntExtra(MusicConstants.EXTRA_DURATION, 0)
                )
            }
        }
    }
}
