package com.example.capaproject

import android.graphics.Color
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.android.volley.toolbox.Volley.*

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.android.PolyUtil
import org.json.JSONObject

class MapsFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    internal lateinit var view: View
    private var userLocation: Location = Location("")
    private val threashold = 400

    companion object{
        var map: GoogleMap? = null
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        view = inflater.inflate(R.layout.activity_maps_fragment, null)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = childFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        userLocation.latitude = 47.0
        userLocation.longitude = -121.0

        return view
    }

    private fun closefragment() {
        activity!!.supportFragmentManager.beginTransaction().remove(this).commit()
    }

    /* override fun onCreate(savedInstanceState: Bundle?) {
         super.onCreate(savedInstanceState)
         setContentView(R.layout.fragment_maps)


         // Obtain the SupportMapFragment and get notified when the map is ready to be used.
         val mapFragment = childFragmentManager
             .findFragmentById(R.id.map) as SupportMapFragment
         mapFragment.getMapAsync(this)
     }

     */

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        map = mMap

        val start = LatLng(47.0,-121.0)
        mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(start, 14.5f))

    }

    fun newLocation(location: Location){
        userLocation.latitude = location.latitude
        userLocation.longitude = location.longitude
        val start = LatLng(location.latitude, location.longitude)
        if(map != null) {
            mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(start, 14.5f))
        }
    }

    fun changeLocation(lat: Double, long: Double, name: String){
        var destination: Location = Location("")
        destination.latitude = lat
        destination.longitude = long
        if(userLocation.distanceTo(destination) <= 400){
            Toast.makeText(activity!!.applicationContext,"ALREADY AT DESTINATION", Toast.LENGTH_SHORT).show()
            return
        }

        mMap.clear()

        val latLngOrigin = LatLng(userLocation.latitude,userLocation.longitude)
        var latLngDestination = LatLng(lat,long)

        mMap!!.addMarker(MarkerOptions().position(latLngOrigin).title("Home"))
        mMap!!.addMarker(MarkerOptions().position(latLngDestination).title(name))
        mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngOrigin, 14.5f))

        val path: MutableList<List<LatLng>> = ArrayList()
        val urlDirections = "https://maps.googleapis.com/maps/api/directions/json?origin=${latLngOrigin.latitude},${latLngOrigin.longitude}&destination=${latLngDestination.latitude},${latLngDestination.longitude}&key=AIzaSyCl0_5pIZ5g3KiUdvYn7mbGtPsg50lSdVQ"
        val directionsRequest = object : StringRequest(Request.Method.GET, urlDirections, Response.Listener<String> {
                response ->
            val jsonResponse = JSONObject(response)
            // Get routes
            val routes = jsonResponse.getJSONArray("routes")
            val legs = routes.getJSONObject(0).getJSONArray("legs")
            val steps = legs.getJSONObject(0).getJSONArray("steps")
            for (i in 0 until steps.length()) {
                val points = steps.getJSONObject(i).getJSONObject("polyline").getString("points")
                path.add(PolyUtil.decode(points))
            }
            for (i in 0 until path.size) {
                mMap.addPolyline(PolylineOptions().addAll(path[i]).color(Color.RED))
            }
        }, Response.ErrorListener {
        }){}

        val requestQueue = newRequestQueue(activity!!.applicationContext)
        requestQueue.add(directionsRequest)
    }

}
