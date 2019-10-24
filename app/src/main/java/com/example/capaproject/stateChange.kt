package com.example.capaproject

import java.util.*

//goal of stateChange class - use information such as time, day of week,
//location, and activity state to guess at the context of the user
class stateChange(){

    private fun getDateTime() : String {
        val min = Calendar.getInstance().get(Calendar.MINUTE)
        var hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val minString = if(min < 10){ "0$min"}
            else{ min.toString() }
        val ampm = if(hour < 12) { "am" }
            else { "pm" }
        if(hour>12)
            hour-=12
        if(hour==0)
            hour=12
        return "$hour:$minString $ampm"
    }
    private fun getDay() : String {
        var s = Calendar.getInstance().getDisplayName(Calendar.DAY_OF_WEEK,Calendar.LONG,Locale.getDefault()) as String
        s+= when(Calendar.getInstance().get(Calendar.HOUR_OF_DAY)){
            in 0..6 -> " Night"
            in 7..11 -> " Morning"
            in 12..18 -> " Afternoon"
            in 19..24 -> " Evening"
            else -> " Null"
        }
        return s
    }
    fun getContext() : String{
        return getDay() + ", " + getDateTime()
    }
}