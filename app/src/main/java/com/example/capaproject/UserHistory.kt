package com.example.capaproject

class UserHistory{
    var dateTime = ""
    var userState = ""
    var latitude = 0.0
    var longitude = 0.0

    fun isEmpty() : Boolean{
        if(dateTime == "" && userState == "" && latitude == 0.0 && longitude == 0.0)
            return true
        return false
    }
}