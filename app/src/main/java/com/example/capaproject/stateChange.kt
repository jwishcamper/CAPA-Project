package com.example.capaproject

import java.util.*

//goal of stateChange class - use information such as time, day of week,
//location, and activity state to guess at the context of the user
class stateChange(){

    private fun getDateTime() : String {
        return Calendar.getInstance().get(Calendar.HOUR_OF_DAY).toString()+":"+Calendar.getInstance().get(Calendar.MINUTE).toString()
    }
    private fun getDay() : String {
        var s = Calendar.getInstance().getDisplayName(Calendar.DAY_OF_WEEK,Calendar.LONG,Locale.getDefault())
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