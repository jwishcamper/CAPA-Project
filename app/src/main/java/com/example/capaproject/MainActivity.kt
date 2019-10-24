package com.example.capaproject

import android.app.ActionBar
import android.app.AlarmManager
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
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import kotlin.concurrent.fixedRateTimer

const val paddingHeight = 35

//if we want something to
const val indexOfTop=1
//used to keep track of created fragments
var viewIDs = mutableListOf<Int>()
var fragments = mutableListOf<Fragment>()
val stateHelper = stateChange()

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        createFragment("alarmDisplay",350)
        updateContext()

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

    //updates textbox context every 1000 milliseconds
    private fun updateContext(){
        fixedRateTimer("timer",false,0,1000){
            this@MainActivity.runOnUiThread {
                text.text = stateHelper.getContext()
            }
        }
    }
    //creates a new frame and fragment in it of type fragmentType
    private fun createFragment(fragmentType:String,height:Int=350){
        //add padding
        val newPadding = FrameLayout(this)
        newPadding.id = ViewCompat.generateViewId()
        viewIDs.add(newPadding.id)
        mainLayout.addView(newPadding)
        newPadding.layoutParams.height = paddingHeight
        newPadding.layoutParams.width =  ActionBar.LayoutParams.MATCH_PARENT

        //add new frame
        val newFrag = FrameLayout(this)
        newFrag.id = ViewCompat.generateViewId()
        viewIDs.add(newFrag.id)
        mainLayout.addView(newFrag)
        newFrag.layoutParams.height = height
        newFrag.layoutParams.width =  ActionBar.LayoutParams.MATCH_PARENT

        //add fragment to created frame
        val fragToAdd = when(fragmentType) {
            "alarmDisplay" -> { alarmDisplay() }
            else -> { testingFragment() }
        }
        fragments.add(fragToAdd)
        addFragment(fragToAdd, newFrag.id)
    }


    private fun createTopFragment(fragmentType:String,height:Int=350){


        val newFrag = FrameLayout(this)
        newFrag.id = ViewCompat.generateViewId()
        viewIDs.add(newFrag.id)
        mainLayout.addView(newFrag,indexOfTop)
        newFrag.layoutParams.height = height
        newFrag.layoutParams.width =  ActionBar.LayoutParams.MATCH_PARENT

        val newPadding = FrameLayout(this)
        newPadding.id = ViewCompat.generateViewId()
        viewIDs.add(newPadding.id)
        mainLayout.addView(newPadding,indexOfTop)
        newPadding.layoutParams.height = paddingHeight
        newPadding.layoutParams.width =  ActionBar.LayoutParams.MATCH_PARENT

        //add fragment to created frame
        val fragToAdd = when(fragmentType) {
            "alarmDisplay" -> { alarmDisplay() }
            else -> { testingFragment() }
        }
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
