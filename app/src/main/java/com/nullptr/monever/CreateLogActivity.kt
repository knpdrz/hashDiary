package com.nullptr.monever

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_create_log.*
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger

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
}