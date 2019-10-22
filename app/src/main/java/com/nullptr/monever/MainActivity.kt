package com.nullptr.monever

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.BaseColumns
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger


const val CREATE_NEW_LOG_REQUEST = 1
const val LOG_FROM_INTENT = "LOG_FROM_INTENT"

class MainActivity : AppCompatActivity() {
    val logger = Logger.getLogger("MainActivity")

    private var logsList = arrayListOf<Log>()
    private lateinit var listAdapter: LogAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        readLogsFromDb()
        prepareListView()

        addLogButton.setOnClickListener {
            createNewLog()
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

                    logger.log(Level.INFO, "logs list after new add  ${logsList}")
                }
            }
        }
    }
}