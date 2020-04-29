package com.example.capaproject


class UserPrefApps{
    var clock : widgetHolder? = null
    var music : widgetHolder? = null
    var search : widgetHolder? = null
    var email : widgetHolder? = null
    var calendar : widgetHolder? = null
    var notes : widgetHolder? = null
    var weather : widgetHolder? = null

    fun isEmpty() : Boolean {
        if(clock==null && music==null && search==null && email==null && calendar==null && notes==null && weather==null)
            return true
        return false
    }
    fun getAttr(name : String) : widgetHolder? {
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