package com.example.capaproject

import android.content.ComponentName

interface CAPAhandler {
    fun updateGUI()
    fun updateGUI(map : HashMap<ComponentName,Double>)
}