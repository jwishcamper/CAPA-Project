package com.example.capaproject

import android.content.ComponentName

class UserPrefApps {
    private val emptyCN = ComponentName("","")
    var clock = ComponentName("","")
    var music = ComponentName("","")

    fun isEmpty() : Boolean {
        if(clock.className=="" || music.className =="")
            return true
        return false
    }
    fun getAttr(name : String) : ComponentName {
        return when(name) {
            in "Clock" -> clock
            "Music" -> music
            else -> emptyCN
        }
    }
}