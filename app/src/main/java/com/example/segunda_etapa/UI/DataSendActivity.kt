package com.example.segunda_etapa.UI

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.segunda_etapa.R
import org.zeromq.SocketType
import org.zeromq.ZContext
import org.zeromq.ZMQ



class DataSendActivity : AppCompatActivity(){
    lateinit var SERVER_ADDRES : String
    lateinit var PORT : String
    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        enableEdgeToEdge()
        setContentView(R.layout.sata_send)
        getSupportActionBar()?.hide()
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        SERVER_ADDRES = "127.0.0.1"
        PORT = "2222"
        initUi()

    }
    fun initUi(){
        findViewById<ImageButton>(R.id.datasent).setOnClickListener {  startClient()}
    }
    fun startClient(){
        val context = ZMQ.context(1)
        val socket = ZContext().createSocket(SocketType.REP)
        val conector_socket = "tcp//:${SERVER_ADDRES}:${PORT}"
        socket.connect(conector_socket)
        val request = "Hello from Android client!"
        for (i in 0..10){
            socket.send(request.toByteArray(ZMQ.CHARSET),0)
            Log.d("dataSend", "[CLIENT] SendT: $request")
            val reply = socket.recv(0)
            Log.d("dataSend", "[CLIENT] Received: " + String(reply, ZMQ.CHARSET))
        }
        socket.close()
        context.close()
    }



}