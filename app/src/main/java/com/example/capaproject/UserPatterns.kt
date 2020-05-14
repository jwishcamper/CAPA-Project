package com.example.capaproject

import android.location.Location
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import java.util.*
import kotlin.math.absoluteValue
import org.json.JSONObject


class UserPatterns(var db : DatabaseHandler, var context : MainActivity) {

    //Difference in time in minutes to consider 2 timestamps "similar"
    private val similarTimeThreshold = 30

    //Initial function call - check if desired address exists before taking time to do work
    fun checkForStatePattern(curLoc : Location, profile : UserProfile){
        if(profile.getField("Work")!=""){
            getTimeToLoc(context.resources.getString(R.string.stateWork),curLoc.latitude,curLoc.longitude,profile.getField("Work"))
        }
        if(profile.getField("School")!=""){
            getTimeToLoc(context.resources.getString(R.string.stateSchool),curLoc.latitude,curLoc.longitude,profile.getField("School"))
        }
        if(profile.getField("Home")!=""){
            getTimeToLoc(context.resources.getString(R.string.stateHome),curLoc.latitude,curLoc.longitude,profile.getField("Home"))
        }
    }

    //Query google maps for the time from current location to the state we are checking
    private fun getTimeToLoc(loc:String,lat1:Double,lon1:Double,loc2:String) {
        val urlDirections = "https://maps.googleapis.com/maps/api/directions/json?origin=$lat1,$lon1&destination=$loc2&key=AIzaSyCl0_5pIZ5g3KiUdvYn7mbGtPsg50lSdVQ"
        val directionsRequest = object : StringRequest(Method.GET, urlDirections, Response.Listener<String> {
                response ->
            val jsonResponse = JSONObject(response)
            // Get routes
            val routes = jsonResponse.getJSONArray("routes")
            val legs = routes.getJSONObject(0).getJSONArray("legs")
            when(loc){
                in context.resources.getString(R.string.stateWork) -> checkPattern(context.resources.getString(R.string.stateWork),legs.getJSONObject(0).getJSONObject("duration").getInt("value") /60)
                context.resources.getString(R.string.stateHome) -> checkPattern(context.resources.getString(R.string.stateHome),legs.getJSONObject(0).getJSONObject("duration").getInt("value") /60)
                context.resources.getString(R.string.stateSchool) -> checkPattern(context.resources.getString(R.string.stateSchool),legs.getJSONObject(0).getJSONObject("duration").getInt("value") /60)
            }
        }, Response.ErrorListener {
        }){}

        val requestQueue = Volley.newRequestQueue(context)
        requestQueue.add(directionsRequest)
    }

    //Refine a list of userHistory entries just for the state we are checking
    //If we find a pattern, check if timeThreshold+10 is close to current time.
    //If so, prompt user that they need to leave soon.
    private fun checkPattern(state:String,timeThreshold : Int){
        //Get info from Database
        val dataList = db.getUserHistory()

        val refinedList = mutableListOf<UserHistory>()

        //separate into different lists by state - We only care about 3 states
        for(entry in dataList) {
            if(entry.userState==state){
                refinedList.add(entry)
            }
        }

        var result = UserHistory("","",mutableSetOf(),0.0,0.0)
        if(refinedList.size > 5)
            result = checkStateList(refinedList)

        //If result is not empty, we have a pattern
        if(result.userState!=""){
            var min = Calendar.getInstance().get(Calendar.MINUTE)
            val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            min += (hour*60)
            //min is current time in minutes
            val resultMin = result.getTimeInMinutes()
            if((min - resultMin).absoluteValue <= (timeThreshold+10) || (min - resultMin).absoluteValue >= (1440-(timeThreshold+10)))
                context.dialogQuickNav(result.userState)
        }
    }

    /*
       Create a variable to store the max count, count = 0
       In addition to max count, we need to store timestamp of "similar" items - this is maxUH
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
        //If we didn't find something, return an empty userHistory object
        return UserHistory()
    }
}