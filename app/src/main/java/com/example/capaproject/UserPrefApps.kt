package com.example.capaproject

import android.appwidget.AppWidgetProviderInfo

class UserPrefApps{
    var clock : AppWidgetProviderInfo? = null
    var music : AppWidgetProviderInfo? = null
    var search : AppWidgetProviderInfo? = null
    var email : AppWidgetProviderInfo? = null
    var calendar : AppWidgetProviderInfo? = null
    var notes : AppWidgetProviderInfo? = null
    var weather : AppWidgetProviderInfo? = null

    fun isEmpty() : Boolean {
        if(clock==null && music==null && search==null && email==null && calendar==null && notes==null && weather==null)
            return true
        return false
    }
    fun getAttr(name : String) : AppWidgetProviderInfo? {
        return when(name) {
            in "Clock" -> clock
            "Music" -> music
            "Search" -> search
            "Email" -> email
            "Calendar" -> calendar
            "Notes" -> notes
            "Weather" -> weather
            else -> null
        }
    }
}