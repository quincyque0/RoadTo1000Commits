package com.example.segunda_etapa

import android.content.pm.ActivityInfo
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


class MusicPlayer : AppCompatActivity() {

    private lateinit var Layout: DrawerLayout
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var gestureDet: GestureDetectorCompat
    private val handler = Handler(Looper.getMainLooper())
    private var isPlaying = false
    private var isRepeat = false
    private var isFavorite = false




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
    }

    private var currentSongIndex = 0

    private val playlist = listOf(Song("Song 1", "aurarosh",R.drawable.subaru, R.raw.requiem_of_silence,false),
        Song("Song 2","serega", R.drawable.gojo, R.raw.twerknation_number,false))


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mp3_activity)
        getSupportActionBar()?.hide()
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        initViews()
        setupListeners()


        mediaPlayer = MediaPlayer()
        loadSong(currentSongIndex)
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
                playMusic()
            }
        }

        Previous.setOnClickListener {
            backSong()
        }

        Next.setOnClickListener {
            nextSong()
        }

        Repeat.setOnClickListener {
            isRepeat = !isRepeat
        }

        Like.setOnClickListener {
            isFavorite = true
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
    }
        private fun loadSong(index: Int) {
            try {
                mediaPlayer.reset()
                mediaPlayer.setDataSource(resources.openRawResourceFd(playlist[index].audioId))
                mediaPlayer.prepare()


                SongTitle.text = playlist[index].title
                Artist.text = playlist[index].artist
                seekBar.max = mediaPlayer.duration
                MaxTime.text = TimeToText(mediaPlayer.duration)

                playMusic()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Error loading song", Toast.LENGTH_SHORT).show()
            }
        }


    private fun playMusic() {
        mediaPlayer.start()
        isPlaying = true
        PlayPause.setImageResource(R.drawable.ic_stop)
    }

    private fun pauseMusic() {
        mediaPlayer.pause()
        isPlaying = false
        PlayPause.setImageResource(R.drawable.ic_play)
    }

    private fun nextSong() {
        currentSongIndex = (currentSongIndex + 1) % playlist.size
        loadSong(currentSongIndex)
    }

    private fun backSong() {
        currentSongIndex = if (currentSongIndex - 1 < 0) playlist.size - 1 else currentSongIndex - 1
        loadSong(currentSongIndex)
    }
    private val updateProgress = object : Runnable {
        override fun run() {
            if (mediaPlayer.isPlaying) {
                seekBar.progress = mediaPlayer.currentPosition
                CurTime.text = TimeToText(mediaPlayer.currentPosition)
            }

            if (mediaPlayer.currentPosition >= mediaPlayer.duration - 1000) {
                if (isRepeat) {
                    mediaPlayer.seekTo(0)
                    mediaPlayer.start()
                } else {
                    nextSong()
                }
            }

            handler.postDelayed(this, 1000)
        }
    }

    private fun TimeToText(milliseconds: Int): String {
        val minutes = milliseconds / 1000 / 60
        val seconds = milliseconds / 1000 % 60
        return String.format("%d:%02d", minutes, seconds)
    }
    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
        handler.removeCallbacks(updateProgress)
    }


}