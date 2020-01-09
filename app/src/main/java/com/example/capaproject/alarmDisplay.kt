@file:Suppress("DEPRECATION")

package com.example.capaproject


import android.annotation.SuppressLint
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.content.ContentResolver
import android.content.Intent
import android.provider.AlarmClock
import android.widget.Button
import kotlin.concurrent.fixedRateTimer
import android.content.Context.ALARM_SERVICE
import androidx.core.content.ContextCompat.getSystemService
import android.app.AlarmManager
import android.content.Context


class alarmDisplay : Fragment() {

    private lateinit var inf : View
    private lateinit var alarmTxtBox : TextView
    private lateinit var resolver : ContentResolver

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        inf = inflater.inflate(R.layout.fragment_alarm_display, container, false)

        resolver = activity!!.contentResolver
        alarmTxtBox = inf.findViewById(R.id.alarmText)
        val removeButton = inf.findViewById<Button>(R.id.removeButton)
        val newButton = inf.findViewById<Button>(R.id.addButton)

        updateNextAlarm()

        newButton.setOnClickListener {
            setAlarm()
        }
        removeButton.setOnClickListener {   
            removeAlarm()
        }

        return inf
    }

    override fun onResume() {
        super.onResume()
        updateNextAlarm()
    }

    private fun setAlarm() {
        val i = Intent(AlarmClock.ACTION_SET_ALARM)
        if (i.resolveActivity(activity!!.packageManager) != null) {
            startActivity(i) }
    }
    @SuppressLint("InlinedApi")
    private fun removeAlarm(){
        val i = Intent(AlarmClock.ACTION_DISMISS_ALARM)
        if (i.resolveActivity(activity!!.packageManager) != null) {
            startActivity(i) }
    }
    //maybe find a better way to update than with a timer
    private fun updateNextAlarm(){
        var alm = Settings.System.getString(resolver, Settings.System.NEXT_ALARM_FORMATTED)
        alm = if(alm == "") {
            "No Alarms currently set." }
        else{
            "Next alarm set for $alm" }
        alarmTxtBox.text = alm

    }

}
