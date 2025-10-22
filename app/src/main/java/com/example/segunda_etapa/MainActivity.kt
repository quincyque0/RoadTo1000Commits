package com.example.segunda_etapa

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.widget.ImageButton

class MainActivity : AppCompatActivity() {

    private lateinit var calcIntent: Intent
    private lateinit var MPIntent: Intent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.calculator_main)
        getSupportActionBar()?.hide()
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        calcIntent = Intent(this, CalcActivity::class.java)
        MPIntent = Intent(this, MusicPlayer::class.java)

        setupButtons()
    }

    private fun setupButtons() {
        findViewById<ImageButton>(R.id.ButtonGoToMP).setOnClickListener { startActivity(MPIntent) }
        findViewById<ImageButton>(R.id.ButtonGotoCalc).setOnClickListener { startActivity(calcIntent) }

    }
}