package com.example.capaproject

import java.util.*
import kotlin.math.absoluteValue

class UserPatterns(var db : DatabaseHandler, var context : MainActivity) {

    //Difference in time in minutes to consider 2 timestamps "similar"
    private val similarTimeThreshold = 30

    //Time in minutes before pattern to notify user
    private val timeBeforeThreshold = 30

    //Will return either "None", "Work", "School", or "Home" depending on if a pattern is found
    //If found, will also return string of timestamp that pattern starts
    fun checkForStatePattern() : String {
        //Get info from Database
        val dataList = db.getUserHistory()

        val workList = mutableListOf<UserHistory>()
        val homeList = mutableListOf<UserHistory>()
        val schoolList = mutableListOf<UserHistory>()
        //separate into different lists by state - We only care about 3 states
        for(entry in dataList) {
            when(entry.userState){
                in context.resources.getString(R.string.stateWork) -> workList.add(entry)
                context.resources.getString(R.string.stateHome) -> homeList.add(entry)
                context.resources.getString(R.string.stateSchool) -> schoolList.add(entry)
            }
        }
        var result = UserHistory("","",0.0,0.0)
        if(workList.size > 5)
            result = checkStateList(workList)
        if(result.userState=="" && homeList.size > 5)
            result = checkStateList(homeList)
        if(result.userState==""&&schoolList.size > 5)
            result = checkStateList(schoolList)

        //If result is not empty, we have a pattern
        if(result.userState!=""){
            var min = Calendar.getInstance().get(Calendar.MINUTE)
            val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            min += (hour*60)
            //min is current time in minutes
            val resultMin = result.getTimeInMinutes()
            if((min - resultMin).absoluteValue <= timeBeforeThreshold || (min - resultMin).absoluteValue >= (1440-timeBeforeThreshold))
                return result.userState
        }
        return "None"
    }

    /*
        Create a variable to store the max count, count = 0
        In addition to max count, we need to store timestamp of "similar" items
        Traverse through the array from start to end.
        For every element in the array run another loop to find the count of similar elements in the given array.
        If the count is greater than the max count update the max count and store the index in another variable.
        If the maximum count is greater than the half the size of the array, return Work and timestamp.
    */
    private fun checkStateList(list : MutableList<UserHistory>) : UserHistory{
        var maxCount = 0
        var maxUH = UserHistory()

        for(entry in list){
            var subCount = 0
            for(otherEntry in list){
                if(entry.similarTo(otherEntry,similarTimeThreshold)){
                    subCount++
                }
            }
            if(subCount > maxCount){
                maxCount = subCount
                maxUH=entry
            }
        }
        //If there are 50% or more entries from the same state within SimilarTimeThreshold time, then we have a pattern
        if(maxCount >= (list.size/2))
            return maxUH
        return UserHistory()
    }
}