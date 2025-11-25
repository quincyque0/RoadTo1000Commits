package com.example.segunda_etapa

import android.Manifest
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.net.Uri
import android.os.IBinder
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.documentfile.provider.DocumentFile
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import org.json.JSONObject
import org.w3c.dom.Document
import java.io.IOException
import java.net.URI

class LocationService : LocationListener, Service() {
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var request: LocationRequest
    private var folderUri: Uri? = null
    private val locationCallback = object : LocationCallback()
    {
        override fun onLocationResult(locationResult: LocationResult)
        {
            locationResult.lastLocation?.let { location ->
                val data = JSONObject().apply {
                    put("Latitude", location.latitude)
                    put("Longitude", location.longitude)
                    put("Altitude", location.altitude)
                    put("Time", location.time)
                }
            saveJSON("location", data,folderUri,applicationContext)
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object{
        public fun saveJSON(filename: String, Data: JSONObject,folderUri: Uri?,context: Context) : Boolean {
            val dir = folderUri?.let { DocumentFile.fromTreeUri(context, it) } ?: return false
            if (!dir.canWrite())
                return false
            val jsonName = if (filename.endsWith(".txt")) filename else "$filename.txt"
            val file = dir.findFile(jsonName) ?: dir.createFile(
                "text/plain",
                jsonName.removeSuffix(".txt")
            ) ?: return false
            try {
                val contentResolver = context.contentResolver
                contentResolver.openOutputStream(file.uri, "wa")?.use { outputStream ->
                    outputStream.write(Data.toString().toByteArray())
                    outputStream.write("\n".toByteArray())
                }
                return true
            } catch (e: IOException) {
                return false
            }
        }
    }


    override fun onCreate() {
        super.onCreate()
        Log.d("123","Service onCreate")


    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val data = intent?.getStringExtra("DATA")

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
            request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000).build()
            Update();

        }

        val priority = Priority.PRIORITY_HIGH_ACCURACY
        fusedLocationProviderClient.getCurrentLocation(
            priority,
            CancellationTokenSource().token
        )

        return START_NOT_STICKY
    }
    override fun onLocationChanged(location: Location) {

    }
    private fun Update()
    {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            stopSelf()
            return
        }

        fusedLocationProviderClient.requestLocationUpdates(
            request,
            locationCallback,
            null
        )
    }


}