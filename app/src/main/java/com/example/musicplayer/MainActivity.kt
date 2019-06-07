package com.example.musicplayer

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.musicplayer.model.Song
import android.content.ComponentName
import android.content.Context
import com.example.musicplayer.MusicService.MusicBinder
import android.os.IBinder
import android.content.ServiceConnection
import android.widget.MediaController


class MainActivity : AppCompatActivity(), ActivityCompat.OnRequestPermissionsResultCallback, MediaController.MediaPlayerControl {
    companion object {
        const val NOTIFICATION_CHANNEL_ID = "NOTIFICATION CHANNEL ID"

        private const val TAG: String = "MainActivity"
        private const val NOTIFICATION_CHANNEL_NAME = "PLAYER_CHANNEL"
        private const val AUDIO_SESSION_ID = 17
    }
    private val myPermissionReadExternalStorage = 23

    private lateinit var songList: List<Song>

    private lateinit var myRecyclerView: RecyclerView
    private lateinit var playbackController: MediaController

    private lateinit var myRecyclerAdapter: SongRecyclerAdapter
    private lateinit var myRecyclerLayoutManager: LinearLayoutManager
    private lateinit var myMusicService: MusicService

    private var isServiceBound: Boolean = false
    //connection to the service
    private val musicConnection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            //get service
            myMusicService = (service as MusicBinder).getService()
            //pass list
            myMusicService.songList =  songList
            isServiceBound = true
        }

        override fun onServiceDisconnected(name: ComponentName) {
            isServiceBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkForDataReadingPermission()
        queryForMusic()
        setRecyclerView()
        setPlaybackController()
        createNotificationChannel()
    }

    override fun onStart() {
        super.onStart()

        Log.d(TAG, "onStart: before binding service")

        Intent(this, MusicService::class.java).
            also { playIntent ->
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onStop() {
        Log.d(TAG, "onStop")
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(musicConnection)
        isServiceBound = false
    }

    private fun checkForDataReadingPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {

            Log.d(TAG,"onCreate: read permission not granted")

            // Permission is not granted
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                myPermissionReadExternalStorage
            )
        }
        else {
            Log.d(TAG,"onCreate: read permission granted")
        }
    }

    private fun queryForMusic() {
        val foundSongs: MutableList<Song> = mutableListOf()
        val musicResolver = contentResolver
        val musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val musicCursor = musicResolver.query(musicUri, null, null, null, null)!!

        if (musicCursor.moveToFirst()) {
            //get columns
            val nameColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.DISPLAY_NAME)
            val idColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media._ID)
            //add songs to list
            do {
                val thisName = musicCursor.getString(nameColumn)
                val thisId = musicCursor.getLong(idColumn)
                foundSongs.add(Song(thisId, thisName))
            } while (musicCursor.moveToNext())
        }

        songList = foundSongs.toList()
    }

    private fun setRecyclerView() {
        myRecyclerLayoutManager = LinearLayoutManager(this)
        myRecyclerAdapter = SongRecyclerAdapter(songList)
        myRecyclerView = findViewById<RecyclerView>(R.id.songList).apply {
            setHasFixedSize(true)
            layoutManager = myRecyclerLayoutManager
            adapter = myRecyclerAdapter
            addItemDecoration(DividerItemDecoration(this.context, DividerItemDecoration.VERTICAL))
        }
    }

    private fun setPlaybackController() {
        playbackController = object : MediaController(this) {
            override fun hide() {

            }
        }.apply {
            setPrevNextListeners({ playNext() }, { playPrev() })
            setMediaPlayer(this@MainActivity)
            setAnchorView(this@MainActivity.findViewById(R.id.songsLayout))
            isEnabled = true
        }
    }

    private fun createNotificationChannel() {
        val name = NOTIFICATION_CHANNEL_NAME
        val importance = NotificationManager.IMPORTANCE_LOW
        val mChannel = NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance)

        // Register the channel with the system; you can't change the importance
        // or other notification behaviors after this
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(mChannel)

    }

    //play next
    private fun playNext() {
        myMusicService.playNext()
        playbackController.show(0)
    }

    //play previous
    private fun playPrev() {
        myMusicService.playPrev()
        playbackController.show(0)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        Log.d(TAG, "onRequestPermissionResult: asking for permission with requestCode $requestCode")
        when (requestCode) {
            myPermissionReadExternalStorage -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // permission was granted, yay!
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    finish()
                }
                return
            }
            else -> {
                // Ignore all other requests.
            }
        }
    }

    fun songPicked(songIndex: Int) {
        myMusicService.setSong(songIndex)
        myMusicService.playSong()
        playbackController.show()
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
}
