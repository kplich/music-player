package com.example.musicplayer

import android.Manifest
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
import android.view.View


class MainActivity : AppCompatActivity(), ActivityCompat.OnRequestPermissionsResultCallback {

    private val TAG: String = "MainActivity"

    private val myPermissionReadExternalStorage = 1

    private lateinit var songList: List<Song>

    private lateinit var myRecyclerView: RecyclerView
    private lateinit var myRecyclerAdapter: SongRecyclerAdapter
    private lateinit var myRecyclerLayoutManager: LinearLayoutManager

    private lateinit var myMusicService: MusicService
    private lateinit var playIntent: Intent
    private var musicBound: Boolean = false

    //connect to the service
    private val musicConnection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            //get service
            myMusicService = (service as MusicBinder).service
            //pass list
            myMusicService.songList =  songList
            musicBound = true
        }

        override fun onServiceDisconnected(name: ComponentName) {
            musicBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkForDataReadingPermission()

        songList = queryForMusic()

        myRecyclerLayoutManager = LinearLayoutManager(this)
        myRecyclerAdapter = SongRecyclerAdapter(songList)
        myRecyclerView = findViewById<RecyclerView>(R.id.songList).apply {
            setHasFixedSize(true)
            layoutManager = myRecyclerLayoutManager
            adapter = myRecyclerAdapter
            addItemDecoration(DividerItemDecoration(this.context, DividerItemDecoration.VERTICAL))
        }
    }

    override fun onStart() {
        super.onStart()

        Log.d(TAG, "onStart: before binding service")

        playIntent = Intent(this, MusicService::class.java)
        bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE)

        startService(playIntent)
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

    private fun queryForMusic(): List<Song> {
        val songList: MutableList<Song> = mutableListOf()
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
                songList.add(Song(thisId, thisName))
            } while (musicCursor.moveToNext())
        }

        return songList.toList()
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
    }
}
