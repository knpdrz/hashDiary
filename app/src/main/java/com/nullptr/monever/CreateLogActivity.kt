package com.nullptr.monever

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.activity_create_log.*
import java.io.IOException
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger

const val RECORD_AUDIO_REQUEST = 3

class CreateLogActivity : AppCompatActivity() {
    val logger = Logger.getLogger("CreateLogActivity")
    private var recorder: MediaRecorder? = null
    private var recordButtonWrapper: RecordButton? = null

    private var playButtonWrapper: PlayButton? = null
    private var player: MediaPlayer? = null

    private var fileName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_log)

        fileName = "${externalCacheDir.absolutePath}/audiorecordtest.3gp"
        logger.log(Level.INFO, "file we're saving to is $fileName")

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
        recordButtonWrapper = RecordButton(recordButton)
        playButtonWrapper = PlayButton(playButton)
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
        if (!permissionToRecordAccepted) Toast.makeText(
            this,
            "permission to startRecording denied",
            Toast.LENGTH_SHORT
        ).show()
        //todo disable startRecording button
    }

    private fun manageRecordingPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestRecordingPermission()
        }
    }

    private fun requestRecordingPermission() {
        ActivityCompat.requestPermissions(
            this, arrayOf(Manifest.permission.RECORD_AUDIO),
            RECORD_AUDIO_REQUEST
        )
    }

    private fun startRecording() {   recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setOutputFile(fileName)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

            try {
                prepare()
            } catch (e: IOException) {
                logger.log(Level.INFO, "recording prepare() failed")
            }

            start()
        }
    }

    private fun stopRecording() {
        recorder?.apply {
            stop()
            release()
        }
        recorder = null
    }

    private fun onRecord(start: Boolean) = if (start) {
        startRecording()
    } else {
        stopRecording()
    }

    private fun onPlay(start: Boolean) = if (start) {
        startPlaying()
    } else {
        stopPlaying()
    }

    private fun startPlaying() {
        player = MediaPlayer().apply {
            try {
                setDataSource(fileName)
                prepare()
                start()
            } catch (e: IOException) {
                logger.log(Level.INFO, "playing prepare() failed")
            }
        }
    }

    private fun stopPlaying() {
        player?.release()
        player = null
    }

    override fun onStop() {
        super.onStop()
        recorder?.release()
        recorder = null
        player?.release()
        player = null
    }

    internal inner class RecordButton(button: Button) {
        var mStartRecording = true

        init {
            button.text = "Start recording"
            button.setOnClickListener {
                onRecord(mStartRecording)
                val text = when (mStartRecording) {
                    true -> "Stop recording"
                    false -> "Start recording"
                }
                button.text = text
                mStartRecording = !mStartRecording
            }
        }
    }

    internal inner class PlayButton(button: Button) {
        var mStartPlaying = true

        init {
            button.text = "Start playing"
            button.setOnClickListener {
                onPlay(mStartPlaying)
                val text = when (mStartPlaying) {
                    true -> "Stop playing"
                    false -> "Start playing"
                }
                button.text = text
                mStartPlaying = !mStartPlaying
            }
        }
    }
}