package com.example.capaproject

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import java.io.IOException

class DrivingFragment : Fragment(){

    val drivingFrag : MapsFragment = MapsFragment()
    internal lateinit var view: View
    lateinit var db: DatabaseHandler
    lateinit var user: UserProfile

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        view = inflater.inflate(R.layout.driving_fragment, null)

        val ft = childFragmentManager.beginTransaction()
        ft.replace(R.id.fragmentMap, drivingFrag)
        ft.commit()

        db = DatabaseHandler(activity!!.applicationContext)
        user = db.getSurvey()

        var hBtn : Button = view.findViewById(R.id.quickH)
        hBtn.setOnClickListener { clickBtn(null, "Home") }
        var wBtn : Button = view.findViewById(R.id.quickW)
        wBtn.setOnClickListener { clickBtn(null, "Work") }
        var sBtn : Button = view.findViewById(R.id.quickS)
        sBtn.setOnClickListener { clickBtn(null, "School") }

        var exitBtn : Button = view.findViewById(R.id.exitMapBtn)
        exitBtn.setOnClickListener { closeFragment(null) }

        return view
    }

    fun locationChanged(location: Location){
        drivingFrag.newLocation(location)
    }

    fun closeFragment(view: View?) {
        (activity as MainActivity).surpressDriving()
    }

    fun clickBtn(view: View?, place: String){
        if(user.getField(place) == ""){
            Toast.makeText(activity!!.applicationContext,"NO DATA FOR ${place.toUpperCase()} ADDRESS", Toast.LENGTH_SHORT).show()
        }

        var location = getLocationFromAddress(activity!!.applicationContext, user.getField(place))
        if (location != null) {
            drivingFrag.changeLocation(location.latitude, location.longitude, place)
        }
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

