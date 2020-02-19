package com.example.capaproject

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import android.widget.ArrayAdapter
import androidx.core.view.ViewCompat
import java.io.IOException
import java.lang.Exception

lateinit var prof: UserProfile
lateinit var db: DatabaseHandler

class Survey() : AppCompatActivity() {

    constructor(profile: UserProfile, context: Context) : this() {
        prof = profile
        db = DatabaseHandler(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_survey)


        val homeAddr: EditText = findViewById(R.id.home)
        val workAddr: EditText = findViewById(R.id.work)
        val schoolAddr: EditText = findViewById(R.id.school)
        val genderSpin: Spinner = this.findViewById(R.id.gender)
        val monthSpin: Spinner = findViewById(R.id.month)
        val daySpin: Spinner = findViewById(R.id.date)
        val yearSpin: Spinner = findViewById(R.id.year)

        homeAddr.setText(prof.getField("Home"))
        workAddr.setText(prof.getField("Work"))
        schoolAddr.setText(prof.getField("School"))

        //setting gender spinner
        var g = prof.getField("Gender")
        if (g == "")
            g = "Other"
        var compareValue = g
        var adapter = ArrayAdapter.createFromResource(
            this,
            R.array.gender,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        genderSpin.setAdapter(adapter)
        if (compareValue != null) {
            val spinnerPosition = adapter.getPosition(compareValue)
            genderSpin.setSelection(spinnerPosition)
        }

        //dividing birthday
        var b = prof.getField("BirthDay")
        if (b=="")
            b = "01/01/1930"
        val birth: List<String> = b.split("/")

        //setting month spinner
        compareValue = birth[0]
        adapter = ArrayAdapter.createFromResource(
            this,
            R.array.month,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        monthSpin.setAdapter(adapter)
        if (compareValue != null) {
            val spinnerPosition = adapter.getPosition(compareValue)
            monthSpin.setSelection(spinnerPosition)
        }

        //setting day spinner
        compareValue = birth[1]
        adapter = ArrayAdapter.createFromResource(
            this,
            R.array.date,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        daySpin.setAdapter(adapter)
        if (compareValue != null) {
            val spinnerPosition = adapter.getPosition(compareValue)
            daySpin.setSelection(spinnerPosition)
        }

        //setting year spinner
        var list_of_items = ArrayList<String>()

        for (x in Calendar.getInstance().get(Calendar.YEAR) downTo 1930) {
            list_of_items.add(x.toString())
        }

        compareValue = birth.get(2)
        adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_item,
            list_of_items as List<CharSequence>
        )

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        yearSpin.setAdapter(adapter)
        if (compareValue != null) {
            val spinnerPosition = adapter.getPosition(compareValue)
            yearSpin.setSelection(spinnerPosition)
        }


    }

    fun clickBtn(view: View) {

        val homeAddr: EditText = findViewById(R.id.home)
        val workAddr: EditText = findViewById(R.id.work)
        val schoolAddr: EditText = findViewById(R.id.school)

        val genderSpin: Spinner = findViewById(R.id.gender)
        val monthSpin: Spinner = findViewById(R.id.month)
        val daySpin: Spinner = findViewById(R.id.date)
        val yearSpin: Spinner = findViewById(R.id.year)

        val home = homeAddr.text.toString()
        val work = workAddr.text.toString()
        val school = schoolAddr.text.toString()
        val gender = genderSpin.selectedItem.toString()
        val month = monthSpin.selectedItem.toString()
        val day = daySpin.selectedItem.toString()
        val yr = yearSpin.selectedItem.toString()

        //checking if home address exists
        try {
            var tries = getLocationFromAddress(this, home)
        }
        catch (e: Exception){
            Toast.makeText(applicationContext,"Home Address Does Not Exist",Toast.LENGTH_SHORT).show()
            return
        }

        //checking if work address exists
        try {
            var tries = getLocationFromAddress(this, work)
        }
        catch (e: Exception){
            Toast.makeText(applicationContext,"Work Address Does Not Exist",Toast.LENGTH_SHORT).show()
            return
        }

        //checking if school address exists
        try {
            var tries = getLocationFromAddress(this, school)
        }
        catch (e: Exception){
            Toast.makeText(applicationContext,"School Address Does Not Exist",Toast.LENGTH_SHORT).show()
            return
        }

/*
        val map = HashMap<String, String>()
        map["Home"] = home
        map["Work"] = work
        map["School"] = school
        map["Gender"] = gender
        map["BirthDay"] = "$month/$day/$yr"
*/
        //making profile object for information

        val profileObj = UserProfile(home, work, school, gender, "$month/$day/$yr")


        db.updateDatabaseSurvey(profileObj)


        finish()
    }

    //translating lat and long from a string address
    fun getLocationFromAddress(context: Context, strAddress: String): Location? {

        val coder = Geocoder(context)
        val address: List<Address>?
        var p1: Location? = null

        try {
            // May throw an IOException
            address = coder.getFromLocationName(strAddress, 5)
            if (address == null) {
                return null
            }

            val location = address[0]
            p1 = Location("service Provider")
            p1.latitude = location.latitude
            p1.longitude = location.longitude

        } catch (ex: IOException) {

            ex.printStackTrace()
        }

        return p1
    }

}