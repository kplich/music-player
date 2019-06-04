package com.example.musicplayer

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

    lateinit var songList: List<Song>
    private lateinit var player: MediaPlayer
    private var songPosition: Int = 0
    private val musicBind = MusicBinder()

    override fun onCreate() {
        super.onCreate()
        player = MediaPlayer().apply {
            setWakeMode(applicationContext, PowerManager.PARTIAL_WAKE_LOCK)
            setAudioStreamType(AudioManager.STREAM_MUSIC)
            setOnPreparedListener(this@MusicService)
            setOnErrorListener(this@MusicService)
            setOnCompletionListener(this@MusicService)
        }
    }

    inner class MusicBinder : Binder() {
        internal val service: MusicService
            get() = this@MusicService
    }

    override fun onPrepared(mp: MediaPlayer?) {
        mp!!.start()
    }

    override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        mp!!.reset()
        return false
    }

    override fun onCompletion(mp: MediaPlayer?) {
        if(player.currentPosition > 0){
            mp!!.reset()
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return musicBind
    }

    override fun onUnbind(intent: Intent): Boolean {
        player.stop()
        player.release()
        return false
    }

    fun playSong() {
        player.reset()

        //get song
        val playSong = songList[songPosition]
        //get id
        val currSong = playSong.songId
        //set uri
        val trackUri = ContentUris.withAppendedId(android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, currSong)

        try {
            player.setDataSource(applicationContext, trackUri)
        } catch (e: Exception) {
            Log.e("MUSIC SERVICE", "Error setting data source", e)
        }
        player.prepareAsync()


    }

    fun setSong(songIndex: Int) {
        songPosition = songIndex
    }
}