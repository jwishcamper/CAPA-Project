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
import kotlinx.android.synthetic.main.activity_survey.*
import java.io.IOException
import java.lang.Exception
import java.util.regex.Pattern

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

        val genderSpin: Spinner = this.findViewById(R.id.gender)
        val monthSpin: Spinner = findViewById(R.id.month)
        val daySpin: Spinner = findViewById(R.id.date)
        val yearSpin: Spinner = findViewById(R.id.year)

        val homeStr : List<String> = prof.getField("Home").split('|')
        if(homeStr[0] != "") {
            home.setText(homeStr[0])
            homeCity.setText(homeStr[1])
            homeState.setText(homeStr[2])
            homeZip.setText(homeStr[3])
        }

        val workStr : List<String> = prof.getField("Work").split('|')
        if(workStr[0] != "") {
            work.setText(workStr[0])
            workCity.setText(workStr[1])
            workState.setText(workStr[2])
            workZip.setText(workStr[3])
        }

        val schoolStr : List<String> = prof.getField("School").split('|')
        if(schoolStr[0] != "") {
            school.setText(schoolStr[0])
            schoolCity.setText(schoolStr[1])
            schoolState.setText(schoolStr[2])
            schoolZip.setText(schoolStr[3])
        }

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

    private fun validate(stream : String, type : String): Boolean{
        //validating city or state
        if (type == "City" || type == "State"){
            if (Pattern.compile("[^[A-Za-z]\\s]").matcher(stream).find()){
                Toast.makeText(applicationContext,"$type Can only contain letters",Toast.LENGTH_SHORT).show()
                return false
            }
        }
        //validating zip
        else if (type == "Zip"){
            if (Pattern.compile("[^[0-9]\\s]").matcher(stream).find()){
                Toast.makeText(applicationContext,"$type Can only contain numbers",Toast.LENGTH_SHORT).show()
                return false
            }
        }
        //validating street
        else if (Pattern.compile("[^\\w\\s]").matcher(stream).find()){
            Toast.makeText(applicationContext,"Address Cannot Contain Special Characters",Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    fun clickBtn(view: View) {

        val genderSpin: Spinner = findViewById(R.id.gender)
        val monthSpin: Spinner = findViewById(R.id.month)
        val daySpin: Spinner = findViewById(R.id.date)
        val yearSpin: Spinner = findViewById(R.id.year)

        //validating fields
        if (home.text.isBlank()){
            if (homeCity.text.isBlank() || homeState.text.isBlank() || homeZip.text.isBlank()) {
                Toast.makeText(applicationContext, "All Home Address Fields Must Be Filled", Toast.LENGTH_LONG).show()
                return
            }
        }
        if (!work.text.isBlank()){
            if (workCity.text.isBlank() || workState.text.isBlank() || workZip.text.isBlank()) {
                Toast.makeText(applicationContext, "All Work Address Fields Must Be Filled", Toast.LENGTH_LONG).show()
                return
            }
        }
        if (!school.text.isBlank()){
            if (schoolCity.text.isBlank() || schoolState.text.isBlank() || schoolZip.text.isBlank()) {
                Toast.makeText(applicationContext, "All School Address Fields Must Be Filled", Toast.LENGTH_LONG).show()
                return
            }
        }

        when {
            !validate(home.text.toString(), "Street") -> return
            !validate(homeCity.text.toString(), "City") -> return
            !validate(homeState.text.toString(), "State") -> return
            !validate(homeZip.text.toString(), "Zip") -> return
            !validate(work.text.toString(), "Street") -> return
            !validate(workCity.text.toString(), "City") -> return
            !validate(workState.text.toString(), "State") -> return
            !validate(workZip.text.toString(), "Zip") -> return
            !validate(school.text.toString(), "Street") -> return
            !validate(schoolCity.text.toString(), "City") -> return
            !validate(schoolState.text.toString(), "State") -> return
            !validate(schoolZip.text.toString(), "Zip") -> return
        }

        val home = home.text.toString() + "|" + homeCity.text.toString() + "|" + homeState.text.toString() + "|" + homeZip.text.toString()
        val work = work.text.toString() + "|" + workCity.text.toString() + "|" + workState.text.toString() + "|" + workZip.text.toString()
        val school = school.text.toString() + "|" + schoolCity.text.toString() + "|" + schoolState.text.toString() + "|" + schoolZip.text.toString()
        val gender = genderSpin.selectedItem.toString()
        val month = monthSpin.selectedItem.toString()
        val day = daySpin.selectedItem.toString()
        val yr = yearSpin.selectedItem.toString()

        //checking if address exists
        var trys : String = work

        try {

            if(work != "|||") {
                trys = work.replace('|', ' ')
                var tries = getLocationFromAddress(this, trys)
            }
            if(school != "|||") {
                trys = school.replace('|', ' ')
                var tries = getLocationFromAddress(this, trys)
            }
            if(home != "|||") {
                trys = home.replace('|', ' ')
                var tries = getLocationFromAddress(this, trys)
            }
        }
        catch (e: Exception){
            Toast.makeText(applicationContext,"$trys Does Not Exist",Toast.LENGTH_SHORT).show()
            return
        }

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
