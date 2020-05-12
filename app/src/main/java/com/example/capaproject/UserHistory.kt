package com.example.capaproject

import kotlin.math.absoluteValue

class UserHistory{
    var dateTime =""
    var userState =""
    var latitude = 0.0
    var longitude = 0.0

    constructor(dt : String, us : String, lat : Double, long : Double){
        dateTime = dt
        userState = us
        latitude = lat
        longitude = long
    }
    constructor(){
        dateTime = ""
        userState = ""
        latitude = 0.0
        longitude = 0.0
    }

    fun isEmpty() : Boolean{
        if(dateTime == "" && userState == "" && latitude == 0.0 && longitude == 0.0)
            return true
        return false
    }
    //Compares 2 UserHistory objects given a threshold in minutes. If their timestamps are both within threshold, returns true
    fun similarTo(uh : UserHistory, threshold : Int) : Boolean {
        //Convert timestamp into Int of hour and minute for both UserHistory objects
        val thisMin = getTimeInMinutes()
        val otherMin = uh.getTimeInMinutes()

        //Now, we can compare timestamps
        if((thisMin - otherMin).absoluteValue <= threshold || (thisMin - otherMin).absoluteValue >= (1440-threshold))
            return true

        return false
    }
    fun getTimeInMinutes() : Int {
        val thisList = dateTime.split(" ")
        var thisHR = thisList[3].split(":")[0].toInt()
        var thisMin = thisList[3].split(":")[1].toInt()
        if(thisList[4] =="pm" && thisHR != 12)
            thisHR+=12
        else if(thisList[4] =="am" && thisHR == 12)
            thisHR-=12
        thisMin += (60 * thisHR)
        return thisMin
    }

}
