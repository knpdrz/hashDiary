package com.nullptr.monever

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Bundle
import android.provider.BaseColumns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.fangxu.allangleexpandablebutton.AllAngleExpandableButton
import com.fangxu.allangleexpandablebutton.ButtonData
import com.fangxu.allangleexpandablebutton.ButtonEventListener
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

    private var logsList = arrayListOf<Log>()
    private lateinit var listAdapter: LogAdapter

    private lateinit var userLocationsService: UserLocationsService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prepareMenuButton()

        userLocationsService = UserLocationsService(this)
        userLocationsService.createNotificationChannel()

        checkLocationPermissions()

        readLogsFromDb()
        prepareListView()
    }

    private fun prepareMenuButton() {
        val button = menuButton as AllAngleExpandableButton
        val buttonsData = arrayListOf<ButtonData>()
        val colors =
            intArrayOf(R.color.colorAccent, R.color.colorAccentLight, R.color.colorAccentLight)
        val drawable =
            intArrayOf(
                R.drawable.ic_menu_24dp,
                R.drawable.ic_add_24dp, R.drawable.ic_map_24dp
            )

        for (i in drawable.indices) {
            val buttonData = ButtonData.buildIconButton(applicationContext, drawable[i], 15f)
            buttonData.setBackgroundColorId(this, colors[i])
            buttonsData.add(buttonData)
        }
        button.buttonDatas = buttonsData

        val buttonClickHandler = object : ButtonEventListener {
            override fun onButtonClicked(index: Int) {
                when (index) {
                    1 -> createNewLog()
                    2 -> openMap()
                }
            }

            override fun onExpand() {}
            override fun onCollapse() {}
        }
        button.setButtonEventListener(buttonClickHandler)
    }

    private fun checkLocationPermissions() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PERMISSION_GRANTED
        ) {
            userLocationsService.prepareGeofences(LocationReader(this).readUserLocationsFromDb())
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
                userLocationsService.prepareGeofences(LocationReader(this).readUserLocationsFromDb())
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

    private fun openMap() {
        Intent(this, MapsActivity::class.java).also { openMapIntent ->
            startActivity(openMapIntent)
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
}