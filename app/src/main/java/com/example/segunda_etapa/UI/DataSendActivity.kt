package com.example.segunda_etapa.UI

import android.Manifest
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.telephony.TelephonyManager
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.activity.result.contract.ActivityResultContracts
import com.example.segunda_etapa.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import org.json.JSONObject
import org.zeromq.SocketType
import org.zeromq.ZContext
import org.zeromq.ZMQ

class DataSendActivity : AppCompatActivity() {
    private lateinit var sendButton: ImageButton
    private lateinit var latitudeText: TextView
    private lateinit var longitudeText: TextView
    private lateinit var altitudeText: TextView
    private lateinit var timestampText: TextView
    private lateinit var imeiText: TextView
    private lateinit var statusText: TextView

    private lateinit var serverAddress: String
    private lateinit var serverPort: String

    private var currentLatitude = 0.0
    private var currentLongitude = 0.0
    private var currentAltitude = 0.0
    private var currentTimestamp = 0L
    private var deviceImei = "Недоступно"
    private var cellInfoString = "Нет данных"

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var telephonyManager: TelephonyManager

    private var isLocationReady = false
    private var isSending = false

    private val locationPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    private val phoneStatePermissions = arrayOf(
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    private val permissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.all { it.value }
        if (allGranted) {
            Toast.makeText(this, "Разрешения получены", Toast.LENGTH_SHORT).show()
            initializeLocation()
            getDeviceImei()
            getCellInfo()
        } else {
            Toast.makeText(this, "Необходимы разрешения", Toast.LENGTH_LONG).show()
            updateStatus("Ошибка: нет разрешений")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sata_send)

        supportActionBar?.hide()
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        serverAddress = "10.103.13.180"
        serverPort = "5555"

        initializeUI()

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        telephonyManager = getSystemService(TELEPHONY_SERVICE) as TelephonyManager

        checkPermissionsAndInitialize()
    }

    private fun initializeUI() {
        sendButton = findViewById(R.id.datasent)
        latitudeText = findViewById(R.id.latitude_value)
        longitudeText = findViewById(R.id.longitude_value)
        altitudeText = findViewById(R.id.altitude_value)
        timestampText = findViewById(R.id.timestamp_value)
        imeiText = findViewById(R.id.imei_value)
        statusText = findViewById(R.id.status_text)

        sendButton.setOnClickListener {
            if (isLocationReady) {
                sendLocationData()
            } else {
                Toast.makeText(this, "Получение данных GPS", Toast.LENGTH_SHORT).show()
                updateStatus("Ожидание данных GPS")
                getCurrentLocation()
            }
        }
    }

    private fun checkPermissionsAndInitialize() {
        if (checkAllPermissions()) {
            initializeLocation()
            getDeviceImei()
            getCellInfo()
        } else {
            permissionRequest.launch(phoneStatePermissions)
        }
    }

    private fun checkAllPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    this, Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    this, Manifest.permission.READ_PHONE_STATE
                ) == PackageManager.PERMISSION_GRANTED
    }

    private fun initializeLocation() {
        updateStatus("Получение местоположения")
        getCurrentLocation()
    }

    private fun getCurrentLocation() {
        if (!checkLocationPermissions()) {
            permissionRequest.launch(locationPermissions)
            return
        }

        try {
            fusedLocationProviderClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                CancellationTokenSource().token
            )
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        updateLocationData(location)
                        isLocationReady = true
                        updateStatus("Местоположение получено")
                    } else {
                        getLastKnownLocation()
                    }
                }
                .addOnFailureListener {
                    getLastKnownLocation()
                }
        } catch (e: SecurityException) {
            getLastKnownLocation()
        }
    }

    private fun getLastKnownLocation() {
        if (!checkLocationPermissions()) {
            return
        }

        try {
            fusedLocationProviderClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        updateLocationData(location)
                        isLocationReady = true
                        updateStatus("Местоположение получено")
                    }
                }
        } catch (e: SecurityException) {
        }
    }

    private fun updateLocationData(location: Location) {
        currentLatitude = location.latitude
        currentLongitude = location.longitude
        currentAltitude = location.altitude
        currentTimestamp = location.time

        runOnUiThread {
            latitudeText.text = String.format("%.6f°", currentLatitude)
            longitudeText.text = String.format("%.6f°", currentLongitude)
            altitudeText.text = String.format("%.1f м", currentAltitude)
            timestampText.text = currentTimestamp.toString()
        }
    }

    private fun getDeviceImei() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                deviceImei = "Android_${Build.SERIAL}"
                if (deviceImei == "Android_unknown" || deviceImei == "Android_null") {
                    deviceImei = "Android_${System.currentTimeMillis()}"
                }
            } else {
                @Suppress("DEPRECATION")
                deviceImei = telephonyManager.deviceId ?: "Unknown"
            }
        } catch (e: SecurityException) {
            deviceImei = "No_IMEI_${System.currentTimeMillis()}"
        }

        runOnUiThread {
            imeiText.text = deviceImei
        }
    }

    private fun getCellInfo() {
        try {
            if (checkPhoneStatePermission()) {
                val cellInfo = telephonyManager.allCellInfo
                if (cellInfo != null && cellInfo.isNotEmpty()) {
                    cellInfoString = cellInfo.joinToString("\n") { it.toString() }
                } else {
                    cellInfoString = "Нет данных о сотах"
                }
            } else {
                cellInfoString = "Нет разрешения READ_PHONE_STATE"
            }
        } catch (e: SecurityException) {
            cellInfoString = "Ошибка получения данных"
        }
    }

    private fun checkPhoneStatePermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this, Manifest.permission.READ_PHONE_STATE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun checkLocationPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    this, Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    private fun sendLocationData() {
        if (isSending) {
            Toast.makeText(this, "Отправка уже выполняется", Toast.LENGTH_SHORT).show()
            return
        }

        getCellInfo()

        isSending = true
        updateStatus("Отправка данных")

        Thread {
            val context = ZContext()
            val socket = context.createSocket(SocketType.REQ)

            try {
                socket.setReceiveTimeOut(5000)
                socket.setSendTimeOut(5000)
                socket.setLinger(0)

                val connectorSocket = "tcp://$serverAddress:$serverPort"
                socket.connect(connectorSocket)

                runOnUiThread {
                    Toast.makeText(this@DataSendActivity, "Подключено к серверу", Toast.LENGTH_SHORT).show()
                }

                val jsonData = JSONObject().apply {
                    put("latitude", currentLatitude)
                    put("longitude", currentLongitude)
                    put("altitude", currentAltitude)
                    put("timestamp", currentTimestamp)
                    put("imei", deviceImei)
                    put("cellInfo", cellInfoString)
                }

                val request = jsonData.toString()
                val sendSuccess = socket.send(request.toByteArray(ZMQ.CHARSET), 0)

                if (sendSuccess) {
                    val reply = socket.recv(0)

                    if (reply != null) {
                        val replyString = String(reply, ZMQ.CHARSET)
                        runOnUiThread {
                            Toast.makeText(this@DataSendActivity,
                                "Успешно! Ответ: $replyString", Toast.LENGTH_LONG).show()
                            updateStatus("Данные отправлены: $replyString")
                        }
                    } else {
                        runOnUiThread {
                            Toast.makeText(this@DataSendActivity,
                                "Нет ответа от сервера", Toast.LENGTH_LONG).show()
                            updateStatus("Ошибка: нет ответа")
                        }
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@DataSendActivity,
                            "Ошибка отправки", Toast.LENGTH_LONG).show()
                        updateStatus("Ошибка отправки")
                    }
                }

            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@DataSendActivity,
                        "Ошибка: ${e.message}", Toast.LENGTH_LONG).show()
                    updateStatus("Ошибка: ${e.message}")
                }
            } finally {
                socket.close()
                context.close()
                isSending = false
            }
        }.start()
    }

    private fun updateStatus(message: String) {
        runOnUiThread {
            statusText.text = "Статус: $message"
        }
    }
}