package com.example.capaproject

import android.appwidget.AppWidgetProviderInfo
import android.content.ComponentName

class CAPAstate(val context:MainActivity, val db : DatabaseHandler, val prefs : UserPrefApps) {
    //this hashmap stores the current widgets in form ComponentName : Double; the doubt is the weight of the widget
    var stateMap : HashMap<AppWidgetProviderInfo?, Double> = HashMap()
    //List of possible states stores as CAPAhandler objects
    var atWork : CAPAhandler = atWorkState(this,context,prefs)
    var default : CAPAhandler = defaultState(this,context,prefs)

    //current state will refer to one of the above possible state variables
    var currentState : CAPAhandler

    //set state to default initially
    init {
        currentState = default
    }
    //call this from mainActivity to change the user state and update GUI
    fun updateUserState(newState : String){

        //save hashmap for current state to database
        db.updateDatabaseState(getState(),stateMap)

        when(newState){
            in context.resources.getString(R.string.stateWork) -> setState(atWork)
            else -> setState(default)
        }

        //use following line when location turned on:
        //val uh = UserHistory(context.stateHelper.getDateTime(),getState(),context.mLastLocation.latitude,context.mLastLocation.longitude)

        //use the following line for use on emulator:
        val uh = UserHistory(context.stateHelper.getDateTime(),getState(),0.0,0.0)

        db.updateUserHistory(uh)

    }
    //helper for updateUserState
    //if state has changed, build GUI based on hashmap. if hashmap is empty, build based on default.
    private fun setState(newState : CAPAhandler){
        if(newState != currentState) {
            currentState = newState
            //load the user prefs from database
            stateMap = db.getStateData(getState())!!
            if(stateMap.isEmpty())
                currentState.updateGUI()
            else
                currentState.updateGUI(stateMap)
        }
    }
    fun removeWidget(name:AppWidgetProviderInfo?){
        if(name!=null) {
            stateMap.remove(name)
            refresh()
        }
    }
    //called when a user adds a custom widget to state.
    fun addWidget(name:AppWidgetProviderInfo?){
        if(name!=null) {
            //add new widget name to hashmap
            //Logic to add new widget at slot 0 then change weight of next one up
            val sorted = stateMap.toList().sortedBy { (_, value) -> value }.toMap()
            if(sorted.isNotEmpty()) {
                val firstKey = sorted.keys.toTypedArray()[0]

                //if there are 2 or more elements, squish the smallest's value between 0 and the second smallest value
                if (sorted.size > 1) {
                    val secondKey = sorted.keys.toTypedArray()[1]
                    stateMap[firstKey] = (stateMap[firstKey]!! + stateMap[secondKey]!!) / 2
                }
                //if there is only 1 element, just set the only element to 1.0
                else
                    stateMap[firstKey] = 1.0
            }
            //set new CN to weight 0.0
            stateMap[name] = 0.0


            //for debugging
            /*
        //print keys/values for testing
        for (entry in stateMap) {
            Log.d("TAG",entry.key.className)
            Log.d("TAG",entry.value.toString())
        }*/

            //refresh display based on new hashmap.
            refresh()
        }
    }
    fun refresh(){
        currentState.updateGUI(stateMap)
    }
    //returns a hashmap variable with current state - to save to database
    fun getList() : HashMap<AppWidgetProviderInfo?,Double> {
        return stateMap
    }
    //Load data from database into local hashmap variable
    private fun loadList(newMap : HashMap<AppWidgetProviderInfo?,Double>){
        //use Nick's database object to retrieve data and store in stateMap
        stateMap = newMap
    }
    //gets current state as a string
    fun getState() : String {
        return if(currentState==atWork) context.resources.getString(R.string.stateWork)
        else context.resources.getString(R.string.stateDefault)

    }
}
