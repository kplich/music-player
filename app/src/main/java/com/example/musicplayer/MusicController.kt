package com.example.musicplayer

import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import android.widget.MediaController

class MusicController(context: Context):
    MediaController.MediaPlayerControl, MediaController(context) {

    init {
        Log.d(TAG, "Creating Music Controller")
        setPrevNextListeners({myMusicService.setNext()},
            {myMusicService.setPrevious()})
        setAnchorView((context as MainActivity).findViewById(R.id.songsLayout))
        setMediaPlayer(this)
        isEnabled = true
    }

    constructor(context: Context, songList: List<Song>): this(context) {
        this.songList = songList
    }

    companion object {
        private const val AUDIO_SESSION_ID = 17
        private const val TAG = "Music Controller"
    }

    lateinit var myMusicService: MusicService
    private var isServiceBound: Boolean = false
    private lateinit var songList: List<Song>

    val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            Log.d(TAG, "ServiceConnection::onServiceConnected")
            connectService((service as MusicService.MusicBinder).getService(), songList)
            myMusicService.controller = this@MusicController
        }

        override fun onServiceDisconnected(name: ComponentName) {
            disconnectService()
        }
    }

    fun songPicked(songIndex: Int) {
        Log.d(TAG, "songPicked")
        myMusicService.setSong(songIndex)
        myMusicService.prepareSong()

        Log.d(TAG, "songPicked: show()")
        show()
    }

    override fun hide() {

    }

    override fun isPlaying(): Boolean {
        Log.d(TAG, "isPlaying? ${if(isServiceBound) myMusicService.isPlaying() else false}")
        return if(isServiceBound) myMusicService.isPlaying() else false
    }

    override fun canSeekForward(): Boolean {
        return true
    }

    override fun getDuration(): Int {
        Log.d(TAG, "getDuration")
        return if(isServiceBound && myMusicService.isPlaying()) myMusicService.getDuration() else 0
    }

    override fun pause() {
        Log.d(TAG, "pause")
        myMusicService.pausePlayer()
    }

    override fun getBufferPercentage(): Int {
        return if(isServiceBound && myMusicService.isPlaying()) myMusicService.getDuration() else 0
    }

    override fun seekTo(pos: Int) {
        myMusicService.seek(pos)
    }

    override fun getCurrentPosition(): Int {
        return if(isServiceBound && myMusicService.isPlaying()) myMusicService.getPosition() else 0
    }

    override fun canSeekBackward(): Boolean {
        return true
    }

    override fun start() {
        Log.d(TAG, "start")
        myMusicService.start()
    }

    override fun getAudioSessionId(): Int {
        return AUDIO_SESSION_ID
    }

    override fun canPause(): Boolean {
        return true
    }

    fun connectService(musicService: MusicService, songList: List<Song>) {
        this.myMusicService = musicService
        myMusicService.songList = songList
        isServiceBound = true
    }

    fun disconnectService() {
        isServiceBound = false
    }

    fun switchShuffle() {
        myMusicService.switchShuffle()
    }
}