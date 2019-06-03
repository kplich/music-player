package com.example.musicplayer

import android.Manifest
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


class MainActivity : AppCompatActivity(), ActivityCompat.OnRequestPermissionsResultCallback {

    private val TAG: String = "MainActivity"

    private val myPermissionReadExternalStorage = 1

    private lateinit var songList: List<Song>

    private lateinit var myRecyclerView: RecyclerView
    private lateinit var myRecyclerAdapter: SongRecyclerAdapter
    private lateinit var myRecyclerLayoutManager: LinearLayoutManager

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
            //add songs to list
            do {
                val thisName = musicCursor.getString(nameColumn)
                songList.add(Song(thisName))
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
}
