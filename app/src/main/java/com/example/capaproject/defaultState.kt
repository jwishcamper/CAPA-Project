package com.example.capaproject

class defaultState(newCstate: CAPAstate,val context: MainActivity) : CAPAhandler{
    var capastate : CAPAstate = newCstate

    override fun updateGUI() {
        //do logic and pass gui elements back to function in main
        val hashMap : HashMap<String, Int> = HashMap()
        hashMap["alarmDisplay"] = 34
        hashMap["mediaPlayer"] = 50
        context.buildGUI(hashMap)
    }
}