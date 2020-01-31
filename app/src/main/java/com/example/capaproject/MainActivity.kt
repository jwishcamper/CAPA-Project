package com.example.capaproject

import android.content.ComponentName
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.collections.HashMap
import kotlin.concurrent.fixedRateTimer
import android.app.Activity
import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.Intent
import android.content.res.Resources
import android.view.*
import java.util.ArrayList

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
    //
    private var currentWidgetList = mutableListOf<AppWidgetProviderInfo>()
    private lateinit var mAppWidgetManager: AppWidgetManager
    private lateinit var mAppWidgetHost: AppWidgetHost
    private val APPWIDGET_HOST_ID = 1
    private val REQUEST_PICK_APPWIDGET = 2
    private val REQUEST_CREATE_APPWIDGET = 3
    private val REQUEST_APPWIDGET_CLOCK_CHAIN = 4
    private val REQUEST_APPWIDGET_MUSIC_CHAIN = 5
    private val REQUEST_APPWIDGET_CLOCK = 6
    private val REQUEST_APPWIDGET_MUSIC = 7
    lateinit var infos : List<AppWidgetProviderInfo>

    private lateinit var mainlayout: ViewGroup

    //helper object to determine user state
    private lateinit var stateHelper: stateChange
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mainlayout = findViewById(R.id.mainLayout)

        //database variables
        databaseHandler = DatabaseHandler(this)

        //NUKE THE DATABASE!!!!!
        databaseHandler.deleteInfo()

        //widget resources
        mAppWidgetManager = AppWidgetManager.getInstance(this)
        mAppWidgetHost = AppWidgetHost(this, APPWIDGET_HOST_ID)
        infos = mAppWidgetManager.installedProviders


        //screenHeight = getScreenHeight()

        prefs = UserPrefApps()
        //Load preferences from database here

        //If user has never set prefs, ask for default widgets
        if(prefs.isEmpty())
            queryUserPrefWidget("Clock")
        else
            finishOnCreate()
    }

    private fun finishOnCreate(){
        stateHelper = stateChange(this)
        guiHelper = CAPAstate(this, databaseHandler, prefs)
        guiHelper.updateUserState("atWork")
        updateContext()
    }
    //Initial query to ask user for all defaults if none exist
    fun queryUserPrefWidget(widgetType : String){

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Please select your preferred $widgetType widget from the following list: ")
        builder.setPositiveButton("OK") { dialog, _ ->
            val appWidgetId = this.mAppWidgetHost.allocateAppWidgetId()
            val pickIntent = Intent(AppWidgetManager.ACTION_APPWIDGET_PICK)
            pickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            when(widgetType) {
                in "Clock" -> startActivityForResult(pickIntent, REQUEST_APPWIDGET_CLOCK_CHAIN)
                "Music" -> startActivityForResult(pickIntent, REQUEST_APPWIDGET_MUSIC_CHAIN)
            }
        }
        builder.create()
        builder.show()

    }
    //for changing individual default widgets
    private fun helperQueryUserPrefWidget(widgetType : String){

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Please select your preferred $widgetType widget from the following list: ")
        builder.setPositiveButton("OK") { dialog, _ ->
            val appWidgetId = this.mAppWidgetHost.allocateAppWidgetId()
            val pickIntent = Intent(AppWidgetManager.ACTION_APPWIDGET_PICK)
            pickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            when(widgetType) {
                in "Clock" -> startActivityForResult(pickIntent, REQUEST_APPWIDGET_CLOCK)
                "Music" -> startActivityForResult(pickIntent, REQUEST_APPWIDGET_MUSIC)
            }
        }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
        }
        builder.create()
        builder.show()

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
    private fun updateContext(){
        fixedRateTimer("timer",false,0,1000){
            this@MainActivity.runOnUiThread {
                text.text = stateHelper.getContext()
                if(currentActivity == "Still"){
                    guiHelper.updateUserState("default")
                }
                else if(currentActivity!="Still"){
                    guiHelper.updateUserState("atWork")
                }
                //Log.d("PrefClock: ",prefs.clock.className)
                //Log.d("PrefMusic: ",prefs.music.className)
                //guiHelper.refresh()
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
                REQUEST_APPWIDGET_CLOCK_CHAIN -> {
                    prefs.clock = widgetPrefHelper(data!!)
                    queryUserPrefWidget("Music")
                }
                REQUEST_APPWIDGET_MUSIC_CHAIN -> {
                    prefs.music = widgetPrefHelper(data!!)
                    finishOnCreate()
                }
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
        hostView.setAppWidget(appWidgetId, appWidgetInfo)
        mainlayout.addView(hostView)


        val cn = ComponentName(
            appWidgetInfo.provider.packageName,
            appWidgetInfo.provider.className
        )
        guiHelper.addWidget(cn)
        //Log.d("TAG",appWidgetInfo.provider.packageName)
        //Log.d("TAG",appWidgetInfo.provider.className)


        currentWidgetList.add(appWidgetInfo)
    }

    override fun onPause(){
        super.onPause()
        //save current UI for current state to database
        if(::guiHelper.isInitialized)
            databaseHandler.addState(guiHelper.getState(),guiHelper.getList())

        //Save user pref apps to database here

    }
    override fun onStart() {
        super.onStart()
        mAppWidgetHost.startListening()
    }

    override fun onStop() {
        super.onStop()
        mAppWidgetHost.stopListening()
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

            //load info from database
            var map = databaseHandler.getSurveyInfo()
            if(map.isEmpty()) {
                map["Home"] = ""
                map["Work"] = ""
                map["School"] = ""
                map["Gender"] = "Other"
                map["BirthDay"] = "01/01/1930"
            }

            val surveyOne = Survey(map,this)

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

        return super.onOptionsItemSelected(item)
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
