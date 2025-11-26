package com.example.segunda_etapa

import android.Manifest
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import android.net.Uri
import android.util.Log
import com.google.android.gms.tasks.CancellationTokenSource
import java.io.File

class LocationActivity : AppCompatActivity() {
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var longitudeText: TextView
    private lateinit var latitudeText: TextView
    private lateinit var altitudeText: TextView
    private lateinit var timeText: TextView
    private lateinit var getCurrentLocationBtn: Button
    private lateinit var getBackgroundLocationBtn: Button

    private var folderUri: Uri? = null

    private val locationPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    private val backgroundLocationPermission = arrayOf(
        Manifest.permission.ACCESS_BACKGROUND_LOCATION
    )

    private val permissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.all { true }
        if (allGranted) {
            getLastLocation()
            Toast.makeText(this, "Разрешения предоставлены", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Некоторые разрешения отклонены", Toast.LENGTH_LONG).show()
        }
    }

    private val backgroundPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.all { true }
        if (allGranted) {
            startBackgroundLocationService()
            Toast.makeText(this, "Фоновое разрешение предоставлено", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Фоновое разрешение отклонено", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.location)
        supportActionBar?.hide()
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        initUI()
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        getCurrentLocationBtn.setOnClickListener {
            getCurrentLocation()
        }

        getBackgroundLocationBtn.setOnClickListener {
            startBackgroundLocationService()
        }
    }

    private fun initUI() {
        longitudeText = findViewById(R.id.Longitude)
        latitudeText = findViewById(R.id.Latitude)
        altitudeText = findViewById(R.id.Altitude)
        timeText = findViewById(R.id.CurTime)
        getCurrentLocationBtn = findViewById(R.id.curLoc)
        getBackgroundLocationBtn = findViewById(R.id.backLoc)
    }

    private fun getLastLocation() {
        if (!locationPermission()) {
            permissionRequest.launch(locationPermissions)
            return
        }

        fusedLocationProviderClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                updateLocationUI(location)
            } else {
                Toast.makeText(this, "Последняя локация недоступна", Toast.LENGTH_SHORT).show()
            }
        }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun getCurrentLocation() {
        if (!locationPermission()) {
            permissionRequest.launch(locationPermissions)
            return
        }

            fusedLocationProviderClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                CancellationTokenSource().token
            )
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        updateLocationUI(location)
                        Toast.makeText(this, "Текущая локация получена", Toast.LENGTH_SHORT).show()
                    } else {
                        getLastLocation()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Ошибка получения локации: ${e.message}", Toast.LENGTH_SHORT).show()
                }
    }

    private fun startBackgroundLocationService() {
        if (!locationPermission()) {
            permissionRequest.launch(locationPermissions)
            return
        }


        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q &&
            !backgroundLocationPermission()) {
            backgroundPermissionRequest.launch(backgroundLocationPermission)
            return
        }

        LocationService.startService(this)
        Toast.makeText(this, "Фоновый сервис запущен", Toast.LENGTH_SHORT).show()

        showSavePath()
    }

    private fun showSavePath() {
        val documentsDir = File(getExternalFilesDir(null), "Documents")
        val file = File(documentsDir, "location.txt")
        Toast.makeText(this, "Файлы сохраняются в: ${file.absolutePath}", Toast.LENGTH_LONG).show()
        Log.d("LocationActivity", "Путь сохранения: ${file.absolutePath}")
    }

    private fun locationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    private fun backgroundLocationPermission(): Boolean {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    private fun updateLocationUI(location: Location) {
        latitudeText.text = "Latitude: ${location.latitude}"
        longitudeText.text = "Longitude: ${location.longitude}"
        altitudeText.text = "Altitude: ${location.altitude}"
        timeText.text = "Time: ${location.time}"
    }

    private fun stopBackgroundLocationService() {
        val intent = Intent(this, LocationService::class.java)
        stopService(intent)
        Toast.makeText(this, "Сервис остановлен", Toast.LENGTH_SHORT).show()
    }
}