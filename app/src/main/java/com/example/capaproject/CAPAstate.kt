package com.example.capaproject

class CAPAstate(context:MainActivity) {
    var atWork : CAPAhandler = atWorkState(this,context)
    var default : CAPAhandler = defaultState(this,context)
    var currentState : CAPAhandler

    init {
        currentState = default
    }
    fun updateUserState(newState : String){
        when(newState){
            in "atWork" -> setState(atWork)
            else -> setState(default)
        }
    }
    private fun setState(newState : CAPAhandler){
        if(newState != currentState) {
            currentState = newState
            currentState.updateGUI()
        }
    }
}