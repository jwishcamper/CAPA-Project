package com.example.capaproject

import android.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
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
//used to keep track of created fragments
var viewIDs = mutableListOf<Int>()
var fragments = mutableListOf<Fragment>()

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        for (i in 1..7){ createFragment("testFragment",i*100) }
        removeAllFragments()
        createFragment("testFragment",800)
        createFragment("testFragment",700)
        createFragment("testFragment",350)
        updateTime()

    }

    private fun removeAllFragments(){
        for ( i in fragments){
            removeFragment(i)
        }
        fragments.clear()
        for (i in viewIDs){
            var currentFrame :View = findViewById(i)
                currentFrame.visibility = GONE
        }
        viewIDs.clear()
    }

    private fun updateTime(){
        fixedRateTimer("timer",false,0,1000){
            this@MainActivity.runOnUiThread {
                val ef = stateChange()
                text.text = ef.getTime()
            }
        }
    }
    //creates a new frame and fragment in it of type fragmentType
    private fun createFragment(fragmentType:String,height:Int=350){
        //add padding
        var newPadding = FrameLayout(this)
        newPadding.id = ViewCompat.generateViewId()
        viewIDs.add(newPadding.id)
        mainLayout.addView(newPadding)
        newPadding.layoutParams.height = paddingHeight
        newPadding.layoutParams.width =  ActionBar.LayoutParams.MATCH_PARENT

        //add new frame
        var newFrag = FrameLayout(this)
        newFrag.id = ViewCompat.generateViewId()
        viewIDs.add(newFrag.id)
        mainLayout.addView(newFrag)
        newFrag.layoutParams.height = height
        newFrag.layoutParams.width =  ActionBar.LayoutParams.MATCH_PARENT

        //add fragment to created frame
        when(fragmentType) {
            "testFragment" -> {
                val fragToAdd = testingFragment()
                fragments.add(fragToAdd)
                addFragment(fragToAdd, newFrag.id) }
        }
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
