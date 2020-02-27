package com.example.capaproject

import android.appwidget.AppWidgetProviderInfo
import android.util.Log

class defaultState(newCstate: CAPAstate,val context: MainActivity, private val prefs : UserPrefApps, val db: DatabaseHandler) : CAPAhandler{
    var capastate : CAPAstate = newCstate

    override fun updateGUI() {
        //do logic and pass gui elements back to function in main
        val hashMap : HashMap<widgetHolder, Double> = HashMap()

        //Store "default" for each state here.
        //hashMap[prefs.search] = 1.0
        //hashMap[prefs.weather] = 0.0

        context.buildGUI(hashMap)
    }
    override fun updateGUI(map : HashMap<widgetHolder,Double>){
        context.buildGUI(map)
    }
}