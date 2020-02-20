package com.example.capaproject

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.ActivityRecognition
import java.text.SimpleDateFormat
import java.util.*

//goal of stateManager class - use information such as time, day of week,
//location, and activity state to guess at the context of the user
@Suppress("DEPRECATION")
class stateManager(private val context : Context) : GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener  {

    var location = "None"
    var driving = false

    private var mApiClient: GoogleApiClient = GoogleApiClient.Builder(context)
        .addApi(ActivityRecognition.API)
        .addConnectionCallbacks(this)
        .addOnConnectionFailedListener(this)
        .build()

    init {
        //comment following line out for testing on emulator:
        //mApiClient.connect()
    }

    override fun onConnected(bundle: Bundle?) {
        val intent = Intent(context, ActivityIntentService::class.java)
        val pendingIntent =
            PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(
            mApiClient,
            3000,
            pendingIntent
        )
    }

    override fun onConnectionSuspended(i: Int) {

    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {

    }



    //returns time as string eg: "9:23 pm"
    private fun getTime() : String {
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
    //returns day of week as string eg: "Thursday Afternoon"
    //if you just want the day, copy the first line of the function
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

    private fun getDate(): String {
        return SimpleDateFormat("MM-dd-yyyy", Locale.getDefault()).format(Date())
    }

    fun getDateTime() : String{
        return "${getDate()}, ${getTime()}"
    }
    fun getString() : String {
        return "${getDay()}, ${getDateTime()}     Activity: ${MainActivity.currentActivity}    Context: ${getContext()}"
    }
    fun getContext() : String{

        //if driving
        if(MainActivity.currentActivity == "In Vehicle"){
            driving = true
            return context.resources.getString(R.string.stateDriving)
        }
        else if(MainActivity.currentActivity == "Walking" || MainActivity.currentActivity == "On Foot"){
            driving = false
        }

        if(driving){
            return context.resources.getString(R.string.stateDriving)
        }

        return when{
            //at work, school, home
            location!="None" && MainActivity.currentActivity != "In Vehicle" ->{
                location
            }
            else -> context.resources.getString(R.string.stateDefault)
        }

    }
}
