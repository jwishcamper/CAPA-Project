package com.example.capaproject

import android.content.BroadcastReceiver
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.content.Intent
import android.media.AudioManager
import android.content.IntentFilter
import androidx.core.content.ContextCompat.getSystemService






class mediaPlayer : Fragment() {
    private lateinit var songTxtBox: TextView
    val mReceiver = object : BroadcastReceiver(){

        override fun onReceive(context: Context, intent: Intent){

            val artist = intent.getStringExtra("artist")
            val track = intent.getStringExtra("track")
            val playing = intent.getBooleanExtra("playing", false)

            if(!playing) songTxtBox.text = "No media currently playing"
            else songTxtBox.text = "$artist - $track"
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val inf = inflater.inflate(R.layout.fragment_media_player, container, false)

        songTxtBox = inf.findViewById<TextView>(R.id.currentlyPlaying)
        val playButton = inf.findViewById<Button>(R.id.playButton)
        val pauseButton = inf.findViewById<Button>(R.id.pauseButton)

        val iF = IntentFilter()
        iF.addAction("com.android.music.musicservicecommand")
        iF.addAction("com.android.music.metachanged")
        iF.addAction("com.android.music.playstatechanged")
        iF.addAction("com.android.music.updateprogress")

        activity!!.registerReceiver(mReceiver, iF)

        val manager = activity!!.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        if (!manager.isMusicActive) {
            songTxtBox.text="No media currently playing"
        }


        pauseButton.setOnClickListener {
            val mAudioManager = activity!!.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            mAudioManager.requestAudioFocus(null,AudioManager.STREAM_MUSIC,AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
        }
        playButton.setOnClickListener {
            val mAudioManager = activity!!.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            mAudioManager.abandonAudioFocus(null)
        }


        return inf
    }


}
