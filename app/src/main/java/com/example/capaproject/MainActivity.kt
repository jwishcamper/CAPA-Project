package com.example.capaproject

import android.Manifest
import android.content.ComponentName
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlin.collections.HashMap
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
import androidx.appcompat.app.AlertDialog
import com.fasterxml.jackson.databind.DeserializationFeature
import java.io.*
import java.util.ArrayList
import com.fasterxml.jackson.module.kotlin.*



//currently unused from fragment logic
/*
//space between fragments
const val paddingHeight = 35

//If there is an XML element to be at top of screen, increment this
const val indexOfTop=1

//used to keep track of created view IDs and fragments
var viewIDs = mutableListOf<Int>()
var fragments = mutableListOf<Fragment>()
*/

class MainActivity : AppCompatActivity() {

    //laction functional vaiables
    lateinit var mLastLocation: Location
    private lateinit var mLocationRequest: LocationRequest
    private val INTERVAL: Long = 2000
    private val FASTEST_INTERVAL: Long = 1000
    private var mFusedLocationProviderClient: FusedLocationProviderClient? = null
    lateinit var userProfile : UserProfile

    var schoolDialog = true
    var workDialog = true
    var homeDialog = true

    //
    private var currentWidgetList = mutableListOf<AppWidgetProviderInfo>()
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
    private var cnToChange = ComponentName("","")

    private lateinit var databaseHandler : DatabaseHandler

    //currently unused from fragment logic
    /*
    private var screenHeight : Int = 0
    private val listOfWidgets : ArrayList<String> = ArrayList(listOf("testingFragment", "alarmDisplay", "mediaPlayer"))
    private var currentWidgets : ArrayList<String> = ArrayList()
*/

    //currentActivity is current most probable activity
companion object{
    var currentActivity : String = "None"
}
    //private val databaseHandler = DatabaseHandler(this)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        mainlayout = findViewById(R.id.mainLayout)

        //database variables
        databaseHandler = DatabaseHandler(this)

        userProfile = databaseHandler.getSurvey()
            //text.text=userProfile.getField("BirthDay")

        //NUKE THE DATABASE!!!!!
        //databaseHandler.deleteInfo()

        //NUKE USER HISTORY TABLE!!!!!
        databaseHandler.clearUserHistory()

        //widget resources
        mAppWidgetManager = AppWidgetManager.getInstance(this)
        mAppWidgetHost = WidgetHost(this, APPWIDGET_HOST_ID)
        infos = mAppWidgetManager.installedProviders

        //screenHeight = getScreenHeight()

        prefs = UserPrefApps()
        //Load preferences from database here
        prefs = databaseHandler.getUserPrefs()

        //If user has never set prefs, ask for default widgets
        if(prefs.isEmpty())
            setDefaultProviders()

        /*

        Log.d("prefs:",prefs.clock.className)
        Log.d("prefs:",prefs.music.className)
        Log.d("prefs:",prefs.search.className)
        Log.d("prefs:",prefs.email.className)
        Log.d("prefs:",prefs.calendar.className)
        Log.d("prefs:",prefs.notes.className)
        Log.d("prefs:",prefs.weather.className)
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
        guiHelper.updateUserState("atWork")

        updateContext()







        val mapper = jacksonObjectMapper()
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        mapper.addMixIn(ComponentName::class.java,CNmixin::class.java)

        val prefString = mapper.writeValueAsString(prefs)

        /*
        val jsonString = mapper.writeValueAsString(infos[0])
        Log.d("TAG",jsonString)
        val prefsReloaded : AppWidgetProviderInfo = mapper.readValue(jsonString)
        Log.d("TAG","pkg: ${prefsReloaded.provider.packageName}, cls: ${prefsReloaded.provider.className}, shortclass: ${prefsReloaded.provider.shortClassName}")
        */

        val prefsReloaded : UserPrefApps = mapper.readValue(prefString)
        Log.d("TAG",prefsReloaded.clock.packageName)
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
                    prefs.clock = ComponentName(
                        info.provider.packageName,
                        info.provider.className
                    )
                    break
                }
            }
            for(element in musicArray){
                if (info.provider.className == element.className && info.provider.packageName == element.packageName) {
                    //we found one
                    prefs.music = ComponentName(
                        info.provider.packageName,
                        info.provider.className
                    )
                    break
                }
            }
            for(element in searchArray){
                if (info.provider.className == element.className && info.provider.packageName == element.packageName) {
                    //we found one
                    prefs.search = ComponentName(
                        info.provider.packageName,
                        info.provider.className
                    )
                    break
                }
            }
            for(element in emailArray){
                if (info.provider.className == element.className && info.provider.packageName == element.packageName) {
                    //we found one
                    prefs.email = ComponentName(
                        info.provider.packageName,
                        info.provider.className
                    )
                    break
                }
            }
            for(element in calendarArray){
                if (info.provider.className == element.className && info.provider.packageName == element.packageName) {
                    //we found one
                    prefs.calendar = ComponentName(
                        info.provider.packageName,
                        info.provider.className
                    )
                    break
                }
            }
            for(element in notesArray){
                if (info.provider.className == element.className && info.provider.packageName == element.packageName) {
                    //we found one
                    prefs.notes = ComponentName(
                        info.provider.packageName,
                        info.provider.className
                    )
                    break
                }
            }
            for(element in weatherArray){
                if (info.provider.className == element.className && info.provider.packageName == element.packageName) {
                    //we found one
                    prefs.weather = ComponentName(
                        info.provider.packageName,
                        info.provider.className
                    )
                    break
                }
            }
        }




    }

    //Build the GUI given a hashmap. Called from CAPAstate.setState
    fun buildGUI(frags : HashMap<ComponentName, Double>){
        removeAllWidgets()
        val sorted = frags.toList().sortedBy { (_, value) -> value}.toMap()
        for (entry in sorted) {
            //Log.d("Trying to build: ",entry.key.className)
            createDefaultWidget(entry.key)
            //createFragment(entry.key,getAppropriateHeight(entry.key),indexOfTop)
        }
    }

    //updates textbox context every 1000 milliseconds
    //placeholder function to be used for testing
    /*private fun updateContext(){
        fixedRateTimer("timer",false,0,1000){
            this@MainActivity.runOnUiThread {
                text.text = stateHelper.getContext()

                //text.text=userProfile.getField("BirthDay")
                //placeholder for testing state changes
                /*
                if(currentActivity == "Still"){
                    guiHelper.updateUserState("default")
                }
                else if(currentActivity!="Still") {
                    guiHelper.updateUserState("atWork")
                }*/
                //Log.d("PrefClock: ",prefs.clock.className)
                //Log.d("PrefMusic: ",prefs.music.className)
                //guiHelper.refresh()
            }
        }
    }*/

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
    private fun createDefaultWidget(cn : ComponentName) {

        var appWidgetInfo: AppWidgetProviderInfo? = null

        for (info in infos) {
            if (info.provider.className == cn.className && info.provider.packageName == cn.packageName) {
                //we found it
                appWidgetInfo = info
                break
            }
        }
        val appWidgetId = mAppWidgetHost.allocateAppWidgetId()
        val hostView = mAppWidgetHost.createView(
            this.applicationContext,
            appWidgetId, appWidgetInfo
        )
        hostView.setAppWidget(appWidgetId, appWidgetInfo)
        hostView.setOnLongClickListener {
            Log.d("TAG", "long click createWidget")
            guiHelper.removeWidget(cn)
//            removeWidget(hostView)
            true
        }
        mainlayout.addView(hostView)
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
                    if(guiHelper.stateMap.contains(cnToChange)) {
                        guiHelper.stateMap.remove(cnToChange)
                        guiHelper.addWidget(prefs.music)
                    }
                }
                REQUEST_APPWIDGET_CLOCK -> {
                    prefs.clock = widgetPrefHelper(data!!)
                    if(guiHelper.stateMap.contains(cnToChange)) {
                        guiHelper.stateMap.remove(cnToChange)
                        guiHelper.addWidget(prefs.clock)
                    }
                }
                REQUEST_APPWIDGET_SEARCH -> {
                    prefs.search = widgetPrefHelper(data!!)
                    if(guiHelper.stateMap.contains(cnToChange)) {
                        guiHelper.stateMap.remove(cnToChange)
                        guiHelper.addWidget(prefs.search)
                    }
                }
                REQUEST_APPWIDGET_EMAIL -> {
                    prefs.email = widgetPrefHelper(data!!)
                    if(guiHelper.stateMap.contains(cnToChange)) {
                        guiHelper.stateMap.remove(cnToChange)
                        guiHelper.addWidget(prefs.email)
                    }
                }
                REQUEST_APPWIDGET_CALENDAR -> {
                    prefs.calendar = widgetPrefHelper(data!!)
                    if(guiHelper.stateMap.contains(cnToChange)) {
                        guiHelper.stateMap.remove(cnToChange)
                        guiHelper.addWidget(prefs.calendar)
                    }
                }
                REQUEST_APPWIDGET_NOTES -> {
                    prefs.notes = widgetPrefHelper(data!!)
                    if(guiHelper.stateMap.contains(cnToChange)) {
                        guiHelper.stateMap.remove(cnToChange)
                        guiHelper.addWidget(prefs.notes)
                    }
                }
                REQUEST_APPWIDGET_WEATHER -> {
                    prefs.weather = widgetPrefHelper(data!!)
                    if(guiHelper.stateMap.contains(cnToChange)) {
                        guiHelper.stateMap.remove(cnToChange)
                        guiHelper.addWidget(prefs.weather)
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
    private fun widgetPrefHelper(data: Intent) : ComponentName{
        val extras = data.extras
        val appWidgetId = extras!!.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)
        val appWidgetInfo = mAppWidgetManager.getAppWidgetInfo(appWidgetId)
        return ComponentName(
            appWidgetInfo.provider.packageName,
            appWidgetInfo.provider.className
        )
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
    private fun createWidget(data: Intent) {
        val extras = data.extras
        val appWidgetId = extras!!.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)
        val appWidgetInfo = mAppWidgetManager.getAppWidgetInfo(appWidgetId)

        val hostView = mAppWidgetHost.createView(
            this.applicationContext,
            appWidgetId, appWidgetInfo
        )



        val cn = ComponentName(
            appWidgetInfo.provider.packageName,
            appWidgetInfo.provider.className
        )
        guiHelper.addWidget(cn)
        //Log.d("TAG",appWidgetInfo.provider.packageName)
        //Log.d("TAG",appWidgetInfo.provider.className)

//        hostView.setAppWidget(appWidgetId, appWidgetInfo)
//        hostView.setOnLongClickListener {
//            Log.d("TAG", "long click createWidget")
////            removeWidget(hostView)
//            guiHelper.removeWidget(cn)
//            true
//        }
//        mainlayout.addView(hostView)
//
//        currentWidgetList.add(appWidgetInfo)
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
                    cnToChange = prefs.getAttr(widgetList[which])
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
            guiHelper.updateUserState("atWork")
        }
        else if(id == R.id.setDefault){
            Toast.makeText(this, "State Changed to Default", Toast.LENGTH_LONG).show()
            guiHelper.updateUserState("default")
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
            if(schoolDialog && userProfile.getField("School")!="None") {
                schoolDialog = false
            }
            //val geocoder = Geocoder(this, Locale.getDefault())
            //locLabel.text = "" + geocoder.getFromLocation(mLastLocation.latitude, mLastLocation.longitude, 1)[0].getAddressLine(0)
        }
        try {
            work = getLocationFromAddress(this, userProfile.getField("Work"))
            wDistance  = mLastLocation.distanceTo(work)
        }catch (e: Exception){
            if(workDialog && userProfile.getField("Work")!="None") {
                workDialog = false
            }
            //val geocoder = Geocoder(this, Locale.getDefault())
            //locLabel.text = "" + geocoder.getFromLocation(mLastLocation.latitude, mLastLocation.longitude, 1)[0].getAddressLine(0)
        }
        try {
            home = getLocationFromAddress(this, userProfile.getField("Home"))
            hDistance  = mLastLocation.distanceTo(home)
        }catch (e: Exception){
            if(homeDialog && userProfile.getField("Home")!="None") {
                homeDialog = false
            }
            //val geocoder = Geocoder(this, Locale.getDefault())
            //locLabel.text = "" + geocoder.getFromLocation(mLastLocation.latitude, mLastLocation.longitude, 1)[0].getAddressLine(0)
        }

        when {
            sDistance < 400 && sDistance >= 0 -> {
                stateHelper.location = "School"
            }
            wDistance < 400 && wDistance >= 0 -> {
                stateHelper.location = "Work"
            }
            hDistance < 400 && hDistance >= 0 -> {
                stateHelper.location = "Home"
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


    //CURRENTLY UNUSED FRAGMENT LOGIC
/*
    private fun getAppropriateHeight(fragmentType : String) : Int{
        return when(fragmentType){
            in "alarmDisplay", "mediaPlayer" -> screenHeight/7
            else -> 1500
        }
    }
    private fun getScreenHeight() : Int{
        var display = windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        return size.y
    }

    private fun removeAllFragments(){
        for ( i in fragments){
            removeFragment(i)
        }
        fragments.clear()
        for (i in viewIDs){
            val currentFrame :View = findViewById(i)
            currentFrame.visibility = GONE
        }
        viewIDs.clear()
    }


    //creates a new frame and fragment in it of type fragmentType
    //if a new fragment at bottom is desired, pass nothing for index
    //if a new fragment at top is desired, pass indexOfTop for index
    private fun createFragment(fragmentType:String,height:Int=350,index:Int=-1){
        val newPadding = FrameLayout(this)
        newPadding.id = ViewCompat.generateViewId()
        val newFrag = FrameLayout(this)
        newFrag.id = ViewCompat.generateViewId()
        //add to bottom
        if(index==-1){
            viewIDs.add(newPadding.id)
            mainLayout.addView(newPadding)
            viewIDs.add(newFrag.id)
            mainLayout.addView(newFrag)
        }
        //add to index
        else{
            viewIDs.add(newFrag.id)
            mainLayout.addView(newFrag,index)
            viewIDs.add(newPadding.id)
            mainLayout.addView(newPadding,index)
        }
        newPadding.layoutParams.height = paddingHeight
        newPadding.layoutParams.width =  ActionBar.LayoutParams.MATCH_PARENT
        newFrag.layoutParams.height = height
        newFrag.layoutParams.width =  ActionBar.LayoutParams.MATCH_PARENT

        //initialize fragment of type fragmentType
        val fragToAdd : Fragment
        when(fragmentType) {
            "alarmDisplay" -> {
                fragToAdd = alarmDisplay()
                currentWidgets.add("alarmDisplay") }
            "mediaPlayer" -> {
                fragToAdd = mediaPlayer()
                currentWidgets.add("mediaPlayer") }
            else -> {
                fragToAdd = testingFragment()
                currentWidgets.add("testingFragment") }
        }

        //add fragment to created frame
        fragments.add(fragToAdd)
        addFragment(fragToAdd, newFrag.id)
    }

    //helper functions to add fragments more easily
    private inline fun FragmentManager.inTransaction(func: FragmentTransaction.() -> Unit) {
        val fragmentTransaction = beginTransaction()
        fragmentTransaction.func()
        fragmentTransaction.commit()
    }
    private fun AppCompatActivity.addFragment(fragment: Fragment, frameId: Int){
        supportFragmentManager.inTransaction { add(frameId, fragment) }
    }
    private fun AppCompatActivity.replaceFragment(fragment: Fragment, frameId: Int) {
        supportFragmentManager.inTransaction{replace(frameId, fragment)}
    }
    private fun AppCompatActivity.removeFragment(fragment: Fragment) {
        supportFragmentManager.inTransaction { remove(fragment) }
    }

    override fun onDestroy() {
        databaseHandler!!.close()
        super.onDestroy()
    }
        //logic to add a new widget to current state using floating action button
    fun clickAdd(view:View){


        //display list of widgets to user
        val availableWidgets = ArrayList<String>()
        for (temp in listOfWidgets){
            if(!currentWidgets.contains(temp)){
                availableWidgets.add(temp)
            }
        }
        val widArr = arrayOfNulls<String>(availableWidgets.size)
        availableWidgets.toArray(widArr)
        val builder = AlertDialog.Builder(view.context)
        if(widArr.isEmpty()){
            builder.setTitle("All available widgets already added to this state.")
            builder.setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }
            builder.create()
            builder.show()
        }
        else {
            builder.setTitle("Select widget to add")
                .setItems(widArr) { dialog, which ->
                    //upon user selection, add widget to bottom of gui
                    //send widget info to capastate to add to custom UI
                    guiHelper.addWidget(widArr[which]!!)
                    dialog.dismiss()
                }
            builder.setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }
            builder.create()
            builder.show()
        }


*/
}
