package com.example.segunda_etapa.UI

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.PersistableBundle
import android.widget.Button
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.segunda_etapa.R
import org.zeromq.SocketType
import org.zeromq.ZContext
import org.zeromq.ZMQ

class DataSendActivity : AppCompatActivity(){

    lateinit var sentData: ImageButton
    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        enableEdgeToEdge()
        setContentView(R.layout.sata_send)
        getSupportActionBar()?.hide()
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

    }
    fun initUi(){
        findViewById<ImageButton>(R.id.datasent).setOnClickListener {  }
    }
    fun startServer(){
        val context = ZMQ.context(1)
        val socket = ZContext().createSocket(SocketType.REP)
        socket.bind("tcp://*:2222") // Replace with your server's IP and port
        var counter: Int = 0
    }


}