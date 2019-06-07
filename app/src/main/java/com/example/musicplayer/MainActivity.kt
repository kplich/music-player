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
import android.content.Context
import android.view.Menu
import android.view.MenuItem
import android.view.View

class MainActivity : AppCompatActivity(), ActivityCompat.OnRequestPermissionsResultCallback{
    companion object {
        const val NOTIFICATION_CHANNEL_ID = "NOTIFICATION CHANNEL ID"

        private const val TAG: String = "MainActivity"
        private const val NOTIFICATION_CHANNEL_NAME = "PLAYER_CHANNEL"
    }
    private val myPermissionReadExternalStorage = 23

    private lateinit var songList: List<Song>

    private lateinit var myRecyclerView: RecyclerView

    private lateinit var myRecyclerAdapter: SongRecyclerAdapter
    private lateinit var myRecyclerLayoutManager: LinearLayoutManager

    lateinit var musicController: MusicController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkForDataReadingPermission()
        queryForMusic()
        setRecyclerView()
        musicController = MusicController(this, songList)
        createNotificationChannel()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_options, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            R.id.menuShuffle -> {
                musicController.switchShuffle()
                item.isChecked = !item.isChecked
            }
        }
        return true
    }

    override fun onStart() {
        super.onStart()

        Log.d(TAG, "onStart: before binding service")

        Intent(this, MusicService::class.java).
            also { playIntent ->
            bindService(playIntent, musicController.serviceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onStop() {
        Log.d(TAG, "onStop")
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(musicController.serviceConnection)
        musicController.disconnectService()
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

        val columns = arrayOf(android.provider.MediaStore.Audio.Media._ID,
            android.provider.MediaStore.Audio.Media.TITLE,
            android.provider.MediaStore.Audio.Media.ARTIST)

        val selectionClause = "${android.provider.MediaStore.Audio.AudioColumns.IS_MUSIC} = 1"

        val musicCursor = musicResolver.query(musicUri, columns, selectionClause, null, null)!!

        if (musicCursor.moveToFirst()) {
            //get columns
            val idColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media._ID)
            val titleColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.TITLE)
            val artistColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.ARTIST)
            //add songs to list
            do {
                val thisId = musicCursor.getLong(idColumn)
                val thisTitle = musicCursor.getString(titleColumn)
                val thisArtist = musicCursor.getString(artistColumn)
                foundSongs.add(Song(thisId, thisTitle, thisArtist))
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

    private fun createNotificationChannel() {
        val name = NOTIFICATION_CHANNEL_NAME
        val importance = NotificationManager.IMPORTANCE_LOW
        val mChannel = NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance)

        // Register the channel with the system; you can't change the importance
        // or other notification behaviors after this
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(mChannel)

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

    fun onShuffleMenuItemClicked(view: View) {
        musicController.switchShuffle()
    }
}
