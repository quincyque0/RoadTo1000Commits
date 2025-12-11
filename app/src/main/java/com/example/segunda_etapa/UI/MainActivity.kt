package com.example.segunda_etapa.UI

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.segunda_etapa.UI.LocationActivity
import com.example.segunda_etapa.R
import com.example.segunda_etapa.UI.MusicPlayer

class MainActivity : AppCompatActivity() {

    private lateinit var calcIntent: Intent
    private lateinit var MPIntent: Intent
    private lateinit var Location: Intent
    private lateinit var Telephony: Intent
    private lateinit var Server: Intent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.calculator_main)
        getSupportActionBar()?.hide()
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        calcIntent = Intent(this, CalcActivity::class.java)
        MPIntent = Intent(this, MusicPlayer::class.java)
        Location = Intent(this, LocationActivity::class.java)
        Telephony = Intent(this, TelephonyActivity::class.java)
        Server = Intent(this, DataSendActivity::class.java)


        setupButtons()
    }

    private fun setupButtons() {
        findViewById<ImageButton>(R.id.ButtonGoToMP).setOnClickListener { startActivity(MPIntent) }
        findViewById<ImageButton>(R.id.ButtonGotoCalc).setOnClickListener { startActivity(calcIntent) }
        findViewById<ImageButton>(R.id.ButtonGoToLocation).setOnClickListener{ startActivity(Location) }
        findViewById<ImageButton>(R.id.ButtonGoToTelephony).setOnClickListener{ startActivity(Telephony) }
        findViewById<ImageButton>(R.id.ButtonGoToServercontact).setOnClickListener{ startActivity(Server) }
    }
}