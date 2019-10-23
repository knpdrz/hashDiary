package com.nullptr.monever

import android.content.Context
import android.content.Intent
import android.text.TextUtils
import androidx.core.app.JobIntentService
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import java.util.logging.Level.INFO
import java.util.logging.Logger


class GeofenceTransitionsJobIntentService : JobIntentService() {
    val logger = Logger.getLogger("GeofenceTransitionsJobIntentService")
    private val JOB_ID = 123

    fun enqueueWork(context: Context, intent: Intent) {
        logger.log(INFO, "enqueuing work")
        enqueueWork(context, GeofenceTransitionsJobIntentService::class.java, JOB_ID, intent)
    }

    override fun onHandleWork(intent: Intent) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent.hasError()) {
            logger.log(INFO, "geofencing event has error " + geofencingEvent.errorCode)
            return
        }
        val geofenceTransition = geofencingEvent.geofenceTransition

        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER || geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            val triggeringGeofences = geofencingEvent.triggeringGeofences
            val geofenceTransitionDetails =
                getGeofenceTransitionDetails(geofenceTransition, triggeringGeofences)
            logger.log(INFO, "geofence transition details $geofenceTransitionDetails")
        } else {
            logger.log(INFO, "invalid geotransition type")
        }
    }

    private fun getGeofenceTransitionDetails(
        geofenceTransition: Int,
        triggeringGeofences: List<Geofence>
    ): String {
        val geofenceTransitionString = getTransitionString(geofenceTransition)

        val triggeringGeofencesIdsList = arrayListOf<String>()
        for (geofence in triggeringGeofences) {
            triggeringGeofencesIdsList.add(geofence.requestId)
        }
        val triggeringGeofencesIdsString = TextUtils.join(", ", triggeringGeofencesIdsList)

        return "$geofenceTransitionString: $triggeringGeofencesIdsString"
    }

    private fun getTransitionString(transitionType: Int): String {
        return when (transitionType) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> "geofence_transition_entered"
            Geofence.GEOFENCE_TRANSITION_EXIT -> "geofence_transition_exited"
            else -> "unknown_geofence_transition"
        }
    }
}