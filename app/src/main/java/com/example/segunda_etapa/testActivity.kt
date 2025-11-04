package com.example.segunda_etapa

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity



class testActivity: AppCompatActivity() {
    private lateinit var button: Button
    private lateinit var mediaMetadataRetriever: MediaMetadataRetriever
    private lateinit var mediaPlayer: MediaPlayer
    private var currentSongIndex = 0
    var Playlist = mutableListOf<Song>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.test_activity)
        getSupportActionBar()?.hide()
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT



        initViews()

        setupListeners()


        mediaMetadataRetriever = MediaMetadataRetriever()
        mediaPlayer = MediaPlayer()
        if(Playlist.size != 0) {
            play(Playlist[currentSongIndex])
        }

    }

    private fun initViews(){
        button = findViewById<Button>(R.id.test)
    }
    private fun setupListeners(){
        button.setOnClickListener { upload() }
    }
    private fun upload(){
        val mediaPickerIntent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "audio/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }


        filePickerLauncher.launch(mediaPickerIntent)
    }
    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val data: Intent? = result.data
            val uri: Uri? = data?.data
            if (uri != null) {
                contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )

            }
            var new : Song = getMusicInfo(this,uri)
            Playlist.add(new)
            currentSongIndex = Playlist.size - 1
            play(Playlist[currentSongIndex])

        }


    }
    private fun getMusicInfo(context: Context,uri: Uri?) : Song{

            mediaMetadataRetriever.setDataSource(context, uri)
            var title =
                mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
                    ?: "Unknown Title"
            var author =
                mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_AUTHOR)
                    ?: "Unknown Author"
            var artBit = mediaMetadataRetriever.embeddedPicture

            val art = if (artBit != null) {
                BitmapFactory.decodeByteArray(artBit, 0, artBit.size)
            } else null;
            return Song(title, author, art, uri, false)


    }
    fun play(song: Song){
        mediaPlayer.reset()
        if(song.uri != null)    mediaPlayer.setDataSource(this,song.uri)
        mediaPlayer.setOnPreparedListener {
            mediaPlayer.start()
        }
        mediaPlayer.prepareAsync()

    }
    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
        mediaMetadataRetriever.release()
    }


}




