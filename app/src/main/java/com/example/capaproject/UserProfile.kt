package com.example.capaproject

class UserProfile(private var h:String,private var w:String,private var s:String,private var g:String,private var b:String){
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
        if (s == "Home")
            return home
        else if(s == "School")
            return school
        else if(s == "Work")
            return work
        else if(s == "Gender")
            return gender
        else if(s == "BirthDay")
            return birthDate
        return "NULL"
    }

}