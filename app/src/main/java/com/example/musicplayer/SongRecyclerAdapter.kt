package com.example.musicplayer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SongRecyclerAdapter(private val songs: List<Song>): RecyclerView.Adapter<SongRecyclerAdapter.SongViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_row_song, parent, false)

        return SongViewHolder(view)
    }

    override fun getItemCount(): Int {
        return songs.size
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        holder.bind(songs[position])
    }

    class SongViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

        init {
            itemView.setOnClickListener {
                (itemView.context as MainActivity).musicController.songPicked(adapterPosition)
            }
        }

        private val songName: TextView = itemView.findViewById(R.id.songName)

        fun bind(song: Song) {
            songName.text = song.title
        }

    }
}