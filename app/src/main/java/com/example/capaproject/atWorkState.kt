package com.example.capaproject

import android.content.ComponentName

class atWorkState(val newCstate: CAPAstate,val context: MainActivity) : CAPAhandler{
    var capastate : CAPAstate = newCstate

    override fun updateGUI() {
        //make a hash map for default workState, then build GUI
        val hashMap : HashMap<ComponentName, Double> = HashMap()
        val compAnalogClock = ComponentName(
            "com.google.android.googlequicksearchbox",
            "com.google.android.googlequicksearchbox.SearchWidgetProvider"
        )
        val compMusic = ComponentName(
            "com.google.android.music",
            "com.android.music.MediaAppWidgetProvider"
        )

        hashMap[compAnalogClock] = 1.0
        hashMap[compMusic]=0.0
        //hashMap["mediaPlayer"] = 50
        newCstate.stateMap = hashMap
        context.buildGUI(hashMap)
    }
    override fun updateGUI(map : HashMap<ComponentName,Double>){
        context.buildGUI(map)
    }
}