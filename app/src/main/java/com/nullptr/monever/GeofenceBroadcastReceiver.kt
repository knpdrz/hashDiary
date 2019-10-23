package com.nullptr.monever

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent


class GeofenceBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        context?.let { ctx ->
            intent?.let { intent ->
                GeofenceTransitionsJobIntentService().enqueueWork(ctx, intent)
            }
        }
    }
}