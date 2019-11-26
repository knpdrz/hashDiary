package com.nullptr.monever

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.activity_create_log.*
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger

const val RECORD_AUDIO_REQUEST = 3

class CreateLogActivity : AppCompatActivity() {
    val logger = Logger.getLogger("CreateLogActivity")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_log)

        saveButton.setOnClickListener {
            Intent().also { resultIntent ->
                val logText = logInput.text.toString()
                val newLog = Log(
                    logText,
                    happinessRatingBar.progress,
                    Calendar.getInstance().time,
                    LogParser().parseLog(logText)
                )
                saveLog(newLog)
                resultIntent.putExtra(LOG_FROM_INTENT, newLog)
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            }
        }

        manageRecordingPermission()

        recordButton.setOnClickListener {
            record()
        }
    }

    private fun record() {
        //https://developer.android.com/guide/topics/media/mediarecorder
//        val recorder = MediaRecorder
    }

    private fun saveLog(log: Log) {
        val dbHelper = LogReaderDbHelper(applicationContext)
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(LogReaderContract.LogEntry.COLUMN_NAME_TEXT, log.text)
            put(LogReaderContract.LogEntry.COLUMN_NAME_HAPPY_RATING, log.happyRating)
            put(LogReaderContract.LogEntry.COLUMN_NAME_CREATION_DATE, log.creationDate?.time)
        }
        val newRowId = db?.insert(LogReaderContract.LogEntry.TABLE_NAME, null, values)
        logger.log(Level.INFO, "saved log to db with id $newRowId")
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val permissionToRecordAccepted = if (requestCode == RECORD_AUDIO_REQUEST) {
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        } else {
            false
        }
        if (!permissionToRecordAccepted) Toast.makeText(this, "permission to record denied", Toast.LENGTH_SHORT).show()
        //todo disable record button
    }

    private fun manageRecordingPermission(){
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestRecordingPermission()
        }
    }

    private fun requestRecordingPermission(){
        ActivityCompat.requestPermissions(
            this, arrayOf(Manifest.permission.RECORD_AUDIO),
            RECORD_AUDIO_REQUEST
        )
    }
}