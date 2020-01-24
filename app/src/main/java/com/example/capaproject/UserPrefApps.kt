package com.example.capaproject

import android.content.ComponentName

class UserPrefApps {
    var clock = ComponentName("","")
    var music = ComponentName("","")

    fun isEmpty() : Boolean {
        if(clock.className=="" || music.className =="")
            return true
        return false
    }
}