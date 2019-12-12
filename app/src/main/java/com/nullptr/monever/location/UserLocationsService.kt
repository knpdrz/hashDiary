package com.nullptr.monever.location

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import java.util.logging.Level
import java.util.logging.Logger

const val GEOFENCE_RADIUS = 1000f //in meters

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
            .setCircularRegion(location.latitude, location.longitude,
                GEOFENCE_RADIUS
            )
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
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
        val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
        PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }
}