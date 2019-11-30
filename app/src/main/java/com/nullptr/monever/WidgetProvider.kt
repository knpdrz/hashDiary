package com.nullptr.monever

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.nullptr.monever.location.MapsActivity
import com.nullptr.monever.log.CreateLogActivity

class WidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { appWidgetId ->
            val views: RemoteViews = RemoteViews(
                context.packageName,
                R.layout.widget
            ).apply {
                setOnClickPendingIntent(
                    R.id.allLogsButton,
                    getPendingIntentForActivity(context, MainActivity::class.java)
                )
                setOnClickPendingIntent(
                    R.id.addLogButton,
                    getPendingIntentForActivity(context, CreateLogActivity::class.java)
                )
                setOnClickPendingIntent(
                    R.id.showMapButton,
                    getPendingIntentForActivity(context, MapsActivity::class.java)
                )
            }

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    private fun getPendingIntentForActivity(
        context: Context,
        activityToLaunch: Class<*>
    ): PendingIntent = Intent(context, activityToLaunch)
        .let { intent ->
            PendingIntent.getActivity(context, 0, intent, 0)
        }
}