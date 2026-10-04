package com.example.musicapp.utils

object MusicConstants {
    const val CHANNEL_ID = "music_playback_channel"
    const val CHANNEL_NAME = "Music playback"
    const val NOTIFICATION_ID = 1001

    const val ACTION_PLAY_SONG = "com.example.musicapp.action.PLAY_SONG"
    const val ACTION_PLAY_PAUSE = "com.example.musicapp.action.PLAY_PAUSE"
    const val ACTION_NEXT = "com.example.musicapp.action.NEXT"
    const val ACTION_PREVIOUS = "com.example.musicapp.action.PREVIOUS"
    const val ACTION_SEEK = "com.example.musicapp.action.SEEK"
    const val ACTION_REQUEST_STATE = "com.example.musicapp.action.REQUEST_STATE"
    const val REQUEST_CODE_PREVIOUS = 101
    const val REQUEST_CODE_PLAY_PAUSE = 102
    const val REQUEST_CODE_NEXT = 103
    const val ACTION_SONG_CHANGED = "com.example.musicapp.broadcast.SONG_CHANGED"
    const val ACTION_PLAY_STATE_CHANGED = "com.example.musicapp.broadcast.PLAY_STATE_CHANGED"
    const val ACTION_PROGRESS_CHANGED = "com.example.musicapp.broadcast.PROGRESS_CHANGED"

    const val EXTRA_SONG_ID = "extra_song_id"
    const val EXTRA_IS_PLAYING = "extra_is_playing"
    const val EXTRA_POSITION = "extra_position"
    const val EXTRA_DURATION = "extra_duration"
    const val EXTRA_AUTO_PLAY = "extra_auto_play"
}
