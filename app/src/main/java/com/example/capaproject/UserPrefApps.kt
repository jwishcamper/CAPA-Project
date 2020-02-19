package com.example.capaproject

import android.content.ComponentName
import java.io.Serializable




class UserPrefApps{
    private val emptyCN = ComponentName("","")
    var clock = ComponentName("","")
    var music = ComponentName("","")
    var search = ComponentName("","")
    var email = ComponentName("","")
    var calendar = ComponentName("","")
    var notes = ComponentName("","")
    var weather = ComponentName("","")

    fun isEmpty() : Boolean {
        if(clock.className=="" && music.className =="" && search.className =="" && email.className =="" && calendar.className =="" && notes.className =="" && weather.className =="")
            return true
        return false
    }
    fun getAttr(name : String) : ComponentName {
        return when(name) {
            in "Clock" -> clock
            "Music" -> music
            "Search" -> search
            "Email" -> email
            "Calendar" -> calendar
            "Notes" -> notes
            "Weather" -> weather
            else -> emptyCN
        }
    }
}