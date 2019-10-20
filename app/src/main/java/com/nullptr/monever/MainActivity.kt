package com.nullptr.monever

import android.os.Bundle
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var logsListView: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prepareListView()
    }

    private fun prepareListView() {
        logsListView = findViewById<ListView>(R.id.logs)
        val logsList = mutableListOf("log1", "logX")
        val adapter = LogAdapter(this, logsList)
        logsListView.adapter = adapter
    }
}