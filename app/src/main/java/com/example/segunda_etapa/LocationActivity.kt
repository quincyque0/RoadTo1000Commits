package com.example.segunda_etapa

import android.Manifest
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices


class LocationActivity : AppCompatActivity() {
    private lateinit var fusedLocationProviderClient :FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.location)
        getSupportActionBar()?.hide()
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        Manifest.permission.ACCESS_COARSE_LOCATION
        Manifest.permission.ACCESS_FINE_LOCATION
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
    }
    fun CurrentLocationScreen() {
        val permissions = listOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
        )
        PermissionBox(
            permissions = permissions,
            requiredPermissions = listOf(permissions.first()),
            onGranted = {
                CurrentLocationContent(
                    usePreciseLocation = it.contains(Manifest.permission.ACCESS_FINE_LOCATION),
                )
            },
        )
    }
    fun getLocation(){
        fusedLocationProviderClient.requestLocationUpdates()
    }

}