package com.example.capaproject

import android.content.Context
import android.os.Bundle
import android.provider.ContactsContract
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import android.widget.ArrayAdapter

var map = HashMap<String, String>()
lateinit var db : DatabaseHandler

class Survey() : AppCompatActivity() {

    constructor(maps: HashMap<String, String>,context: Context) : this() {
        map = maps
        db = DatabaseHandler(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_survey)

        if(map != null) {
            val homeAddr: EditText = findViewById(R.id.home)
            val workAddr: EditText = findViewById(R.id.work)
            val schoolAddr: EditText = findViewById(R.id.school)
            val genderSpin: Spinner = findViewById(R.id.gender)
            val monthSpin: Spinner = findViewById(R.id.month)
            val daySpin: Spinner = findViewById(R.id.date)
            val yearSpin: Spinner = findViewById(R.id.year)

            homeAddr.setText(map["Home"])
            workAddr.setText(map["Work"])
            schoolAddr.setText(map["School"])

            //setting gender spinner
            var compareValue = map["Gender"]
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
            val birth: List<String>? = map["BirthDay"]?.split("/")

            //setting month spinner
            compareValue = birth?.get(0)
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
            compareValue = birth?.get(1)
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

            for (x in Calendar.getInstance().get(Calendar.YEAR) downTo 1930){
                list_of_items.add(x.toString())
            }

            compareValue = birth?.get(2)
            adapter =ArrayAdapter(this, android.R.layout.simple_spinner_item,
                list_of_items as List<CharSequence>
            )

            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            yearSpin.setAdapter(adapter)
            if (compareValue != null) {
                val spinnerPosition = adapter.getPosition(compareValue)
                yearSpin.setSelection(spinnerPosition)
            }

        }

    }

    fun clickBtn(view: View){

        val homeAddr: EditText = findViewById(R.id.home)
        val workAddr: EditText = findViewById(R.id.work)
        val schoolAddr: EditText = findViewById(R.id.school)
        val genderSpin: Spinner = findViewById(R.id.gender)
        val monthSpin: Spinner = findViewById(R.id.month)
        val daySpin: Spinner = findViewById(R.id.date)
        val yearSpin: Spinner = findViewById(R.id.year)

        //check if addresses exist in google maps


        val home = homeAddr.text.toString()
        val work = workAddr.text.toString()
        val school = schoolAddr.text.toString()
        val gender = genderSpin.selectedItem.toString()
        val month = monthSpin.selectedItem.toString()
        val day = daySpin.selectedItem.toString()
        val yr = yearSpin.selectedItem.toString()

        val map = HashMap<String, String>()
        map["Home"] = home
        map["Work"] = work
        map["School"] = school
        map["Gender"] = gender
        map["BirthDay"] = "$month/$day/$yr"

        var profileObj = UserProfile(home,work,school,gender,"$month/$day/$yr")

        db.updateSurveyInfo(profileObj)
        //db.updateSurveyInfo(map)


        finish()
    }

}