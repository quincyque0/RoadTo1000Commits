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
import android.location.LocationListener
import com.google.android.gms.tasks.CancellationTokenSource
import java.io.File
import java.io.FileWriter

class LocationActivity :AppCompatActivity() {
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var longitudeText: TextView
    private lateinit var latitudeText: TextView
    private lateinit var altitudeText: TextView
    private lateinit var timeText: TextView
    private lateinit var getCurrentLocationBtn: Button
    private lateinit var getBackgroundLocationBtn: Button
    var intentServ = Intent(this, LocationService::class.java)

    private val locationPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    private val permissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                getLastLocation()
            }
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                getLastLocation()
            }
            else -> {
                Toast.makeText(this, "отклонено", Toast.LENGTH_LONG).show()
            }
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
            startService(intentServ)
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
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionRequest.launch(locationPermissions)
            return
        }

        fusedLocationProviderClient.lastLocation.addOnSuccessListener {
            location: Location? -> if (location != null) {updateLocationUI(location)}
        }
            .addOnFailureListener {
                e -> Toast.makeText(this, " ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun getCurrentLocation(){
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
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
                } else {
                    getLastLocation()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, " ${e.message}", Toast.LENGTH_SHORT).show()
            }

    }




    private fun updateLocationUI(location: Location) {
        latitudeText.text = "Latitude: ${location.latitude}"
        longitudeText.text = "Longitude: ${location.longitude}"
        altitudeText.text = "Altitude: ${location.altitude}"
        timeText.text = "Time: ${location.time}"
    }
}