package com.nullptr.monever.log

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Environment
import android.view.View.GONE
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.nullptr.monever.LOG_EXTRA
import com.nullptr.monever.R
import kotlinx.android.synthetic.main.activity_log_details.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.logging.Level.INFO
import java.util.logging.Logger

class LogDetailsActivity : AppCompatActivity() {
    val logger = Logger.getLogger("LogDetailsActivity")

    private lateinit var log: Log
    private var filePath: String = ""

    private var player: MediaPlayer? = null
    private var playButtonWrapper: PlayButtonWrapper? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_details)

        log = intent.getSerializableExtra(LOG_EXTRA) as Log
        setLogData()
        prepareRecording()
    }

    private fun setLogData(){
        logTextView.text = log.text

        val formatter = SimpleDateFormat("EEE, dd MMM yyyy HH:mm")
        val logDateString = formatter.format(log.creationDate)

        logDate.text = logDateString
    }

    private fun prepareRecording() {
        val fileName = log.creationDate!!.time
        val baseDir = Environment.getExternalStorageDirectory().absolutePath
        val pathDir = "$baseDir/Android/data/com.nullptr.monever"
        filePath = pathDir + File.separator + fileName + ".3gp"
        logger.log(INFO, "recording file name = $filePath")

        if(File(filePath).exists()){
            playButtonWrapper = PlayButtonWrapper(playButton)
        }else{
            playButton.visibility = GONE
        }
    }

    private fun startPlaying() {
        player = MediaPlayer().apply {
            try {
                setDataSource(filePath)
                setOnCompletionListener {
                    playButtonWrapper!!.isPlaying = false
                    playButton.setImageResource(R.drawable.ic_play_arrow_white_24dp)
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
        stopPlaying()
    }

    internal inner class PlayButtonWrapper(button: ImageButton) {
        var isPlaying = false

        init {
            button.setImageResource(R.drawable.ic_play_arrow_white_24dp)
            button.setOnClickListener {
                when (isPlaying) {
                    false -> {
                        startPlaying()
                        button.setImageResource(R.drawable.ic_stop_white_24dp)
                        logger.log(INFO, "playing started")
                    }
                    true -> {
                        stopPlaying()
                        button.setImageResource(R.drawable.ic_play_arrow_white_24dp)
                        logger.log(INFO, "playing stopped")
                    }
                }
                isPlaying = !isPlaying
            }
        }
    }
}
