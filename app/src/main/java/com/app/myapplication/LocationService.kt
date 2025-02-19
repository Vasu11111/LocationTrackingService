package com.app.myapplication

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class LocationService:Service() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val handler = Handler(Looper.getMainLooper())
    private val interval: Long = 5000  // 5 seconds

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient  = LocationServices.getFusedLocationProviderClient(this)
        startForeground(1, createNotification())
        startLocationUpdates()
    }

    private fun startLocationUpdates() {
        handler.post(object : Runnable {
            override fun run() {
                if (ActivityCompat.checkSelfPermission(this@LocationService, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this@LocationService, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    Log.e("LocationService", "Location permission not granted")
                    return
                }

                fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                    location?.let {
                        sendLocationToServer(it.latitude, it.longitude)
                    }
                }
                handler.postDelayed(this, interval)
            }
        })
    }

    private fun sendLocationToServer(latitude: Double, longitude: Double) {
        Log.d("LocationService", "Lat: $latitude, Lng: $longitude")
        // Implement API call here (e.g., Retrofit)
    }

    private fun createNotification(): Notification {
        val channelId = "LocationServiceChannel"
        val channelName = "Location Service"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_LOW // Avoids sound/vibration
            )

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Location Tracking Active")
            .setContentText("Fetching location every 5 seconds...")
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Ensure this icon exists
            .setContentIntent(pendingIntent)
            .setOngoing(true) // Prevents user from swiping it away
            .build()
    }



    override fun onBind(intent: Intent?): IBinder? = null

}