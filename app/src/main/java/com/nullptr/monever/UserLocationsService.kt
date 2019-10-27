package com.nullptr.monever

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import java.util.logging.Level
import java.util.logging.Logger

const val GEOFENCE_RADIUS = 10f //in meters
const val GEOFENCE_EXPIRATION_DURATION = 7200000000L //in millis

class UserLocationsService(private val context: Context) {
    private val logger = Logger.getLogger("UserLocationsService")

    private fun createGeofences(locations: List<LatLng>): List<Geofence> {
        val geofences = mutableListOf<Geofence>()
        for (location in locations) {
            geofences.add(createGeofenceForLocation(location))
        }
        return geofences
    }

    private fun createGeofenceForLocation(location: LatLng): Geofence {
        return Geofence.Builder()
            .setRequestId(location.toString())
            .setCircularRegion(location.latitude, location.longitude, GEOFENCE_RADIUS)
            .setExpirationDuration(GEOFENCE_EXPIRATION_DURATION)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
            .build()
    }

    private fun getGeofencingRequest(geofences: List<Geofence>): GeofencingRequest {
        return GeofencingRequest.Builder().apply {
            setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            addGeofences(geofences)
        }.build()
    }

    fun prepareGeofences(locations: List<LatLng>) {
        val geofencingClient = LocationServices.getGeofencingClient(context)
        val geofenceReq = getGeofencingRequest(createGeofences(locations))
        geofencingClient.addGeofences(geofenceReq, geofencePendingIntent).run {
            addOnSuccessListener {
                logger.log(Level.INFO, "geofences added!")
                Toast.makeText(context, "Added ${locations.size} geofences!", Toast.LENGTH_SHORT).show()
            }
            addOnFailureListener {
                logger.log(Level.INFO, "FAILED to add geofences! " + it.localizedMessage)
            }
        }
    }

    private val geofencePendingIntent: PendingIntent by lazy {
        //todo by lazy means?
        val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
        PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    fun createNotificationChannel() {
        //todo: info: Because you must create the notification channel before posting any notifications on Android 8.0 and higher,
        // you should execute this code as soon as your app starts. It's safe to call this repeatedly because creating an existing notification channel performs no operation.
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.channel_name)
            val descriptionText = context.getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}