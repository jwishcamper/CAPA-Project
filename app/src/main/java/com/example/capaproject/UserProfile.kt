package com.example.capaproject

class UserProfile(h:String, w:String, s:String, g:String, b:String){
    private var home = h
    private var work = w
    private var school = s
    private var gender = g
    private var birthDate = b

    fun getFieldNames(): ArrayList<String> {
        var list = ArrayList<String>()
        list.add("Home")
        list.add("School")
        list.add("Work")
        list.add("Gender")
        list.add("BirthDay")

        return list
    }

    fun getField(s:String): String{
        return when (s) {
            "Home" -> home
            "School" -> school
            "Work" -> work
            "Gender" -> gender
            "BirthDay" -> birthDate
            else -> "NULL"
        }
    }

}