package com.nullptr.monever

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Environment
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.activity_create_log.*
import java.io.File
import java.io.IOException
import java.util.*
import java.util.logging.Level.INFO
import java.util.logging.Logger


const val RECORD_AUDIO_REQUEST = 3

class CreateLogActivity : AppCompatActivity() {
    val logger = Logger.getLogger("CreateLogActivity")

    private var recorder: MediaRecorder? = null
    private var player: MediaPlayer? = null

    private var playRecordButtonWrapper: RecordPlayButton? = null

    private var filePath: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_log)

        setUpRecordingFile()

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
        playRecordButtonWrapper = RecordPlayButton(playRecordButton)
    }

    private fun setUpRecordingFile() {
        val fileName = "tmp.3gp"
        val baseDir = Environment.getExternalStorageDirectory().absolutePath
        val pathDir = "$baseDir/Android/data/com.nullptr.monever"
        filePath = pathDir + File.separator + fileName
    }

    private fun saveLog(log: Log) {
        val dbHelper = LogReaderDbHelper(applicationContext)
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(LogReaderContract.LogEntry.COLUMN_NAME_TEXT, log.text)
            put(LogReaderContract.LogEntry.COLUMN_NAME_HAPPY_RATING, log.happyRating)
            put(LogReaderContract.LogEntry.COLUMN_NAME_CREATION_DATE, log.creationDate?.time)
        }

        renameRecordingFile(log.creationDate?.time.toString())

        val newRowId = db?.insert(LogReaderContract.LogEntry.TABLE_NAME, null, values)
        logger.log(INFO, "saved log to db with id $newRowId")
    }

    private fun renameRecordingFile(newRecordingName: String) {
        val path = filePath.substringBeforeLast("/")
        val from = File(path, "tmp.3gp")
        val to = File(path, "$newRecordingName.3gp")
        if (from.exists())
            from.renameTo(to)
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
        if (!permissionToRecordAccepted) {
            Toast.makeText(
                this,
                "permission to startRecording denied",
                Toast.LENGTH_SHORT
            ).show()
            playRecordButton.isEnabled = false
        }
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

    private fun startRecording() {
        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setOutputFile(filePath)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

            try {
                prepare()
            } catch (e: IOException) {
                logger.log(INFO, "recording prepare() failed")
            }

            start()
        }
    }

    private fun stopRecording() {
        recorder?.apply {
            stop()
            reset()
            release()
        }
        recorder = null
    }

    private fun startPlaying() {
        player = MediaPlayer().apply {
            try {
                setDataSource(filePath)
                setOnCompletionListener {
                    playRecordButtonWrapper!!.buttonState = RecordPlayButtonState.RECORDED
                    playRecordButton.setImageResource(RecordPlayButtonState.RECORDED.image)
                }
                prepare()
                start()
            } catch (e: IOException) {
                logger.log(INFO, "playing prepare() failed")
            }
        }
    }

    private fun stopPlaying() {
        player?.release()
        player = null
    }

    override fun onStop() {
        super.onStop()
        recorder?.reset()
        recorder?.release()
        recorder = null
        player?.release()
        player = null
    }

    enum class RecordPlayButtonState(val image: Int) {
        IDLE(R.drawable.ic_recording_white_24dp),
        RECORDING(R.drawable.ic_stop_white_24dp),
        RECORDED(R.drawable.ic_play_arrow_white_24dp),
        PLAYING(R.drawable.ic_stop_white_24dp)
    }

    internal inner class RecordPlayButton(button: ImageButton) {
        var buttonState = RecordPlayButtonState.IDLE

        init {
            button.setImageResource(R.drawable.ic_mic_white_24dp) // "Start recording"
            button.setOnClickListener {
                when (buttonState.name) {
                    RecordPlayButtonState.IDLE.name -> {
                        startRecording()
                        buttonState = RecordPlayButtonState.RECORDING
                        logger.log(INFO, "recording started")
                    }
                    RecordPlayButtonState.RECORDING.name -> {
                        stopRecording()
                        buttonState = RecordPlayButtonState.RECORDED
                        logger.log(INFO, "recording stopped")
                    }
                    RecordPlayButtonState.RECORDED.name -> {
                        startPlaying()
                        buttonState = RecordPlayButtonState.PLAYING
                        logger.log(INFO, "playing started")
                    }
                    RecordPlayButtonState.PLAYING.name -> {
                        stopPlaying()
                        buttonState = RecordPlayButtonState.RECORDED
                        logger.log(INFO, "playing stopped")
                    }
                }
                button.setImageResource(buttonState.image)
            }
        }
    }
    //IDLE (dot) -> RECORDING (square) -> RECORDED (triangle) -> PLAYING (square)
    //                                        ^                      |
    //                                        |----------------------|
}