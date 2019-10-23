package com.nullptr.monever

import android.Manifest
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Build
import android.os.Bundle
import android.provider.BaseColumns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_ENTER
import com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_EXIT
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import java.util.logging.Level.INFO
import java.util.logging.Logger


const val CREATE_NEW_LOG_REQUEST = 1
const val LOG_FROM_INTENT = "LOG_FROM_INTENT"
const val NOTIFICATION_CHANNEL_ID = "notif_channel"

class MainActivity : AppCompatActivity(), ActivityCompat.OnRequestPermissionsResultCallback {
    private val logger = Logger.getLogger("MainActivity")
    private val PERMISSION_REQUEST_LOCATION = 66

    private lateinit var geofencingClient: GeofencingClient

    private var logsList = arrayListOf<Log>()
    private lateinit var listAdapter: LogAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        createNotificationChannel()
        checkLocationPermissions()

        readLogsFromDb()
        prepareListView()

        addLogButton.setOnClickListener {
            createNewLog()
        }
    }

    private fun checkLocationPermissions() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PERMISSION_GRANTED
        ) {
            prepareGeofences()
        } else {
            requestLocationPermission()
        }
    }

    private fun requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        ) {
            Toast.makeText(
                this, "we need you to give us permission to access location",
                Toast.LENGTH_LONG
            ).show() //todo long explanation for the first time
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSION_REQUEST_LOCATION
            )
        } else {
            Toast.makeText(
                this, "gimme yo location",
                Toast.LENGTH_LONG
            ).show()
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSION_REQUEST_LOCATION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSION_REQUEST_LOCATION) {
            if (grantResults.size == 1 && grantResults[0] == PERMISSION_GRANTED) {
                logger.log(INFO, "yay, location permission granted, preparing geofences")
                prepareGeofences()
            } else {
                Toast.makeText(
                    this, "you didn't give permission",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun readLogsFromDb() {
        val dbHelper = LogReaderDbHelper(applicationContext)
        val db = dbHelper.readableDatabase
        val projection = arrayOf(
            BaseColumns._ID,
            LogReaderContract.LogEntry.COLUMN_NAME_TEXT,
            LogReaderContract.LogEntry.COLUMN_NAME_HAPPY_RATING,
            LogReaderContract.LogEntry.COLUMN_NAME_CREATION_DATE
        )
        val cursor =
            db.query(
                LogReaderContract.LogEntry.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                null
            )
        with(cursor) {
            while (moveToNext()) {
                // val logId = getLong(getColumnIndexOrThrow(BaseColumns._ID))
                val logText =
                    getString(getColumnIndexOrThrow(LogReaderContract.LogEntry.COLUMN_NAME_TEXT))
                val logHappyRating =
                    getInt(getColumnIndexOrThrow(LogReaderContract.LogEntry.COLUMN_NAME_HAPPY_RATING))
                val logCreationDate =
                    getLong(getColumnIndexOrThrow(LogReaderContract.LogEntry.COLUMN_NAME_CREATION_DATE))
                logsList.add(Log(logText, logHappyRating, Date(logCreationDate)))
            }
        }
    }

    private fun prepareListView() {
        listAdapter = LogAdapter(this, logsList)
        logsListView.adapter = listAdapter
    }

    private fun createNewLog() {
        Intent(this, CreateLogActivity::class.java).also { createNewLogIntent ->
            startActivityForResult(createNewLogIntent, CREATE_NEW_LOG_REQUEST)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultIntent: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultIntent)
        if (requestCode == CREATE_NEW_LOG_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                resultIntent?.also {
                    val newLog =
                        resultIntent.getSerializableExtra(LOG_FROM_INTENT) as Log
                    logsList.add(newLog)
                    listAdapter.notifyDataSetChanged()

                    logger.log(INFO, "logs list after new add $logsList")
                }
            }
        }
    }

    private fun createDummyGeofence(): Geofence {
        return Geofence.Builder()
            .setRequestId("dummy2")
            .setCircularRegion(52.2004527, 20.95511, 9000f)
            .setExpirationDuration(7200000)
            .setTransitionTypes(GEOFENCE_TRANSITION_ENTER or GEOFENCE_TRANSITION_EXIT)
            .build()
    }

    private fun getGeofencingRequest(geofence: Geofence): GeofencingRequest {
        return GeofencingRequest.Builder().apply {
            setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            addGeofence(geofence)
        }.build()
    }

    private fun prepareGeofences() {
        geofencingClient = LocationServices.getGeofencingClient(this)
        val geofenceReq = getGeofencingRequest(createDummyGeofence())
        geofencingClient.addGeofences(geofenceReq, geofencePendingIntent).run {
            addOnSuccessListener {
                logger.log(INFO, "geofence added!")
            }
            addOnFailureListener {
                logger.log(INFO, "FAILED to add geofence! " + it.localizedMessage)
            }
        }
    }

    private val geofencePendingIntent: PendingIntent by lazy {
        //todo by lazy means?
        val intent = Intent(this, GeofenceBroadcastReceiver::class.java)
        PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private fun createNotificationChannel() {
        //todo: info: Because you must create the notification channel before posting any notifications on Android 8.0 and higher,
        // you should execute this code as soon as your app starts. It's safe to call this repeatedly because creating an existing notification channel performs no operation.
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}