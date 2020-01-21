package com.example.capaproject

import android.content.ComponentName
import android.util.Log

class CAPAstate(context:MainActivity) {
    //this hashmap stores the current widgets in form String:int, where int is the weight
    var stateMap : HashMap<ComponentName, Double> = HashMap()
    //List of possible states stores as CAPAhandler objects
    var atWork : CAPAhandler = atWorkState(this,context)
    var default : CAPAhandler = defaultState(this,context)

    //current state will refer to one of the above possible state variables
    var currentState : CAPAhandler

    //set state to default initially
    init {
        currentState = default
    }
    //call this from mainActivity to change the user state and update GUI
    fun updateUserState(newState : String){
        when(newState){
            in "atWork" -> setState(atWork)
            else -> setState(default)
        }
    }
    //helper for updateUserState
    //if state has changed, build GUI based on hashmap. if hashmap is empty, build based on default.
    private fun setState(newState : CAPAhandler){
        if(newState != currentState) {
            currentState = newState
            if(stateMap.isEmpty())
                currentState.updateGUI()
            else
                currentState.updateGUI(stateMap)
        }
    }

    //called when a user adds a custom widget to state.
    fun addWidget(name:ComponentName){
        //add new widget name to hashmap
        val sorted = stateMap.toList().sortedBy { (_, value) -> value}.toMap()
        val firstKey = sorted.keys.toTypedArray()[0]
        val secondKey = sorted.keys.toTypedArray()[1]
        stateMap[firstKey] = (stateMap[firstKey]!!+stateMap[secondKey]!!)/2
        stateMap[name]=0.0

        /*
        //print keys/values for testing
        for (entry in stateMap) {
            Log.d("TAG",entry.key.className)
            Log.d("TAG",entry.value.toString())

        }*/

        //refresh display based on new hashmap.
        refresh()


        //save hashmap to database:
        //use nicks database object to save stateMap to database here
    }
    private fun refresh(){
        currentState.updateGUI(stateMap)
    }
    //returns a hashmap variable with current state - to save to database
    fun getList() : HashMap<ComponentName,Double> {
        return stateMap
    }
    //Load data from database into local hashmap variable
    private fun loadList(newMap : HashMap<ComponentName,Double>){
        //use Nick's database object to retrieve data and store in stateMap
        stateMap = newMap
    }
    //gets current state as a string
    fun getState() : String {
        return if(currentState==atWork) "atWork"
            else "default"

    }
}