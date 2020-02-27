package com.example.capaproject

import android.Manifest
import android.content.ComponentName
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.collections.HashMap
import kotlin.concurrent.fixedRateTimer
import android.app.Activity
import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.view.*
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Looper
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import java.lang.Exception
import android.content.res.Resources
import android.util.Log
import androidx.appcompat.app.AlertDialog
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import java.io.*
import java.util.ArrayList
import com.fasterxml.jackson.module.kotlin.*

class MainActivity : AppCompatActivity() {

    //laction functional vaiables
    lateinit var mLastLocation: Location
    private lateinit var mLocationRequest: LocationRequest
    private val INTERVAL: Long = 2000
    private val FASTEST_INTERVAL: Long = 1000
    private var mFusedLocationProviderClient: FusedLocationProviderClient? = null
    lateinit var userProfile : UserProfile

    //
    private var currentWidgetList = mutableListOf<widgetHolder>()
    private lateinit var mAppWidgetManager: AppWidgetManager
    private lateinit var mAppWidgetHost: WidgetHost
    private val APPWIDGET_HOST_ID = 1
    private val REQUEST_PICK_APPWIDGET = 2
    private val REQUEST_CREATE_APPWIDGET = 3
    private val REQUEST_APPWIDGET_CLOCK = 4
    private val REQUEST_APPWIDGET_MUSIC = 5
    private val REQUEST_APPWIDGET_SEARCH = 6
    private val REQUEST_APPWIDGET_EMAIL = 7
    private val REQUEST_APPWIDGET_CALENDAR = 8
    private val REQUEST_APPWIDGET_NOTES = 9
    private val REQUEST_APPWIDGET_WEATHER = 10

    lateinit var infos : List<AppWidgetProviderInfo>

    private lateinit var mainlayout: ViewGroup

    //helper object to determine user state
    lateinit var stateHelper: stateManager
    private lateinit var guiHelper : CAPAstate

    private lateinit var prefs : UserPrefApps
    private var awpiToChange : widgetHolder? = null

    private lateinit var databaseHandler : DatabaseHandler

    //used for testing serialize objects
    //private lateinit var mapper : ObjectMapper


    //currentActivity is current most probable activity
    //currentState is updated when state changes to ensure that we won't enter the same state twice
companion object{
    var currentActivity : String = "None"
    var currentState : String = "None"
}
    //private val databaseHandler = DatabaseHandler(this)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mainlayout = findViewById(R.id.mainLayout)

        //make sure mLastLocation is not null
        val dummyLocation = Location("")
        dummyLocation.latitude = 0.0
        dummyLocation.longitude = 0.0
        mLastLocation = dummyLocation

        //database variables
        databaseHandler = DatabaseHandler(this)

        userProfile = databaseHandler.getSurvey()

        //NUKE THE DATABASE!!!!!
        //databaseHandler.deleteData()

        //widget resources
        mAppWidgetManager = AppWidgetManager.getInstance(this)
        mAppWidgetHost = WidgetHost(this, APPWIDGET_HOST_ID)
        infos = mAppWidgetManager.installedProviders

/*
        mapper = jacksonObjectMapper()
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        mapper.addMixIn(ComponentName::class.java,CNmixin::class.java)
*/

        //Log.d("hostView",hostViewReloaded.awpi.provider.packageName)
        prefs = UserPrefApps()

        //Load preferences from database here
            //prefs = databaseHandler.getUserPrefs()

        //If user has never set prefs, ask for default widgets
        /*
        if(prefs.isEmpty())
            setDefaultProviders()
        */

        //starts location updates
        mLocationRequest = LocationRequest()

        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (checkPermissionForLocation(this)) {

            //comment following line out for use on emulator
            //startLocationUpdates()
        }
        stateHelper = stateManager(this)
        guiHelper = CAPAstate(this, databaseHandler, prefs)

        updateContext()

        guiHelper.updateUserState(resources.getString(R.string.stateDefault))
        currentState = resources.getString(R.string.stateDefault)
    }

    //Build the GUI given a hashmap. Called from CAPAstate.setState
    fun buildGUI(frags : HashMap<widgetHolder, Double>){
        removeAllWidgets()
        val sorted = frags.toList().sortedBy { (_, value) -> value}.toMap()
        for (entry in sorted) {
            //Log.d("Trying to build: ",entry.key.className)
            if(entry.key != null)
                createDefaultWidget(entry.key)
            //createFragment(entry.key,getAppropriateHeight(entry.key),indexOfTop)
        }
    }

    //for changing individual default widgets
    private fun helperQueryUserPrefWidget(widgetType : String){

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Please select your preferred $widgetType widget from the following list: ")
        builder.setPositiveButton("OK") { _, _ ->
            val appWidgetId = this.mAppWidgetHost.allocateAppWidgetId()
            val pickIntent = Intent(AppWidgetManager.ACTION_APPWIDGET_PICK)
            pickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            when(widgetType) {
                in "Clock" -> startActivityForResult(pickIntent, REQUEST_APPWIDGET_CLOCK)
                "Music" -> startActivityForResult(pickIntent, REQUEST_APPWIDGET_MUSIC)
                "Search" -> startActivityForResult(pickIntent, REQUEST_APPWIDGET_SEARCH)
                "Email" -> startActivityForResult(pickIntent, REQUEST_APPWIDGET_EMAIL)
                "Calendar" -> startActivityForResult(pickIntent, REQUEST_APPWIDGET_CALENDAR)
                "Notes" -> startActivityForResult(pickIntent, REQUEST_APPWIDGET_NOTES)
                "Weather" -> startActivityForResult(pickIntent, REQUEST_APPWIDGET_WEATHER)

            }
        }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
        }
        builder.create()
        builder.show()

    }


    //Search to set defaults if none exist
    private fun setDefaultProviders(){
        /*
        val clockArray = arrayOf(
            ComponentName("com.sec.android.app.clockpackage", "com.sec.android.widgetapp.analogclock.AnalogClockWidgetProvider"),
            ComponentName("com.sec.android.app.clockpackage", "com.sec.android.widgetapp.digitalclock.DigitalClockWidgetProvider"),
            ComponentName("com.google.android.deskclock", "com.android.alarmclock.AnalogAppWidgetProvider"),
            ComponentName("com.google.android.deskclock", "com.android.alarmclock.DigitalAppWidgetProvider"),
            ComponentName("com.oneplus.deskclock", "com.oneplus.alarmclock.DigitalAppWidgetProvider"),
            ComponentName("com.oneplus.deskclock", "com.oneplus.alarmclock.AnalogAppWidgetProvider"))

        val musicArray = arrayOf(
            ComponentName("com.google.android.music", "com.android.music.MediaAppWidgetProvider"),
            ComponentName("com.spotify.music", "com.spotify.music.features.widget.SpotifyWidget"))

        val searchArray = arrayOf(
            ComponentName("com.google.android.googlequicksearchbox", "com.google.android.googlequicksearchbox.SearchWidgetProvider"),
            ComponentName("com.android.chrome", "org.chromium.chrome.browser.searchwidget.SearchWidgetProvider"),
            ComponentName("com.microsoft.launcher", "com.microsoft.bingsearchsdk.api.ui.widgets.SearchWidgetProvider"))

        val emailArray = arrayOf(
            ComponentName("com.samsung.android.email.provider", "com.samsung.android.email.widget.WidgetProvider"),
            ComponentName("com.google.android.gm", "com.google.android.gm.widget.GmailWidgetProvider"),
            ComponentName("com.microsoft.office.outlook", "com.acompli.acompli.InboxWidgetProvider"))

        val calendarArray = arrayOf(
            ComponentName("com.samsung.android.calendar", "com.android.calendar.widget.list.ListWidgetProvider"),
            ComponentName("com.google.android.calendar", "com.android.calendar.widget.CalendarAppWidgetProvider"))

        val notesArray = arrayOf(
            ComponentName("com.samsung.android.app.notes", "com.samsung.android.app.notes.widget.WidgetProvider"),
            ComponentName("com.microsoft.office.onenote", "com.microsoft.office.onenote.ui.widget.ONMTextNoteWidgetReceiver"))

        val weatherArray = arrayOf(
            ComponentName("com.sec.android.daemonapp","com.sec.android.daemonapp.appwidget.WeatherAppWidget"),
            ComponentName("net.oneplus.weather", "net.oneplus.weather.widget.widget.WeatherWidgetProvider"))

        for (info in infos) {
            for(element in clockArray) {
                if (info.provider.className == element.className && info.provider.packageName == element.packageName) {
                    //we found one
                    prefs.clock = info
                    break
                }
            }
            for(element in musicArray){
                if (info.provider.className == element.className && info.provider.packageName == element.packageName) {
                    //we found one
                    prefs.music = info
                    break
                }
            }
            for(element in searchArray){
                if (info.provider.className == element.className && info.provider.packageName == element.packageName) {
                    //we found one
                    prefs.search = info
                    break
                }
            }
            for(element in emailArray){
                if (info.provider.className == element.className && info.provider.packageName == element.packageName) {
                    //we found one
                    prefs.email = info
                    break
                }
            }
            for(element in calendarArray){
                if (info.provider.className == element.className && info.provider.packageName == element.packageName) {
                    //we found one
                    prefs.calendar = info
                    break
                }
            }
            for(element in notesArray){
                if (info.provider.className == element.className && info.provider.packageName == element.packageName) {
                    //we found one
                    prefs.notes = info
                    break
                }
            }
            for(element in weatherArray){
                if (info.provider.className == element.className && info.provider.packageName == element.packageName) {
                    //we found one
                    prefs.weather = info
                    break
                }
            }
        }


*/

    }

    //updates textbox context every 1000 milliseconds
    //placeholder function to be used for testing
    private fun updateContext(){
        fixedRateTimer("timer",false,0,1000){
            this@MainActivity.runOnUiThread {
                text.text = stateHelper.getString()


                /*
                //If context has changed, updateuserstate
                if(stateHelper.getContext() == resources.getString(R.string.stateDriving) && currentState != resources.getString(R.string.stateDriving)) {
                    guiHelper.updateUserState(resources.getString(R.string.stateDriving))
                    currentState = resources.getString(R.string.stateDriving)
                }
                else if(stateHelper.getContext() == resources.getString(R.string.stateSchool) && currentState != resources.getString(R.string.stateSchool)) {
                    guiHelper.updateUserState(resources.getString(R.string.stateSchool))
                    currentState = resources.getString(R.string.stateSchool)
                }
                else if(stateHelper.getContext() == resources.getString(R.string.stateWork) && currentState != resources.getString(R.string.stateWork)) {
                    guiHelper.updateUserState(resources.getString(R.string.stateWork))
                    currentState = resources.getString(R.string.stateWork)
                }
                else if(stateHelper.getContext() == resources.getString(R.string.stateHome) && currentState != resources.getString(R.string.stateHome)) {
                    guiHelper.updateUserState(resources.getString(R.string.stateHome))
                    currentState = resources.getString(R.string.stateHome)
                }
                else if(stateHelper.getContext() == resources.getString(R.string.stateDefault) && currentState != resources.getString(R.string.stateDefault)) {
                    guiHelper.updateUserState(resources.getString(R.string.stateDefault))
                    currentState = resources.getString(R.string.stateDefault)
                }

                */
            }
        }
    }

    private fun removeAllWidgets() {
        var childCount = mainlayout.childCount
        while (childCount > 0) {
            val view = mainlayout.getChildAt(childCount - 1)
            if (view is AppWidgetHostView) {
                removeWidget(view)
            }
            childCount--
        }
    }
    private fun createDefaultWidget(awpi : widgetHolder) {
        val appWidgetId = mAppWidgetHost.allocateAppWidgetId()
        val hostView = mAppWidgetHost.createView(
            this.applicationContext,
            awpi.id, awpi.awpi
        )
        hostView.setAppWidget(appWidgetId, awpi.awpi)
        hostView.setOnLongClickListener {
            Log.d("TAG", "long click createWidget")
            guiHelper.removeWidget(awpi)
//            removeWidget(hostView)
            true
        }


        mainlayout.addView(hostView)
    }
    private fun createWidget(data: Intent) {
        val extras = data.extras
        val appWidgetId = extras!!.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)
        val appWidgetInfo = mAppWidgetManager.getAppWidgetInfo(appWidgetId)

        val h = widgetHolder(appWidgetInfo,appWidgetId)
        guiHelper.addWidget(h)
    }

    //logic to add a new widget to current state using floating action button
    fun clickAdd(view:View){
        selectWidget()
    }
    private fun selectWidget() {
        val appWidgetId = this.mAppWidgetHost.allocateAppWidgetId()
        val pickIntent = Intent(AppWidgetManager.ACTION_APPWIDGET_PICK)
        pickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        startActivityForResult(pickIntent, REQUEST_PICK_APPWIDGET)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_PICK_APPWIDGET -> configureWidget(data!!)
                REQUEST_CREATE_APPWIDGET -> createWidget(data!!)
                REQUEST_APPWIDGET_MUSIC -> {
                    prefs.music = widgetPrefHelper(data!!)
                    if(guiHelper.stateMap.contains(awpiToChange)) {
                        guiHelper.stateMap.remove(awpiToChange)
                        //guiHelper.addWidget(prefs.music)
                    }
                }
                REQUEST_APPWIDGET_CLOCK -> {
                    prefs.clock = widgetPrefHelper(data!!)
                    if(guiHelper.stateMap.contains(awpiToChange)) {
                        guiHelper.stateMap.remove(awpiToChange)
                        //guiHelper.addWidget(prefs.clock)
                    }
                }
                REQUEST_APPWIDGET_SEARCH -> {
                    prefs.search = widgetPrefHelper(data!!)
                    if(guiHelper.stateMap.contains(awpiToChange)) {
                        guiHelper.stateMap.remove(awpiToChange)
                        //guiHelper.addWidget(prefs.search)
                    }
                }
                REQUEST_APPWIDGET_EMAIL -> {
                    prefs.email = widgetPrefHelper(data!!)
                    if(guiHelper.stateMap.contains(awpiToChange)) {
                        guiHelper.stateMap.remove(awpiToChange)
                        //guiHelper.addWidget(prefs.email)
                    }
                }
                REQUEST_APPWIDGET_CALENDAR -> {
                    prefs.calendar = widgetPrefHelper(data!!)
                    if(guiHelper.stateMap.contains(awpiToChange)) {
                        guiHelper.stateMap.remove(awpiToChange)
                        //guiHelper.addWidget(prefs.calendar)
                    }
                }
                REQUEST_APPWIDGET_NOTES -> {
                    prefs.notes = widgetPrefHelper(data!!)
                    if(guiHelper.stateMap.contains(awpiToChange)) {
                        guiHelper.stateMap.remove(awpiToChange)
                        //guiHelper.addWidget(prefs.notes)
                    }
                }
                REQUEST_APPWIDGET_WEATHER -> {
                    prefs.weather = widgetPrefHelper(data!!)
                    if(guiHelper.stateMap.contains(awpiToChange)) {
                        guiHelper.stateMap.remove(awpiToChange)
                        //guiHelper.addWidget(prefs.weather)
                    }
                }


            }
        } else if (resultCode == Activity.RESULT_CANCELED && data != null) {
            val appWidgetId = data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)
            if (appWidgetId != -1) {
                mAppWidgetHost.deleteAppWidgetId(appWidgetId)
            }
        }
    }
    private fun widgetPrefHelper(data: Intent) : widgetHolder{
        val extras = data.extras
        val appWidgetId = extras!!.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)
        return widgetHolder(mAppWidgetManager.getAppWidgetInfo(appWidgetId),appWidgetId)
    }
    private fun configureWidget(data: Intent) {
        val extras = data.extras
        val appWidgetId = extras!!.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)
        val appWidgetInfo = mAppWidgetManager.getAppWidgetInfo(appWidgetId)
        if (appWidgetInfo.configure != null) {
            val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE)
            intent.component = appWidgetInfo.configure
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            startActivityForResult(intent, REQUEST_CREATE_APPWIDGET)
        } else {
            createWidget(data)
        }
    }

    override fun onResume(){
        super.onResume()
        userProfile = databaseHandler.getSurvey()
    }
    override fun onPause(){
        super.onPause()

        //save current UI for current state to database
        if(::guiHelper.isInitialized)
            databaseHandler.updateDatabaseState(guiHelper.getState(),guiHelper.getList())

        //Save user pref apps to database here
        databaseHandler.updateUserPrefs(prefs)
    }
    override fun onStart() {
        super.onStart()
        mAppWidgetHost.startListening()
        userProfile = databaseHandler.getSurvey()
    }

    override fun onStop() {
        super.onStop()
        mAppWidgetHost.stopListening()
    }

    override fun onDestroy() {
        databaseHandler.close()
        super.onDestroy()
    }

    private fun removeWidget(hostView: AppWidgetHostView) {
        //println(hostView.appWidgetId)
        mAppWidgetHost.deleteAppWidgetId(hostView.appWidgetId)
        mainlayout.removeView(hostView)
    }
    internal fun addEmptyData(pickIntent: Intent) {
        val customInfo = ArrayList<AppWidgetProviderInfo>()
        pickIntent.putParcelableArrayListExtra(AppWidgetManager.EXTRA_CUSTOM_INFO, customInfo)
        val customExtras = ArrayList<Bundle>()
        pickIntent.putParcelableArrayListExtra(AppWidgetManager.EXTRA_CUSTOM_EXTRAS, customExtras)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == R.id.action_setting) {

            val surveyOne = Survey(userProfile,this)

            val intent = Intent(this, surveyOne.javaClass)
            startActivity(intent)
            Toast.makeText(this, "User Survey", Toast.LENGTH_SHORT).show()

        }
        else if(id == R.id.prefApps){
            //display list of widgets to user
            val res: Resources = resources
            val widgetList = res.getStringArray(R.array.Widgets)
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Select widget to change default:")
                .setItems(widgetList) { dialog, which ->
                    //remove old widget from stateMap
                    awpiToChange = prefs.getAttr(widgetList[which])
                    helperQueryUserPrefWidget(widgetList[which])
                    dialog.dismiss()
                }
                builder.setNegativeButton("Cancel") { dialog, _ ->
                    dialog.cancel()
                }
                builder.create()
                builder.show()
        }
        else if(id == R.id.setWork){
            Toast.makeText(this, "State Changed to Work", Toast.LENGTH_LONG).show()
            currentState = resources.getString(R.string.stateWork)
            guiHelper.updateUserState(resources.getString(R.string.stateWork))
        }
        else if(id == R.id.setDefault){
            Toast.makeText(this, "State Changed to Default", Toast.LENGTH_LONG).show()
            currentState = resources.getString(R.string.stateDefault)
            guiHelper.updateUserState(resources.getString(R.string.stateDefault))
        }

        return super.onOptionsItemSelected(item)
    }

    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {

            locationResult.lastLocation
            onLocationChanged(locationResult.lastLocation)
        }
    }

    //when location is changed
    fun onLocationChanged(location: Location){
        //new location has now been determined
        mLastLocation = location

        //userProfile = databaseHandler.getSurvey()
        //checking if you are close to one of you survey addresses

        //checking school address
        val school : Location?
        val work : Location?
        val home : Location?
        var sDistance : Float = (-1).toFloat()
        var wDistance : Float = (-1).toFloat()
        var hDistance : Float = (-1).toFloat()

        try {
            school = getLocationFromAddress(this, userProfile.getField("School"))
            sDistance  = mLastLocation.distanceTo(school)
        }catch (e: Exception){
            //val geocoder = Geocoder(this, Locale.getDefault())
            //locLabel.text = "" + geocoder.getFromLocation(mLastLocation.latitude, mLastLocation.longitude, 1)[0].getAddressLine(0)
        }
        try {
            work = getLocationFromAddress(this, userProfile.getField("Work"))
            wDistance  = mLastLocation.distanceTo(work)
        }catch (e: Exception){
            //val geocoder = Geocoder(this, Locale.getDefault())
            //locLabel.text = "" + geocoder.getFromLocation(mLastLocation.latitude, mLastLocation.longitude, 1)[0].getAddressLine(0)
        }
        try {
            home = getLocationFromAddress(this, userProfile.getField("Home"))
            hDistance  = mLastLocation.distanceTo(home)
        }catch (e: Exception){
            //val geocoder = Geocoder(this, Locale.getDefault())
            //locLabel.text = "" + geocoder.getFromLocation(mLastLocation.latitude, mLastLocation.longitude, 1)[0].getAddressLine(0)
        }

        when {
            sDistance < 400 && sDistance >= 0 -> {
                stateHelper.location = resources.getString(R.string.stateSchool)
            }
            wDistance < 400 && wDistance >= 0 -> {
                stateHelper.location = resources.getString(R.string.stateWork)
            }
            hDistance < 400 && hDistance >= 0 -> {
                stateHelper.location = resources.getString(R.string.stateHome)
            }
            else -> {
                stateHelper.location = "None"
            }
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

    protected fun startLocationUpdates(){

        //create the location request to start receiving updates
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = INTERVAL
        mLocationRequest.fastestInterval = FASTEST_INTERVAL

        //create locationsettingrequest object using location request
        val builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(mLocationRequest!!)
        val locationSettingsRequest = builder.build()

        val settingsClient = LocationServices.getSettingsClient(this)
        settingsClient.checkLocationSettings(locationSettingsRequest)

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            return
        }
        mFusedLocationProviderClient!!.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper())

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == 10) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates()
            } else {
                Toast.makeText(this@MainActivity, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun checkPermissionForLocation(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
                true
            } else {
                // Show the permission request
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    10)
                false
            }
        } else {
            true
        }
    }
}
