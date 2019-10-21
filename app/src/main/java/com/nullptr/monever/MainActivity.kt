package com.nullptr.monever

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.util.logging.Level
import java.util.logging.Logger


const val CREATE_NEW_LOG_REQUEST = 1
const val LOG_FROM_INTENT = "LOG_FROM_INTENT"
const val HAPPY_RATING_FROM_INTENT = "HAPPY_RATING_FROM_INTENT"

class MainActivity : AppCompatActivity() {
    val logger = Logger.getLogger("foo")

    private var logsList = mutableListOf(
        Log("log1",0),
        Log("logXX",10),
        Log("logXX",10),
        Log("logXX",10),
        Log("logXX",10),
        Log("logXX",10),
        Log("logXX",10),
        Log("logXX",10),
        Log("log333",7)
    )
    private lateinit var listAdapter: LogAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prepareListView()

        addLogButton.setOnClickListener {
            createNewLog()
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