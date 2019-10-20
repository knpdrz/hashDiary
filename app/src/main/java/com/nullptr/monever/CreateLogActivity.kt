package com.nullptr.monever

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_create_log.*

class CreateLogActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_log)

        saveButton.setOnClickListener {
            Intent().also { resultIntent ->
                resultIntent.putExtra(LOG_TEXT_FROM_INTENT, logInput.text.toString())
                resultIntent.putExtra(HAPPY_RATING_FROM_INTENT, happinessRatingBar.progress.toString())
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            }
        }
    }
}