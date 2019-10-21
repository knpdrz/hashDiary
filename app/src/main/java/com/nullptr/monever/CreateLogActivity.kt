package com.nullptr.monever

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_create_log.*
import java.util.*

class CreateLogActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_log)

        saveButton.setOnClickListener {
            Intent().also { resultIntent ->
                val logText = logInput.text.toString()
                resultIntent.putExtra(
                    LOG_FROM_INTENT,
                    Log(
                        logText,
                        happinessRatingBar.progress,
                        Calendar.getInstance().time,
                        LogParser().parseLog(logText)
                    )
                )
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            }
        }
    }
}