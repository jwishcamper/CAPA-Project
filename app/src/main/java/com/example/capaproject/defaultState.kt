package com.example.capaproject

import android.appwidget.AppWidgetProviderInfo

class defaultState(newCstate: CAPAstate,val context: MainActivity, prefs : UserPrefApps) : CAPAhandler{
    var capastate : CAPAstate = newCstate

    override fun updateGUI() {
        //do logic and pass gui elements back to function in main
        val hashMap : HashMap<AppWidgetProviderInfo?, Double> = HashMap()
        //hashMap["alarmDisplay"] = 0.0
        //hashMap["mediaPlayer"] = 1.0
        context.buildGUI(hashMap)
    }
    override fun updateGUI(map : HashMap<AppWidgetProviderInfo?,Double>){
        context.buildGUI(map)
    }
}