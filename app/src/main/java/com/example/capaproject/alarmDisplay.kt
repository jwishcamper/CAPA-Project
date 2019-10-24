package com.example.capaproject


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


class alarmDisplay : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val inf = inflater.inflate(R.layout.fragment_alarm_display, container, false)

        val resolver = activity!!.contentResolver
        val alarmTxtBox = inf.findViewById<TextView>(R.id.alarmText)
        val removeButton = inf.findViewById<Button>(R.id.removeButton)
        val newButton = inf.findViewById<Button>(R.id.addButton)

        updateNextAlarm(alarmTxtBox,resolver)

        newButton.setOnClickListener {
            setAlarm()
        }
        removeButton.setOnClickListener {   
            removeAlarm()
        }

        return inf
    }

    private fun setAlarm() {
        val i = Intent(AlarmClock.ACTION_SET_ALARM)
        if (i.resolveActivity(activity!!.packageManager) != null) {
            startActivity(i) }
    }
    private fun removeAlarm(){
        val i = Intent(AlarmClock.ACTION_DISMISS_ALARM)
        if (i.resolveActivity(activity!!.packageManager) != null) {
            startActivity(i) }
    }
    private fun updateNextAlarm(txt:TextView,res:ContentResolver){
        fixedRateTimer("timer",false,0,1000){
            this@alarmDisplay.activity!!.runOnUiThread {
                var alm = Settings.System.getString(res, Settings.System.NEXT_ALARM_FORMATTED)
                alm = if(alm == "") {
                    "No Alarms currently set." }
                else{
                    "Next alarm set for $alm" }
                txt.text = alm

            }
        }
    }
}
