package com.example.segunda_etapa.UI

import android.Manifest
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.telephony.TelephonyManager
import android.widget.ImageButton
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.activity.result.contract.ActivityResultContracts
import com.example.segunda_etapa.R
import com.example.segunda_etapa.Services.BackgroundDataSendService
import com.example.segunda_etapa.Supp.saveJSON
import com.example.segunda_etapa.Supp.readAllJSON
import com.example.segunda_etapa.Supp.clearJSON
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import org.json.JSONObject
import org.zeromq.SocketType
import android.widget.EditText
import org.zeromq.ZContext
import org.zeromq.ZMQ
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DataSendActivity : AppCompatActivity() {
    private lateinit var serverAddressInput: EditText
    private lateinit var serverPortInput: EditText
    private lateinit var btnApplyServer: Button
    private lateinit var btnTestConnection: Button
    private lateinit var sendButton: ImageButton
    private lateinit var statusText: TextView
    private lateinit var switchSendMode: SwitchCompat
    private lateinit var btnClearData: Button
    private lateinit var recordsCountText: TextView

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
            updateRecordsCount()
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

        serverAddress = "10.44.78.180"
        serverPort = "5555"

        initializeUI()

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        telephonyManager = getSystemService(TELEPHONY_SERVICE) as TelephonyManager

        checkPermissionsAndInitialize()
    }

    private fun initializeUI() {
        serverAddressInput = findViewById(R.id.server_address_input)
        serverPortInput = findViewById(R.id.server_port_input)
        btnApplyServer = findViewById(R.id.btn_apply_server)
        btnTestConnection = findViewById(R.id.btn_test_connection)
        switchSendMode = findViewById(R.id.switch_send_mode)
        btnClearData = findViewById(R.id.btn_clear_data)
        recordsCountText = findViewById(R.id.records_count_text)

        serverAddressInput.setText(serverAddress)
        serverPortInput.setText(serverPort)

        val btnUpdateLocation = findViewById<Button>(R.id.btn_update_location)
        val btnGetCellInfo = findViewById<Button>(R.id.btn_get_cellinfo)
        val btnStartService = findViewById<Button>(R.id.btn_start_service)
        val btnStopService = findViewById<Button>(R.id.btn_stop_service)

        sendButton = findViewById(R.id.datasent)
        statusText = findViewById(R.id.status_text)

        switchSendMode.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                updateStatus("Режим: отправка на сервер")
                Toast.makeText(this, "Режим отправки: Сервер", Toast.LENGTH_SHORT).show()
                BackgroundDataSendService.updateSendMode(this, true)
            } else {
                updateStatus("Режим: сохранение на телефоне")
                Toast.makeText(this, "Режим отправки: Сохранение на телефоне", Toast.LENGTH_SHORT).show()
                BackgroundDataSendService.updateSendMode(this, false)
                updateRecordsCount()
            }
        }


        btnClearData.setOnClickListener {
            clearLocalData()
        }

        btnUpdateLocation.setOnClickListener {
            getCurrentLocation()
            Toast.makeText(this, "Обновление GPS", Toast.LENGTH_SHORT).show()
        }

        btnGetCellInfo.setOnClickListener {
            getCellInfo()
            Toast.makeText(this, "Информация о вышках обновлена", Toast.LENGTH_SHORT).show()
        }

        btnStartService.setOnClickListener {
            BackgroundDataSendService.startService(this)
            Toast.makeText(this, "Фоновый сервис запущен", Toast.LENGTH_SHORT).show()
        }

        btnStopService.setOnClickListener {
            BackgroundDataSendService.stopService(this)
            Toast.makeText(this, "Фоновый сервис остановлен", Toast.LENGTH_SHORT).show()
        }

        btnApplyServer.setOnClickListener {
            val newAddress = serverAddressInput.text.toString().trim()
            val newPort = serverPortInput.text.toString().trim()

            if (newAddress.isNotEmpty() && newPort.isNotEmpty()) {
                serverAddress = newAddress
                serverPort = newPort
                Toast.makeText(this, "Адрес сервера обновлен: $serverAddress:$serverPort", Toast.LENGTH_SHORT).show()
                updateStatus("Сервер: $serverAddress:$serverPort")
            } else {
                Toast.makeText(this, "Введите адрес и порт", Toast.LENGTH_SHORT).show()
            }
        }

        btnTestConnection.setOnClickListener {
            testServerConnection()
        }

        sendButton.setOnClickListener {
            if (isLocationReady) {
                if (switchSendMode.isChecked) {
                    sendLocationData()
                } else {
                    saveLocationToPhone()
                }
            } else {
                Toast.makeText(this, "Получение данных GPS", Toast.LENGTH_SHORT).show()
                updateStatus("Ожидание данных GPS")
                getCurrentLocation()
            }
        }
    }

    private fun testServerConnection() {
        updateStatus("Проверка подключения к серверу")

        Thread {
            val context = ZContext()
            val socket = context.createSocket(SocketType.REQ)

            try {
                socket.setReceiveTimeOut(3000)
                socket.setSendTimeOut(3000)
                socket.setLinger(0)

                val connectorSocket = "tcp://$serverAddress:$serverPort"
                socket.connect(connectorSocket)

                val testData = JSONObject().apply {
                    put("test", "connection")
                    put("timestamp", System.currentTimeMillis())
                }

                val request = testData.toString()
                val sendSuccess = socket.send(request.toByteArray(ZMQ.CHARSET), 0)

                if (sendSuccess) {
                    val reply = socket.recv(0)
                    if (reply != null) {
                        runOnUiThread {
                            Toast.makeText(this@DataSendActivity,
                                "Сервер доступен", Toast.LENGTH_SHORT).show()
                            updateStatus("Сервер доступен")
                        }
                    } else {
                        runOnUiThread {
                            Toast.makeText(this@DataSendActivity,
                                "Сервер не отвечает", Toast.LENGTH_SHORT).show()
                            updateStatus("Сервер не отвечает")
                        }
                    }
                } else {
                    runOnUiThread {
                        updateStatus("Ошибка подключения")
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@DataSendActivity,
                        "Ошибка подключения: ${e.message}", Toast.LENGTH_SHORT).show()
                    updateStatus("Ошибка подключения")
                }
            } finally {
                socket.close()
                context.close()
            }
        }.start()
    }

    private fun clearLocalData() {
        val success = clearJSON(this, "location")
        if (success) {
            Toast.makeText(this, "Локальные данные очищены", Toast.LENGTH_SHORT).show()
            updateRecordsCount()
            updateStatus("Локальные данные очищены")
        } else {
            Toast.makeText(this, "Ошибка очистки данных", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateRecordsCount() {
        try {
            val jsonArray = readAllJSON(this, "location")
            if (jsonArray != null) {
                val count = jsonArray.length()
                recordsCountText.text = "Записей в файле: $count"
            } else {
                recordsCountText.text = "Записей в файле: 0"
            }
        } catch (e: Exception) {
            recordsCountText.text = "Записей в файле: ошибка"
        }
    }

    private fun saveLocationToPhone() {
        if (isSending) {
            Toast.makeText(this, "Сохранение уже выполняется", Toast.LENGTH_SHORT).show()
            return
        }

        getCellInfo()
        isSending = true
        updateStatus("Сохранение данных на телефон")

        try {
            val jsonData = JSONObject().apply {
                put("latitude", currentLatitude)
                put("longitude", currentLongitude)
                put("altitude", currentAltitude)
                put("timestamp", currentTimestamp)
                put("imei", deviceImei)
                put("cellInfo", cellInfoString)
                put("savedAt", System.currentTimeMillis())
                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                put("savedAtFormatted", dateFormat.format(Date()))
            }

            val success = saveJSON("location", jsonData, this)

            runOnUiThread {
                if (success) {
                    Toast.makeText(this,
                        "Данные сохранены в location.json", Toast.LENGTH_LONG).show()
                    updateStatus("Данные сохранены в location.json")
                    updateRecordsCount()
                } else {
                    Toast.makeText(this,
                        "Ошибка сохранения данных", Toast.LENGTH_LONG).show()
                    updateStatus("Ошибка сохранения")
                }
                isSending = false
            }

        } catch (e: Exception) {
            runOnUiThread {
                Toast.makeText(this,
                    "Ошибка: ${e.message}", Toast.LENGTH_LONG).show()
                updateStatus("Ошибка: ${e.message}")
                isSending = false
            }
        }
    }

    private fun checkPermissionsAndInitialize() {
        if (checkAllPermissions()) {
            initializeLocation()
            getDeviceImei()
            getCellInfo()
            updateRecordsCount()
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
        updateStatus("Отправка данных на сервер")

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
            if (::statusText.isInitialized) {
                statusText.text = "Статус: $message"
            }
        }
    }
}