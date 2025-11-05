package com.example.segunda_etapa

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GestureDetectorCompat
import androidx.drawerlayout.widget.DrawerLayout
import android.media.MediaPlayer
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.ScaleGestureDetectorCompat


class MusicPlayer : AppCompatActivity() {

    private lateinit var Layout: DrawerLayout
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var gestureDet: GestureDetectorCompat
    private val handler = Handler(Looper.getMainLooper())
    private var isPlaying = false
    private var isRepeat = false
    private var currentSongIndex = 0
    var playlist = mutableListOf<Song>()





    private lateinit var seekBar: SeekBar
    private lateinit var PlayPause: ImageButton
    private lateinit var Previous: ImageButton
    private lateinit var Next: ImageButton
    private lateinit var Repeat: ImageButton
    private lateinit var Like: ImageButton
    private lateinit var CurTime: TextView
    private lateinit var MaxTime: TextView
    private lateinit var SongTitle: TextView
    private lateinit var Artist: TextView
    private lateinit var MusicImage: ImageView
    private lateinit var Exit: ImageButton
    private lateinit var Upload: ImageButton
    private lateinit var mediaMetadataRetriever: MediaMetadataRetriever
    private lateinit var Sound: SeekBar



    private fun initViews() {
        seekBar = findViewById(R.id.musicSeekBar)
        PlayPause = findViewById(R.id.PlayMusic)
        Previous = findViewById(R.id.BackMusic)
        Next = findViewById(R.id.NextMusic)
        Repeat = findViewById(R.id.RepeatMusic)
        Like = findViewById(R.id.LikeMusic)
        CurTime = findViewById(R.id.MusicCurTime)
        MaxTime = findViewById(R.id.MusicMaxTime)
        SongTitle = findViewById(R.id.Title)
        Artist = findViewById(R.id.Artist)
        MusicImage = findViewById(R.id.MusicIMG)
        Exit = findViewById(R.id.Exit)
        Upload = findViewById(R.id.upload)
        Sound = findViewById(R.id.soundSeekBar)
    }





    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mp3_activity)
        getSupportActionBar()?.hide()
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        initViews()
        setupListeners()

        mediaMetadataRetriever = MediaMetadataRetriever()
        mediaPlayer = MediaPlayer()


        handler.postDelayed(updateProgress, 1000)

        Layout = findViewById(R.id.drawer_layout)




        gestureDet = GestureDetectorCompat(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float): Boolean {
                    if (e1 == null) return false

                    val diffX = e2.x - e1.x

                    if (diffX > 100 && !Layout.isDrawerOpen(findViewById(R.id.navigation_view))) {
                        Layout.openDrawer(findViewById(R.id.navigation_view))
                        return true
                    }

                    if (diffX < -100 && Layout.isDrawerOpen(findViewById(R.id.navigation_view))) {
                        Layout.closeDrawer(findViewById(R.id.navigation_view))
                        return true
                    }

                    return false
                }
            })



            findViewById<android.view.View>(R.id.main_content).setOnTouchListener(
                object : View.OnTouchListener {
                    override fun onTouch(v: View?, event: MotionEvent): Boolean {
                        return gestureDet.onTouchEvent(event)
                    }
                }
            )
    }


    private fun setupListeners() {
        PlayPause.setOnClickListener {
            if (isPlaying) {
                pauseMusic()
            } else {
                mediaPlayer.start()
                isPlaying = true
                PlayPause.setImageResource(R.drawable.ic_stop)
            }
        }
        Exit.setOnClickListener {
            super.onDestroy()
            if (mediaPlayer.isPlaying) {
                mediaPlayer.stop()
            }
            mediaPlayer.release()
            mediaMetadataRetriever.release()
            finish()



        }
        Previous.setOnClickListener {
            backSong()
        }

        Next.setOnClickListener {
            nextSong()
        }
        Upload.setOnClickListener {
            upload()
        }

        Repeat.setOnClickListener {
            if (playlist.size!= 0){
                isRepeat = !isRepeat
                updateAllIncurrent()}
        }

        Like.setOnClickListener {
            if(playlist.size!= 0)
                likeChange(currentSongIndex)
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    mediaPlayer.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        Sound.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?,progress: Int,fromUser: Boolean){
                if(fromUser){
                    val cur = progress.toFloat()/100
                    mediaPlayer.setVolume(cur,cur)
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }
    private fun upload(){
        val mediaPickerIntent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
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
            playlist.add(new)
            currentSongIndex = playlist.size - 1
            play(playlist[currentSongIndex])
            updateSongInfo(playlist[currentSongIndex])

        }


    }
    private fun getMusicInfo(context: Context,uri: Uri?) : Song{

        mediaMetadataRetriever.setDataSource(context, uri)
        var title =
            mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
                ?: "Unknown Title"
        var author =
            mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
                ?: "Unknown Author"
        var artBit = mediaMetadataRetriever.embeddedPicture

        val art : Bitmap? = if (artBit != null) {
            BitmapFactory.decodeByteArray(artBit, 0, artBit.size)
        } else null;
        return Song(title, author, art, uri, false)


    }
    fun play(song: Song){
        mediaPlayer.reset()
        if(song.uri != null)    mediaPlayer.setDataSource(this,song.uri)
        mediaPlayer.setOnPreparedListener {
            mediaPlayer.start()
            isPlaying = true
            PlayPause.setImageResource(R.drawable.ic_stop)
            updateSongInfo(song)
        }
        mediaPlayer.prepareAsync()

    }
    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
        mediaMetadataRetriever.release()
    }

    private fun playMusic() {
        if(isPlaying == false && playlist.size != 0){
            if (mediaPlayer.currentPosition == 0 ) {
                play(playlist[currentSongIndex])
            } else {
                mediaPlayer.start()
            }
        }
    }


    private fun pauseMusic() {
        if(isPlaying == true)
        {
            mediaPlayer.pause()
            PlayPause.setImageResource(R.drawable.ic_play)
            isPlaying = false
        }
    }

    private fun nextSong() {
        if(playlist.size != 0){
            if(currentSongIndex +1 != playlist.size) {
                currentSongIndex += 1
                play(playlist[currentSongIndex])
            }
            else {
                currentSongIndex = 0
                play(playlist[currentSongIndex])
            }
    }}


    private fun backSong() {
        if(playlist.size != 0){
            if (currentSongIndex > 0) {
                currentSongIndex -= 1
            } else {
                currentSongIndex = playlist.size - 1
            }
            play(playlist[currentSongIndex])
    }}
    private val updateProgress = object : Runnable {
        override fun run() {
            if (isPlaying && mediaPlayer.isPlaying) {
                val cur = TimeToText(mediaPlayer.currentPosition)
                CurTime.text = cur
                seekBar.progress = mediaPlayer.currentPosition
            }
            handler.postDelayed(this,1000)
        }
    }

    private fun TimeToText(milliseconds: Int): String {
        val minutes = milliseconds / 1000 / 60
        val seconds = milliseconds / 1000 % 60
        return String.format("%d:%02d", minutes, seconds)
    }

    private fun updateIcon(icon: Int) {
        Like.setImageResource(icon)
    }
    private fun likeChange(id: Int) {
        playlist[id].licked = !playlist[id].licked
        updateAllIncurrent()

    }
    private fun updateImage(id: Int){
        if(playlist[id].bitmap!= null){
            MusicImage.setImageResource(playlist[id].bitmap as Int)
        }
    }
    private fun updateSongInfo(song: Song) {
        SongTitle.text = song.title
        Artist.text = song.artist
        if (song.bitmap != null) {
            MusicImage.setImageBitmap(song.bitmap)
        } else {
            MusicImage.setImageResource(R.drawable.no_img)
        }

        MaxTime.text = TimeToText(mediaPlayer.duration)
        seekBar.max = mediaPlayer.duration

        updateAllIncurrent()
    }
    private fun updateAllIncurrent(){
        updateImage(currentSongIndex)
        if (playlist[currentSongIndex].licked)
            updateIcon(R.drawable.ic_heart)
        else
            updateIcon(R.drawable.ic_heart_like)
        if(isRepeat){
            Repeat.setImageResource(R.drawable.ic_replay_active)
        }else
            Repeat.setImageResource(R.drawable.ic_repeat)

    }
    override fun onPause() {
        super.onPause()

        if (isPlaying) {
            pauseMusic()
        }
    }


}