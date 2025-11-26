package com.example.segunda_etapa

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import org.json.JSONObject
import java.io.File
import java.io.FileWriter
import java.io.IOException

class LocationService : Service() {
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var request: LocationRequest

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.lastLocation?.let { location ->
                Log.d("LocationService", "лоакция изменена на: ${location.latitude}, ${location.longitude}")
                val data = JSONObject().apply {
                    put("Latitude", location.latitude)
                    put("Longitude", location.longitude)
                    put("Altitude", location.altitude)
                    put("Time", location.time)
                    put("Accuracy", location.accuracy)
                    put("Provider", location.provider)
                }
                saveJSON("location", data, applicationContext)
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val NOTIFICATION_ID = 1111
        const val CHANNEL_ID = "location_service"

        fun startService(context: Context) {
            val intent = Intent(context, LocationService::class.java)
            context.startService(intent)
            Log.d("LocationService", "Сервис запущен")
        }

        fun saveJSON(filename: String, data: JSONObject, context: Context): Boolean {
            return try {
                val documentsDir = File(context.getExternalFilesDir(null), "Documents")
                if (!documentsDir.exists()) {
                    documentsDir.mkdirs()
                    Log.d("LocationService", "Создана папка: ${documentsDir.absolutePath}")
                }

                val file = File(documentsDir, "$filename.txt")
                FileWriter(file, true).use { writer ->
                    writer.write(data.toString() + "\n")
                }

                Log.d("LocationService", "Локация сохранена в: ${file.absolutePath}")
                true
            } catch (e: IOException) {
                Log.e("LocationService", "Ошибка сохранения: ${e.message}")
                false
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        request = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            5000
        ).apply {
            setMinUpdateIntervalMillis(3000)
            setMaxUpdateDelayMillis(10000)
        }.build()

        Log.d("LocationService", "Сервис инициализирован")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(NOTIFICATION_ID, createNotification())
        }

        if (checkLocationPermission()) {
            startLocationUpdates()
            Log.d("LocationService", "Обновления локации запущены")
        } else {
            Log.w("LocationService", "Нет разрешения на локацию")
            stopSelf()
        }

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        stopLocationUpdates()
    }

    private fun checkLocationPermission(): Boolean {
        val hasFineLocation = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val needsBackgroundPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
        val hasBackgroundLocation = !needsBackgroundPermission ||
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ) == PackageManager.PERMISSION_GRANTED

        return hasFineLocation && hasBackgroundLocation
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e("LocationService", "Нет разрешения ACCESS_FINE_LOCATION")
            stopSelf()
            return
        }

        try {
            fusedLocationProviderClient.requestLocationUpdates(
                request,
                locationCallback,
                Looper.getMainLooper()
            )
            Log.d("LocationService", "Запущен сбор данных")
        } catch (e: Exception) {
            Log.e("LocationService", "${e.message}")
            stopSelf()
        }
    }

    private fun stopLocationUpdates() {
        try {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
            Log.d("LocationService", "Остановка сервиса")
        } catch (e: Exception) {
            Log.e("LocationService", "${e.message}")
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "канал",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "для оповещений"
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Отслеживание")
            .setContentText("Сбор данных о местоположении")
            .setSmallIcon(R.drawable.ic_location)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }
}