package com.example.capaproject

import android.appwidget.AppWidgetProviderInfo


interface CAPAhandler {
    fun updateGUI()
    fun updateGUI(map : HashMap<AppWidgetProviderInfo?,Double>)
}