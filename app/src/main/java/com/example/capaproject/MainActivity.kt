package com.example.capaproject

import android.Manifest
import android.os.Bundle
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
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.activity_main.*
import java.io.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.*
import kotlin.concurrent.fixedRateTimer

class MainActivity : AppCompatActivity() {

    //Update this boolean variable depending on use for emulator or physical device
    val useEmulator = false

    //Update this boolean to "true" if you want state to change automatically with location
    //false is more useful for testing
    private val autoUpdateState = false

    //Update this to true to delete all database info
    private val nukeDB = false




    //location functional variables
    val drivingFragment : DrivingFragment = DrivingFragment()
    var drivingFlag = false
    lateinit var mLastLocation: Location
    private lateinit var mLocationRequest: LocationRequest
    private val INTERVAL: Long = 2000
    private val FASTEST_INTERVAL: Long = 1000
    private var mFusedLocationProviderClient: FusedLocationProviderClient? = null
    lateinit var userProfile : UserProfile


    //AppWidgetHost Variables
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
    private var widgetHolderToChange : widgetHolder? = null

    private lateinit var databaseHandler : DatabaseHandler
    private lateinit var userPattern : UserPatterns

    //Variables used to determine whether to show the QuickNav dialog or not
    private var showDialog = false
    private var waitTime = 0
    private var waitDate = 0

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
        //var tempTime = Calendar.getInstance().get(Calendar.HOUR_OF_DAY).toString()
        //tempTime += Calendar.getInstance().get(Calendar.MINUTE)
        //waitTime = tempTime.toInt()
        Log.d("test", waitTime.toString())
        Log.d("test", waitDate.toString())
        //make sure mLastLocation is not null
        val dummyLocation = Location("")
        dummyLocation.latitude = 0.0
        dummyLocation.longitude = 0.0
        mLastLocation = dummyLocation

        //database variables
        databaseHandler = DatabaseHandler(this)

        userProfile = databaseHandler.getSurvey()

        if(nukeDB) {
            //NUKE THE DATABASE!!!!!
            databaseHandler.clearUserData()

            //NUKE USER HISTORY TABLE!!!!!
            databaseHandler.clearUserHistory()

            //NUKE USER PATTERNS TABLE!!!!!
            databaseHandler.clearUserPatterns()
        }

        //User pattern
        userPattern = UserPatterns(databaseHandler,this)

        //widget resources
        mAppWidgetManager = AppWidgetManager.getInstance(this)
        mAppWidgetHost = WidgetHost(this, APPWIDGET_HOST_ID)
        infos = mAppWidgetManager.installedProviders

        prefs = UserPrefApps()

        //Load preferences from database
        prefs = databaseHandler.getUserPrefs()

        //If user has never set prefs, set them
        if(prefs.isEmpty())
            setDefaultProviders()

        //starts location updates
        mLocationRequest = LocationRequest()

        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        //Start location updates if relevant
        if (checkPermissionForLocation(this)) {
            if(!useEmulator)
                startLocationUpdates()
        }
        stateHelper = stateManager(this)
        guiHelper = CAPAstate(this, databaseHandler, prefs)
        text.text=""
        updateContext()

        guiHelper.updateUserState(resources.getString(R.string.stateDefault))
        currentState = resources.getString(R.string.stateDefault)


        dialogQuickNav(resources.getString(R.string.stateWork))

    }

    //Build the GUI given a hashmap. Called from CAPAstate.setState
    fun buildGUI(frags : HashMap<widgetHolder, Double>){
        removeAllWidgets()
        val sorted = frags.toList().sortedBy { (_, value) -> value}.toMap()
        for (entry in sorted) {
            createDefaultWidget(entry.key)
        }
    }

    //for changing individual default widgets
    fun helperQueryUserPrefWidget(widgetType : String){

        val appWidgetId = mAppWidgetHost.allocateAppWidgetId()
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

    //Search to set defaults if none exist
    private fun setDefaultProviders(){
        for(info in infos) {
            when {
                info.provider.className == "com.example.capaproject.WidgetMusic" -> prefs.music = widgetHolder(info, mAppWidgetHost.allocateAppWidgetId())
                info.provider.className == "com.example.capaproject.WidgetClock" -> prefs.clock = widgetHolder(info, mAppWidgetHost.allocateAppWidgetId())
                info.provider.className == "com.example.capaproject.WidgetCalendar" -> prefs.calendar = widgetHolder(info, mAppWidgetHost.allocateAppWidgetId())
                info.provider.className == "com.example.capaproject.WidgetWeather" -> prefs.weather = widgetHolder(info, mAppWidgetHost.allocateAppWidgetId())
                info.provider.className == "com.example.capaproject.WidgetNotes" -> prefs.notes = widgetHolder(info, mAppWidgetHost.allocateAppWidgetId())
                info.provider.className == "com.example.capaproject.WidgetEmail" -> prefs.email = widgetHolder(info, mAppWidgetHost.allocateAppWidgetId())
                info.provider.className == "com.example.capaproject.WidgetSearch" -> prefs.search = widgetHolder(info, mAppWidgetHost.allocateAppWidgetId())
            }
        }
    }

    //updates textbox context every 5000 milliseconds
    private fun updateContext(){
        fixedRateTimer("timer",false,0,5000){
            this@MainActivity.runOnUiThread {
                //text.text = stateHelper.getString()

                if(autoUpdateState) {
                    if (currentActivity == "In Vehicle" && !drivingFlag) {
                        activateDriving()
                    }

                    //If context has changed, updateuserstate
                    if(stateHelper.getContext() == resources.getString(R.string.stateDriving) && currentState != resources.getString(R.string.stateDriving)) {
                        guiHelper.updateUserState(resources.getString(R.string.stateDriving))
                        currentState = resources.getString(R.string.stateDriving)
                        this@MainActivity.runOnUiThread {
                            userPattern.checkForStatePattern(mLastLocation,userProfile)
                        }
                    }
                    else if(stateHelper.getContext() == resources.getString(R.string.stateSchool) && currentState != resources.getString(R.string.stateSchool)) {
                        guiHelper.updateUserState(resources.getString(R.string.stateSchool))
                        currentState = resources.getString(R.string.stateSchool)
                        this@MainActivity.runOnUiThread {
                            userPattern.checkForStatePattern(mLastLocation,userProfile)
                        }
                    }
                    else if(stateHelper.getContext() == resources.getString(R.string.stateWork) && currentState != resources.getString(R.string.stateWork)) {
                        guiHelper.updateUserState(resources.getString(R.string.stateWork))
                        currentState = resources.getString(R.string.stateWork)
                        this@MainActivity.runOnUiThread {
                            userPattern.checkForStatePattern(mLastLocation,userProfile)
                        }
                    }
                    else if(stateHelper.getContext() == resources.getString(R.string.stateHome) && currentState != resources.getString(R.string.stateHome)) {
                        guiHelper.updateUserState(resources.getString(R.string.stateHome))
                        currentState = resources.getString(R.string.stateHome)
                        this@MainActivity.runOnUiThread {
                            userPattern.checkForStatePattern(mLastLocation,userProfile)
                        }
                    }
                    else if(stateHelper.getContext() == resources.getString(R.string.stateDefault) && currentState != resources.getString(R.string.stateDefault)) {
                        guiHelper.updateUserState(resources.getString(R.string.stateDefault))
                        currentState = resources.getString(R.string.stateDefault)
                        this@MainActivity.runOnUiThread {
                            userPattern.checkForStatePattern(mLastLocation,userProfile)
                        }
                    }

                }
            }
        }
    }
    /*
    //Runs every 5 minutes
    private fun slowLoop(){
        fixedRateTimer("timer2",false,0,300000){
            this@MainActivity.runOnUiThread {
                userPattern.checkForStatePattern(mLastLocation,userProfile)

            }
        }
    }
    */

    fun dialogQuickNav(s :String){
        val builder = AlertDialog.Builder(this)
        val suppressed = databaseHandler.getUserPatterns()
        waitDate = suppressed[0]
        waitTime = suppressed[1]
        //Log.d("waitTime", waitTime.toString())
        //Log.d("waitDate", waitDate.toString())
        val currentDate = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        var tempCurrentTime = Calendar.getInstance().get(Calendar.HOUR_OF_DAY).toString()
        tempCurrentTime += Calendar.getInstance().get(Calendar.MINUTE)
        val currentTime = tempCurrentTime.toInt()
        if(currentDate > waitDate || (currentDate == waitDate && currentTime >= waitTime + 60)) {
            builder.setTitle("In order to get to ${s.dropLast(5)} on time, you need to leave within 10 minutes.")
                .setMessage("Would you like to start quick navigation now?")
                .setPositiveButton("Yes") { _, _ ->
                    activateDriving()
                    //Automatically click quick nav button here
                }
            showDialog = true
        }
        builder.setNegativeButton("No") { dialog, _ ->
            dialog.cancel()
            var tempWaitTime = Calendar.getInstance().get(Calendar.HOUR_OF_DAY).toString()
            tempWaitTime += Calendar.getInstance().get(Calendar.MINUTE)
            waitTime = tempWaitTime.toInt()
            waitDate = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
            databaseHandler.updateUserPatterns(waitDate, waitTime)
            showDialog = false
        }
        if(showDialog) {
            builder.create()
            builder.show()
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
        //val appWidgetId = mAppWidgetHost.allocateAppWidgetId()
        val hostView = mAppWidgetHost.createView(
            this.applicationContext,
            awpi.id, awpi.awpi
        )
        Log.d("App",awpi.awpi.provider.packageName)
        Log.d("App",awpi.awpi.provider.className)
        hostView.setAppWidget(awpi.id, awpi.awpi)

        for(entry in resources.getStringArray(R.array.Widgets)) {
            if(awpi.awpi.provider.className=="com.example.capaproject.Widget$entry"){
                hostView.setOnClickListener {
                    widgetHolderToChange = prefs.getAttr(entry)
                    helperQueryUserPrefWidget(entry)
                }
            }
        }
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

    //Helper function to test if the current state contains widgetHolderToChange's widget
    private fun compareToChange() : Boolean {
        for(entry in guiHelper.stateMap){
            if(entry.key.awpi.provider.className==widgetHolderToChange!!.awpi.provider.className){
                return true
            }
        }
        return false
    }

    //Called automatically when an activity request is received
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_PICK_APPWIDGET -> configureWidget(data!!)
                REQUEST_CREATE_APPWIDGET -> createWidget(data!!)
                REQUEST_APPWIDGET_MUSIC -> {
                    prefs.music = widgetPrefHelper(data!!)
                    databaseHandler.updatePrefsForAllStates(widgetHolderToChange!!,prefs.music!!)
                    if(compareToChange()) {
                        guiHelper.removeAssociated(widgetHolderToChange)
                        guiHelper.addWidget(prefs.music!!)
                    }
                }
                REQUEST_APPWIDGET_CLOCK -> {
                    prefs.clock = widgetPrefHelper(data!!)
                    databaseHandler.updatePrefsForAllStates(widgetHolderToChange!!,prefs.clock!!)
                    if(compareToChange()) {
                        guiHelper.removeAssociated(widgetHolderToChange)
                        guiHelper.addWidget(prefs.clock!!)
                    }
                }
                REQUEST_APPWIDGET_SEARCH -> {
                    prefs.search = widgetPrefHelper(data!!)
                    databaseHandler.updatePrefsForAllStates(widgetHolderToChange!!,prefs.search!!)
                    if(compareToChange()) {
                        guiHelper.removeAssociated(widgetHolderToChange)
                        guiHelper.addWidget(prefs.search!!)
                    }
                }
                REQUEST_APPWIDGET_EMAIL -> {
                    prefs.email = widgetPrefHelper(data!!)
                    databaseHandler.updatePrefsForAllStates(widgetHolderToChange!!,prefs.email!!)
                    if(compareToChange()) {
                        guiHelper.removeAssociated(widgetHolderToChange)
                        guiHelper.addWidget(prefs.email!!)
                    }
                }
                REQUEST_APPWIDGET_CALENDAR -> {
                    prefs.calendar = widgetPrefHelper(data!!)
                    databaseHandler.updatePrefsForAllStates(widgetHolderToChange!!,prefs.calendar!!)
                    if(compareToChange()) {
                        guiHelper.removeAssociated(widgetHolderToChange)
                        guiHelper.addWidget(prefs.calendar!!)
                    }
                }
                REQUEST_APPWIDGET_NOTES -> {
                    prefs.notes = widgetPrefHelper(data!!)
                    databaseHandler.updatePrefsForAllStates(widgetHolderToChange!!,prefs.notes!!)
                    if(compareToChange()) {
                        guiHelper.removeAssociated(widgetHolderToChange)
                        guiHelper.addWidget(prefs.notes!!)
                    }
                }
                REQUEST_APPWIDGET_WEATHER -> {
                    prefs.weather = widgetPrefHelper(data!!)
                    databaseHandler.updatePrefsForAllStates(widgetHolderToChange!!,prefs.weather!!)
                    if(compareToChange()) {
                        guiHelper.removeAssociated(widgetHolderToChange)
                        guiHelper.addWidget(prefs.weather!!)
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
        //mAppWidgetHost.deleteAppWidgetId(hostView.appWidgetId)
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
            surpressDriving()

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
                    widgetHolderToChange = prefs.getAttr(widgetList[which])
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
            this@MainActivity.runOnUiThread {
                userPattern.checkForStatePattern(mLastLocation,userProfile)
            }
        }
        else if(id == R.id.setDefault){
            Toast.makeText(this, "State Changed to Default", Toast.LENGTH_LONG).show()
            currentState = resources.getString(R.string.stateDefault)
            guiHelper.updateUserState(resources.getString(R.string.stateDefault))
            this@MainActivity.runOnUiThread {
                userPattern.checkForStatePattern(mLastLocation,userProfile)
            }
        }
        else if(id == R.id.setDriving){
            activateDriving()
        }

        return super.onOptionsItemSelected(item)
    }

    fun activateDriving(){
        drivingFlag = true
        Toast.makeText(this, "State Changed to Driving", Toast.LENGTH_LONG).show()

        val ft = supportFragmentManager.beginTransaction()
        ft.replace(R.id.fragmentM, drivingFragment)
        ft.commit()

        var addBtn : FloatingActionButton = findViewById(R.id.addNew)
        addBtn.hide()

        //updating fragment size
        //findViewById<View>(R.id.fragmentM).layoutParams.height = 980
    }

    fun surpressDriving(){
        drivingFlag = false
        val blankFragment : Fragment = BlankFragment()
        val ft = supportFragmentManager.beginTransaction()
        ft.replace(R.id.fragmentM, blankFragment)
        ft.commit()

        var addBtn = findViewById<FloatingActionButton>(R.id.addNew)
        addBtn.show()

        //updating fragment size
        //findViewById<View>(R.id.fragmentM).layoutParams.height = 0
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

        //if driving fragment is displayed
        if (findViewById<View>(R.id.fragmentM).layoutParams.height != 0) {
            drivingFragment.locationChanged(location)
        }

        //update map
        var zoomLevel = 16.0f
        //val latLng = LatLng(mLastLocation.latitude, mLastLocation.longitude)
        //map?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,zoomLevel))


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
            school = getLocationFromAddress(this, userProfile.getField("School").replace('|', ' '))
            sDistance  = mLastLocation.distanceTo(school)
        }catch (e: Exception){
            //val geocoder = Geocoder(this, Locale.getDefault())
            //locLabel.text = "" + geocoder.getFromLocation(mLastLocation.latitude, mLastLocation.longitude, 1)[0].getAddressLine(0)
        }
        try {
            work = getLocationFromAddress(this, userProfile.getField("Work").replace('|', ' '))
            wDistance  = mLastLocation.distanceTo(work)
        }catch (e: Exception){
            //val geocoder = Geocoder(this, Locale.getDefault())
            //locLabel.text = "" + geocoder.getFromLocation(mLastLocation.latitude, mLastLocation.longitude, 1)[0].getAddressLine(0)
        }
        try {
            home = getLocationFromAddress(this, userProfile.getField("Home").replace('|', ' '))
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
