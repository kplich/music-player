package com.example.musicplayer

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import android.os.PowerManager
import android.content.ContentUris
import android.util.Log

class MusicService: Service(), MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {

    companion object {
        private const val TAG = "Music Service"
        const val PLAYER_NOTIFICATION_ID = 53
    }

    lateinit var songList: List<Song>
    private lateinit var player: MediaPlayer
    lateinit var controller: MusicController
    private var songPosition: Int = 0
    private val binder = MusicBinder()

    inner class MusicBinder : Binder() {
        fun getService(): MusicService = this@MusicService

    }

    override fun onCreate() {
        super.onCreate()

        Log.d(TAG, "onCreate")

        player = MediaPlayer().apply {
            setWakeMode(applicationContext, PowerManager.PARTIAL_WAKE_LOCK)
            setAudioStreamType(AudioManager.STREAM_MUSIC)
            setOnPreparedListener(this@MusicService)
            setOnErrorListener(this@MusicService)
            setOnCompletionListener(this@MusicService)
        }
    }

    override fun onPrepared(mp: MediaPlayer?) {
        Log.d(TAG, "onPrepared: start playing")

        mp!!.start() //start playback

        //prepare a notification about playback
        val pendingIntent: PendingIntent =
            Intent(this, MainActivity::class.java).let { notificationIntent ->
                PendingIntent.getActivity(this, 0, notificationIntent, 0)
            }

        val notification: Notification = Notification.Builder(this, MainActivity.NOTIFICATION_CHANNEL_ID)
            .setContentTitle(getText(R.string.player_notification_title))
            .setSmallIcon(R.drawable.icon)
            .setContentText(songList[songPosition].title)
            .setContentIntent(pendingIntent)
            .build()

        startForeground(PLAYER_NOTIFICATION_ID, notification)
        controller.show(0)
    }

    override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        Log.d(TAG, "onError")
        mp!!.reset()
        return false
    }

    override fun onCompletion(mp: MediaPlayer?) {
        Log.d(TAG, "onCompletition")

        if(player.currentPosition > 0){
            mp!!.reset()
            setNext()
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        Log.d(TAG, "onBind")
        return binder
    }

    override fun onUnbind(intent: Intent): Boolean {
        Log.d(TAG, "onUnbind")
        player.stop()
        player.release()
        return false
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
        player.release()
        stopForeground(true)
    }

    fun prepareSong() {
        Log.d(TAG, "prepareSong: preparing ${songList[songPosition].title}")

        player.reset()

        //get id
        val songToPlayId = songList[songPosition].songId
        //set uri
        val trackUri = ContentUris.withAppendedId(android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            songToPlayId)

        try {
            player.setDataSource(applicationContext, trackUri)
        } catch (e: Exception) {
            Log.e(TAG, "Error setting data source", e)
        }

        player.prepare()
    }

    fun setSong(songIndex: Int) {
        Log.d(TAG, "setSong: index $songIndex, name ${songList[songPosition].title}")
        songPosition = songIndex
    }

    fun getPosition(): Int {
        return player.currentPosition
    }

    fun getDuration(): Int {
        return player.duration
    }

    fun isPlaying(): Boolean {
        return player.isPlaying
    }

    fun pausePlayer() {
        Log.d(TAG, "pausePlayer")
        player.pause()
    }

    fun seek(position: Int) {
        player.seekTo(position)
    }

    fun start() {
        player.start()
    }

    fun setPrevious() {
        songPosition--
        if(songPosition < 0) {
            songPosition = songList.size - 1
        }
        prepareSong()
    }

    fun setNext() {
        songPosition++
        if(songPosition >= songList.size) {
            songPosition = 0
        }
        prepareSong()
    }
}