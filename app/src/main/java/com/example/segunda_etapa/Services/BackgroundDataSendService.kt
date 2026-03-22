package com.example.segunda_etapa.Services

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.segunda_etapa.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import org.json.JSONObject
import org.zeromq.SocketType
import org.zeromq.ZContext
import org.zeromq.ZMQ
import java.util.Timer
import java.util.TimerTask

class BackgroundDataSendService : Service() {
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var telephonyManager: TelephonyManager
    private lateinit var locationRequest: LocationRequest

    private var currentLatitude = 0.0
    private var currentLongitude = 0.0
    private var currentAltitude = 0.0
    private var currentTimestamp = 0L
    private var deviceImei = "Недоступно"
    private var cellInfoString = "Нет данных"

    private var isSending = false
    private var sendTimer: Timer? = null
    private var locationReceived = false
    private var consecutiveFailures = 0

    private val serverAddress = "10.44.78.180"
    private val serverPort = "5555"
    private val sendIntervalMs = 10000L

    companion object {
        const val NOTIFICATION_ID = 2222
        const val CHANNEL_ID = "background_data_send"
        const val TAG = "BackgroundDataSend"

        fun startService(context: Context) {
            val intent = Intent(context, BackgroundDataSendService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
            Log.d(TAG, "Команда на запуск сервиса отправлена")
        }

        fun stopService(context: Context) {
            val intent = Intent(context, BackgroundDataSendService::class.java)
            context.stopService(intent)
            Log.d(TAG, "Команда на остановку сервиса отправлена")
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        telephonyManager = getSystemService(TELEPHONY_SERVICE) as TelephonyManager

        locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            5000
        ).apply {
            setMinUpdateIntervalMillis(3000)
            setMaxUpdateDelayMillis(10000)
        }.build()

        checkAllPermissions()
        getDeviceImei()
        startLocationUpdates()

        android.os.Handler(mainLooper).postDelayed({
            if (!locationReceived) {
                Log.w(TAG, "Локация не получена")
                forceGetLocation()
            }
            startPeriodicSending()
        }, 5000)
    }

    private fun checkAllPermissions() {
        val hasFineLocation = ContextCompat.checkSelfPermission(this,
            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarseLocation = ContextCompat.checkSelfPermission(this,
            Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasPhoneState = ContextCompat.checkSelfPermission(this,
            Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED

        if (!hasFineLocation || !hasCoarseLocation) {
            Log.e(TAG, "Нет разрешений на локацию! Сервис не сможет получать координаты")
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Сервис фоновой отправки запущен")
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        stopLocationUpdates()
        stopPeriodicSending()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Фоновая отправка данных",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Отправка данных о местоположении и сотах на сервер"
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Отправка данных")
            .setContentText("Отправка геоданных на сервер")
            .setSmallIcon(R.drawable.ic_location)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }

    private fun startLocationUpdates() {
        if (!checkLocationPermission()) {
            Log.e(TAG, "Нет разрешений на получение местоположения")
            return
        }

        try {
            fusedLocationProviderClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
            Log.d(TAG, "Запущено отслеживание местоположения")
            forceGetLocation()

        } catch (e: SecurityException) {
            Log.e(TAG, "Ошибка при запросе обновлений: ${e.message}")
        }
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.lastLocation?.let { location ->
                updateLocationData(location)
                locationReceived = true
                Log.d(TAG, "Локация обновлена: ${location.latitude}, ${location.longitude}")
            }
        }
    }

    private fun forceGetLocation() {
        if (!checkLocationPermission()) return

        try {
            fusedLocationProviderClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                CancellationTokenSource().token
            )
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        updateLocationData(location)
                        locationReceived = true
                        Log.d(TAG, "Текущая локация получена принудительно: ${location.latitude}, ${location.longitude}")
                    } else {
                        Log.w(TAG, "getCurrentLocation вернул null")
                        getLastKnownLocation()
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Ошибка getCurrentLocation: ${e.message}")
                    getLastKnownLocation()
                }
        } catch (e: SecurityException) {
            getLastKnownLocation()
        }
    }

    private fun getLastKnownLocation() {
        if (!checkLocationPermission()) return

        try {
            fusedLocationProviderClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        updateLocationData(location)
                        locationReceived = true
                        Log.d(TAG, "Последняя известная локация: ${location.latitude}, ${location.longitude}")
                    } else {
                        Log.e(TAG, "Нет последней известной локации")
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Ошибка получения последней локации: ${e.message}")
                }
        } catch (e: SecurityException) {
            Log.e(TAG, "${e.message}")
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
        Log.d(TAG, "IMEI устройства: $deviceImei")
    }

    private fun getCellInfo() {
        try {
            if (checkPhoneStatePermission()) {
                val cellInfo = telephonyManager.allCellInfo
                cellInfoString = if (cellInfo != null && cellInfo.isNotEmpty()) {
                    cellInfo.joinToString(";") { info ->
                        "${info.cellIdentity}:${info.cellSignalStrength}"
                    }
                } else {
                    "Нет данных о сотах"
                }
                Log.d(TAG, "Cell info получена, длина: ${cellInfoString.length}")
            } else {
                cellInfoString = "Нет разрешения READ_PHONE_STATE"
                Log.w(TAG, "Нет разрешения READ_PHONE_STATE")
            }
        } catch (e: SecurityException) {
            cellInfoString = "Ошибка получения данных"
            Log.e(TAG, "Ошибка получения cell info: ${e.message}")
        }
    }

    private fun startPeriodicSending() {
        sendTimer = Timer()
        sendTimer?.schedule(object : TimerTask() {
            override fun run() {
                if (!locationReceived) {
                    Log.w(TAG, "Локация еще не получена")
                    forceGetLocation()
                    return
                }
                sendDataToServer()
            }
        }, 10000, sendIntervalMs)

        Log.d(TAG, "Периодическая отправка запущена с интервалом $sendIntervalMs мс")
    }

    private fun stopPeriodicSending() {
        sendTimer?.cancel()
        sendTimer = null
    }

    private fun sendDataToServer() {
        if (isSending) {
            Log.d(TAG, "Предыдущая отправка еще выполняется")
            return
        }

        if (!locationReceived || currentLatitude == 0.0) {
            Log.w(TAG, "Нет данных локации (lat=$currentLatitude, lon=$currentLongitude)")
            forceGetLocation()
        }

        getCellInfo()

        isSending = true
        val currentData = String.format("lat=%.6f, lon=%.6f, alt=%.1f",currentLatitude, currentLongitude, currentAltitude)
        Log.d(TAG, "Отправка данных: $currentData")

        Thread {
            var zContext: ZContext? = null
            var socket: ZMQ.Socket? = null

            try {
                zContext = ZContext()
                socket = zContext.createSocket(SocketType.REQ)

                socket.receiveTimeOut = 5000
                socket.sendTimeOut = 5000
                socket.linger = 0

                val connectorSocket = "tcp://$serverAddress:$serverPort"
                Log.d(TAG, "Подключение к $connectorSocket")

                socket.connect(connectorSocket)
                Log.d(TAG, "Подключено к серверу")

                val jsonData = JSONObject().apply {
                    put("latitude", currentLatitude)
                    put("longitude", currentLongitude)
                    put("altitude", currentAltitude)
                    put("timestamp", currentTimestamp)
                    put("imei", deviceImei)
                    put("cellInfo", cellInfoString)
                    put("source", "background_service")
                }

                val request = jsonData.toString()
                Log.d(TAG, "Отправка JSON: $request")

                val sendSuccess = socket.send(request.toByteArray(ZMQ.CHARSET), 0)

                if (sendSuccess) {
                    Log.d(TAG, "Данные отправлены, ожидание ответа")
                    val reply = socket.recv(0)

                    if (reply != null) {
                        val replyString = String(reply, ZMQ.CHARSET)
                        Log.d(TAG, "Получен ответ от сервера: $replyString")
                        consecutiveFailures = 0
                    } else {
                        Log.e(TAG, "Нет ответа от сервера")
                        consecutiveFailures++
                    }
                } else {
                    Log.e(TAG, "Ошибка отправки данных")
                    consecutiveFailures++
                }

            } catch (e: Exception) {
                Log.e(TAG, "Ошибка при отправке: ${e.message}")
                consecutiveFailures++
            } finally {
                try {
                    socket?.close()
                    zContext?.close()
                } catch (e: Exception) {
                    Log.e(TAG, "Ошибка при закрытии сокета: ${e.message}")
                }
                isSending = false
            }
        }.start()
    }

    private fun stopLocationUpdates() {
        try {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
            Log.d(TAG, "Обновления локации остановлены")
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при остановке обновлений: ${e.message}")
        }
    }

    private fun checkLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun checkPhoneStatePermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this, Manifest.permission.READ_PHONE_STATE
        ) == PackageManager.PERMISSION_GRANTED
    }
}