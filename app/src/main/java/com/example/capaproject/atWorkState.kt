package com.example.capaproject

import android.appwidget.AppWidgetProviderInfo

class atWorkState(val newCstate: CAPAstate,val context: MainActivity,private val prefs : UserPrefApps) : CAPAhandler{
    var capastate : CAPAstate = newCstate

    override fun updateGUI() {
        //make a hash map for default workState, then build GUI
        val hashMap : HashMap<widgetHolder, Double> = HashMap()

        //Store "default" for each state here. In this case, we want clock and music.
        hashMap[prefs.clock!!] = 1.0
        hashMap[prefs.music!!] = 0.0

        //Add prefs.clock and prefs.music to database with appropriate tags

        newCstate.stateMap = hashMap
        context.buildGUI(hashMap)
    }
    override fun updateGUI(map : HashMap<widgetHolder,Double>){
        context.buildGUI(map)
    }
}