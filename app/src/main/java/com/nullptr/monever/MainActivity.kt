package com.nullptr.monever

import android.Manifest
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager.getDefaultSharedPreferences
import android.provider.BaseColumns
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.fangxu.allangleexpandablebutton.AllAngleExpandableButton
import com.fangxu.allangleexpandablebutton.ButtonData
import com.fangxu.allangleexpandablebutton.ButtonEventListener
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.nullptr.monever.location.MapsActivity
import com.nullptr.monever.location.UserLocationsService
import com.nullptr.monever.log.*
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import java.util.logging.Logger


const val CREATE_NEW_LOG_REQUEST = 1
const val SIGN_IN_REQUEST = 2
const val LOG_FROM_INTENT = "LOG_FROM_INTENT"
const val NOTIFICATION_CHANNEL_ID = "notif_channel"
const val LOG_EXTRA = "LOG_EXTRA"
const val PERMISSION_LOCATION_REQUEST = 66

class MainActivity : AppCompatActivity(), ActivityCompat.OnRequestPermissionsResultCallback {
    private val logger = Logger.getLogger("MainActivity")

    private var logsList = arrayListOf<Log>()
    private lateinit var listAdapter: LogAdapter

    private lateinit var userLocationsService: UserLocationsService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prepareMenuButton()
        prepareGreetingButton()

        signUserIn()

        createNotificationChannel()

        userLocationsService = UserLocationsService(this)

        checkLocationPermissions()

        readLogsFromDb()
        prepareListView()

    }

    private fun handleSpecialGreeting() {
        val sharedPref = getDefaultSharedPreferences(this) ?: return
        val greetingEnabled =
            sharedPref.getBoolean(getString(R.string.special_greeting_enabled), false)
        if (greetingEnabled) {
            Toast.makeText(this, getString(R.string.special_greeting), Toast.LENGTH_SHORT).show()
        }
    }

    private fun signUserIn() {
        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(arrayListOf(AuthUI.IdpConfig.EmailBuilder().build()))
                .build(), SIGN_IN_REQUEST
        )
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

    private fun prepareGreetingButton() {
        addGreetingButton.setOnClickListener {
            AlertDialog.Builder(this).apply {
                setMessage(getString(R.string.special_greeting_question))
                setCancelable(true)
                setPositiveButton(
                    getString(R.string.special_greeting_answer_yes)
                ) { _: DialogInterface, _: Int ->
                    Toast.makeText(
                        context,
                        getString(R.string.special_greeting_set),
                        Toast.LENGTH_SHORT
                    ).show()
                    enableSpecialGreeting()
                }
                setNegativeButton(
                    getString(R.string.special_greeting_answer_no)
                )
                { _: DialogInterface, _: Int ->
                    Toast.makeText(
                        context,
                        getString(R.string.special_greeting_not_set),
                        Toast.LENGTH_SHORT
                    ).show()
                    disableSpecialGreeting()
                }
                create().show()
            }
        }
    }

    private fun enableSpecialGreeting() {
        val sharedPref = getDefaultSharedPreferences(this)
        with(sharedPref.edit()) {
            putBoolean(getString(R.string.special_greeting_enabled), true)
            apply()
        }
    }

    private fun disableSpecialGreeting() {
        val sharedPref = getDefaultSharedPreferences(this)
        with(sharedPref.edit()) {
            putBoolean(getString(R.string.special_greeting_enabled), false)
            apply()
        }
    }

    private fun checkLocationPermissions() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PERMISSION_GRANTED
        ){
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
            ).show()
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSION_LOCATION_REQUEST
            )
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSION_LOCATION_REQUEST
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSION_LOCATION_REQUEST) {
            if (grantResults.size != 1 || grantResults[0] != PERMISSION_GRANTED) {
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
                logsList.add(
                    Log(
                        logText,
                        logHappyRating,
                        Date(logCreationDate)
                    )
                )
            }
        }
    }

    private fun prepareListView() {
        listAdapter = LogAdapter(this, logsList)
        logsListView.adapter = listAdapter
        logsListView.setOnItemClickListener { _, _, position, _ ->
            val log = logsList[position]
            Intent(applicationContext, LogDetailsActivity::class.java).also {
                it.putExtra(LOG_EXTRA, log)
                startActivity(it)
            }
        }
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
                }
            }
        } else if (requestCode == SIGN_IN_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                //successful sign in
                val user = FirebaseAuth.getInstance().currentUser
                Toast.makeText(
                    this,
                    "You are logged in as ${user?.email}",
                    Toast.LENGTH_SHORT
                ).show()
                handleSpecialGreeting()
            } else {
                Toast.makeText(
                    this,
                    "There was a problem with login",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    fun createNotificationChannel() {
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