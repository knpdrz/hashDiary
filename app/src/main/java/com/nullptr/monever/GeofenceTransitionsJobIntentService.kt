package com.nullptr.monever

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.JobIntentService
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.PRIORITY_DEFAULT
import androidx.core.app.NotificationManagerCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_ENTER
import com.google.android.gms.location.GeofencingEvent
import java.util.logging.Level.INFO
import java.util.logging.Logger


class GeofenceTransitionsJobIntentService : JobIntentService() {
    private val logger = Logger.getLogger("GeofenceTransitionsJobIntentService")
    private val GEOFENCE_JOB_ID = 123

    fun enqueueWork(context: Context, intent: Intent) {
        enqueueWork(context, GeofenceTransitionsJobIntentService::class.java, GEOFENCE_JOB_ID, intent)
    }

    override fun onHandleWork(intent: Intent) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent.hasError()) {
            logger.log(INFO, "geofencing event has error " + geofencingEvent.errorCode)
            return
        }
        val geofenceTransition = geofencingEvent.geofenceTransition

        if (geofenceTransition == GEOFENCE_TRANSITION_ENTER || geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            val triggeringGeofences = geofencingEvent.triggeringGeofences
            logger.log(INFO, "geofence ${triggeringGeofences[0].requestId} entered or exited")
            showNotification(triggeringGeofences[0].requestId, geofenceTransition == GEOFENCE_TRANSITION_ENTER)
        }
    }

    private fun showNotification(placeName: String, userEntered: Boolean) {
        val notificationText = if(userEntered) getString(R.string.user_reached_place, placeName) else getString(R.string.user_left_place, placeName)

        val intent = Intent(this, CreateLogActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, 0)
        val builder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_heart_24dp)
            .setContentTitle(getString(R.string.create_log))
            .setContentText(notificationText)
            .setPriority(PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true) //removes notification upon user tap

        with(NotificationManagerCompat.from(this)) {
            notify(1, builder.build()) //todo notificationId is a unique int for each notification that you must define
        }
    }
}