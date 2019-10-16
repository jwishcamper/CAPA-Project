package com.example.capaproject

import android.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import kotlinx.android.synthetic.main.activity_main.*

const val paddingHeight = 35

//used to keep track of created fragments
var fragmentIDs = mutableListOf<Int>()

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        for (i in 1..7){ createFragment("testFragment",400) }

//        replaceFragment(testingFragment(),fragmentIDs[0])
    }

    //creates a new frame and fragment in it of type fragmentType
    private fun createFragment(fragmentType:String,height:Int=350){
        //add padding
        var newPadding = FrameLayout(this)
        linearLayout.addView(newPadding)
        newPadding.layoutParams.height = paddingHeight
        newPadding.layoutParams.width =  ActionBar.LayoutParams.MATCH_PARENT

        //add new frame
        var newFrag = FrameLayout(this)
        newFrag.id = ViewCompat.generateViewId()
        fragmentIDs.add(newFrag.id)
        linearLayout.addView(newFrag)
        newFrag.layoutParams.height = height
        newFrag.layoutParams.width =  ActionBar.LayoutParams.MATCH_PARENT

        //add fragment to created frame
        when(fragmentType) {
            "testFragment" -> { addFragment(testingFragment(), newFrag.id) }
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
