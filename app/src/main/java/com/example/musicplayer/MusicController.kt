package com.example.musicplayer

import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.os.IBinder
import android.util.AttributeSet
import android.widget.MediaController
import com.example.musicplayer.model.Song

class MusicController(context: Context):
    MediaController.MediaPlayerControl, MediaController(context) {

    init {
        setPrevNextListeners({myMusicService.playNext()},
            {myMusicService.playPrev()})
        setAnchorView((context as MainActivity).findViewById(R.id.songsLayout))
        setMediaPlayer(this)
        isEnabled = true
    }

    constructor(context: Context, songList: List<Song>): this(context) {
        this.songList = songList
    }

    companion object {
        private const val AUDIO_SESSION_ID = 17
    }

    lateinit var myMusicService: MusicService
    private var isServiceBound: Boolean = false
    private lateinit var songList: List<Song>
    val serviceConnection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            connectService((service as MusicService.MusicBinder).getService(), songList)
        }

        override fun onServiceDisconnected(name: ComponentName) {
            disconnectService()
        }
    }

    fun songPicked(songIndex: Int) {
        myMusicService.setSong(songIndex)
        myMusicService.playSong()
        show()
    }

    override fun hide() {

    }

    override fun isPlaying(): Boolean {
        return if(isServiceBound) myMusicService.isPng() else false
    }

    override fun canSeekForward(): Boolean {
        return true
    }

    override fun getDuration(): Int {
        return if(isServiceBound && myMusicService.isPng()) myMusicService.getDur() else 0
    }

    override fun pause() {
        myMusicService.pausePlayer()
    }

    override fun getBufferPercentage(): Int {
        return if(isServiceBound && myMusicService.isPng()) myMusicService.getDur() else 0
    }

    override fun seekTo(pos: Int) {
        myMusicService.seek(pos)
    }

    override fun getCurrentPosition(): Int {
        return if(isServiceBound && myMusicService.isPng()) myMusicService.getPosn() else 0
    }

    override fun canSeekBackward(): Boolean {
        return true
    }

    override fun start() {
        myMusicService.go()
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
}