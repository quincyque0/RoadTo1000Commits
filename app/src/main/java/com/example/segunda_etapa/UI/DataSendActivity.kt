package com.example.segunda_etapa.UI

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.segunda_etapa.R
import org.zeromq.SocketType
import org.zeromq.ZContext
import org.zeromq.ZMQ
import java.lang.Exception

class DataSendActivity : AppCompatActivity() {
    private lateinit var SERVER_ADDRESS: String
    private lateinit var PORT: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sata_send)

        supportActionBar?.hide()
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        SERVER_ADDRESS = "172.20.10.2"
        PORT = "3333"
//        SERVER_ADDRESS = "127.0.0.1"
//        PORT = "3333"
        Log.d("DataSendActivity", "Server: $SERVER_ADDRESS:$PORT")
        initUi()
    }

    private fun initUi() {
        val button = findViewById<ImageButton>(R.id.datasent)
        button.setOnClickListener {
            Toast.makeText(this, "Отправка данных", Toast.LENGTH_SHORT).show()
            startServer()
        }
    }

    private fun startServer() {
        Thread {
            val context = ZContext()
            val socket = context.createSocket(SocketType.REQ)

            try {
                val connectorSocket = "tcp://$SERVER_ADDRESS:$PORT"
                Log.d("ZeroMQ", "Подключeниек $connectorSocket")

                socket.connect(connectorSocket)
                Log.d("ZeroMQ", "подключено")
                runOnUiThread {
                Toast.makeText(this, "connected", Toast.LENGTH_LONG).show()}

                val request = "Hello from Android"

                val sendSuccess = socket.send(request.toByteArray(ZMQ.CHARSET), 0)
                if (sendSuccess) {
                    Log.d("ZeroMQ", "Отправлено: $request")
                    runOnUiThread {
                    Toast.makeText(this, "otpravleno", Toast.LENGTH_LONG).show()}

                    val reply = socket.recv(0)
                    Log.d("ZeroMQ", "test")
                    if (reply != null) {
                        val replyString = String(reply, ZMQ.CHARSET)
                        Log.d("ZeroMQ", "Получен ответ: $replyString")
                        runOnUiThread {
                            Toast.makeText(this, "otvethave${reply}", Toast.LENGTH_LONG).show()}
                        runOnUiThread {
                            Toast.makeText(this, "Ответ сервера: $replyString", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        runOnUiThread {
                            Toast.makeText(this, "Нет ответа от сервера", Toast.LENGTH_LONG).show()
                        }
                    }
                } else {
                    Log.e("ZeroMQ", "Ошибка отправки данных")
                    runOnUiThread {
                        Toast.makeText(this, "Ошибка отправки данных", Toast.LENGTH_LONG).show()
                    }
                }

            } catch (e: Exception) {
                Log.e("ZeroMQ", "Ошибка: ${e.message}", e)
                runOnUiThread {
                    Toast.makeText(this, "Ошибка: ${e.message}", Toast.LENGTH_LONG).show()
                }
            } finally {
                socket.close()
                context.close()
                Log.d("ZeroMQ", "Соединение закрыто")
            }
        }.start()
    }
}