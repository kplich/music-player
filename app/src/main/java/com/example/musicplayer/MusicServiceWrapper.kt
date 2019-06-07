package com.example.musicplayer

import android.content.Context
import android.util.AttributeSet
import android.widget.MediaController
import com.example.musicplayer.model.Song

class MusicServiceWrapper(context: Context, attributes: AttributeSet?):
    MediaController.MediaPlayerControl, MediaController(context, attributes) {

    init {
        setPrevNextListeners({myMusicService.playNext()},
            {myMusicService.playPrev()})
        setMediaPlayer(this)
        isEnabled = true
    }

    companion object {
        private const val AUDIO_SESSION_ID = 17
        private const val PLAYER_VIEW = R.id.songsLayout
    }

    lateinit var myMusicService: MusicService
    private var isServiceBound: Boolean = false

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
        setAnchorView((context as MainActivity).findViewById(PLAYER_VIEW))
        myMusicService.songList = songList
        isServiceBound = true
    }

    fun disconnectService() {
        isServiceBound = false
    }
}