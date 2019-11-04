package com.example.capaproject

import android.app.ActionBar
import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.view.View
import android.view.View.GONE
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.ActivityRecognition
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import kotlin.concurrent.fixedRateTimer
import android.R.attr.y
import android.R.attr.x
import android.graphics.Point
import android.view.Display

//space between fragments
const val paddingHeight = 35

//If there is an XML element to be at top of screen, increment this
const val indexOfTop=1

//used to keep track of created view IDs and fragments
var viewIDs = mutableListOf<Int>()
var fragments = mutableListOf<Fragment>()


class MainActivity : AppCompatActivity() {

    //helper object to determine user state
    lateinit var stateHelper: stateChange

    //currentActivity is current most probable activity
companion object{
    var currentActivity : String = "None"
}
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        stateHelper = stateChange(this)

        createFragment("alarmDisplay",getScreenHeight()/7)
        createFragment("mediaPlayer",getScreenHeight()/7, indexOfTop)
        updateContext()

    }

    //updates textbox context every 1000 milliseconds
    //placeholder to be used for testing
    private fun updateContext(){
        fixedRateTimer("timer",false,0,1000){
            this@MainActivity.runOnUiThread {
                text.text = stateHelper.getContext()
            }
        }
    }

    private fun getScreenHeight() : Int{
        val display = windowManager.defaultDisplay
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
        val fragToAdd = when(fragmentType) {
            "alarmDisplay" -> { alarmDisplay() }
            "mediaPlayer" -> { mediaPlayer() }
            else -> { testingFragment() }
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

}
