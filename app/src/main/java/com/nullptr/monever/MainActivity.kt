package com.nullptr.monever

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.util.logging.Level
import java.util.logging.Logger


const val CREATE_NEW_LOG_REQUEST = 1
const val LOG_TEXT_FROM_INTENT = "LOG_TEXT_FROM_INTENT"

class MainActivity : AppCompatActivity() {
    val logger = Logger.getLogger("foo")

    private var logsList = mutableListOf(
        "log1",
        "logX",
        "log1",
        "logX",
        "log1",
        "logXXXX"
    )
    private lateinit var listAdapter: LogAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prepareListView()

        add_log.setOnClickListener {
            createNewLog()
        }
    }

    private fun prepareListView() {
        listAdapter = LogAdapter(this, logsList)
        logs_list_view.adapter = listAdapter
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
                        resultIntent.getStringExtra(LOG_TEXT_FROM_INTENT)
                    logsList.add(newLog)
                    listAdapter.notifyDataSetChanged()

                    logger.log(Level.INFO, "logs list after new add  ${logsList}")
                }
            }
        }
    }
}