package com.nullptr.monever

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import java.util.logging.Level
import java.util.logging.Logger


class GeofenceBroadcastReceiver : BroadcastReceiver() {
    val logger = Logger.getLogger("GeofenceBroadcastReceiver")

    override fun onReceive(context: Context?, intent: Intent?) {
        logger.log(Level.INFO, "received broadcast")

        context?.let { ctx ->
            intent?.let { intent ->
                GeofenceTransitionsJobIntentService().enqueueWork(ctx, intent)
            }
        }
    }
}