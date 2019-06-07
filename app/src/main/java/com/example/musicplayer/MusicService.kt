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
import com.example.musicplayer.model.Song

class MusicService: Service(), MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {

    companion object {
        private const val TAG = "Music Service"
        const val PLAYER_NOTIFICATION_ID = 53
    }

    lateinit var songList: List<Song>
    private lateinit var player: MediaPlayer
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
        Log.d(TAG, "onPrepared")

        mp!!.start()

        val pendingIntent: PendingIntent =
            Intent(this, MainActivity::class.java).let { notificationIntent ->
                PendingIntent.getActivity(this, 0, notificationIntent, 0)
            }

        val notification: Notification = Notification.Builder(this, MainActivity.NOTIFICATION_CHANNEL_ID)
            .setContentTitle(getText(R.string.player_notification_title))
            .setSmallIcon(R.drawable.icon)
            .setContentText(songList[songPosition].fileName)
            .setContentIntent(pendingIntent)
            .build()


        startForeground(PLAYER_NOTIFICATION_ID, notification)
    }

    override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        mp!!.reset()
        return false
    }

    override fun onCompletion(mp: MediaPlayer?) {
        Log.d(TAG, "onCompletition")

        if(player.currentPosition > 0){
            mp!!.reset()
            playNext()
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    override fun onUnbind(intent: Intent): Boolean {
        player.stop()
        player.release()
        return false
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        super.onDestroy()
        player.release()
        stopForeground(true)
    }

    fun playSong() {
        Log.d(TAG, "playSong: playing ${songList[songPosition].fileName}")

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

        player.prepareAsync()
    }

    fun setSong(songIndex: Int) {
        songPosition = songIndex
    }

    fun getCurrentSong(): Song {
        return songList[songPosition]
    }

    fun getPosn(): Int {
        return player.currentPosition
    }

    fun getDur(): Int {
        return player.duration
    }

    fun isPng(): Boolean {
        return player.isPlaying
    }

    fun pausePlayer() {
        player.pause()
    }

    fun seek(posn: Int) {
        player.seekTo(posn)
    }

    fun go() {
        player.start()
    }

    fun playPrev(){
        songPosition--
        if(songPosition < 0) {
            songPosition = songList.size - 1
        }
        playSong()
    }

    //skip to next
    fun playNext(){
        songPosition++
        if(songPosition >= songList.size) {
            songPosition = 0
        }
        playSong()
    }
}