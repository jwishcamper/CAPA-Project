package com.example.capaproject

import android.content.ComponentName

class atWorkState(val newCstate: CAPAstate,val context: MainActivity, val prefs : UserPrefApps) : CAPAhandler{
    var capastate : CAPAstate = newCstate

    override fun updateGUI() {
        //make a hash map for default workState, then build GUI
        val hashMap : HashMap<ComponentName, Double> = HashMap()

        hashMap[prefs.clock] = 1.0
        hashMap[prefs.music] = 0.0

        newCstate.stateMap = hashMap
        context.buildGUI(hashMap)
    }
    override fun updateGUI(map : HashMap<ComponentName,Double>){
        context.buildGUI(map)
    }
}