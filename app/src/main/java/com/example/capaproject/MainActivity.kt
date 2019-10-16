package com.example.capaproject

import android.app.ActionBar
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.FrameLayout
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import kotlinx.android.synthetic.main.activity_main.*

const val fragHeight = 200
const val paddingHeight = 5

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        for (i in 1..11){ createFragment("testFragment") }
    }

    //creates a new frame and fragment in it of type fragmentType
    private fun createFragment(fragmentType:String){
        //add padding
        var newPadding = FrameLayout(this)
        linearLayout.addView(newPadding)
        newPadding.layoutParams.height = paddingHeight
        newPadding.layoutParams.width =  ActionBar.LayoutParams.MATCH_PARENT

        //add new frame
        var newFrag = FrameLayout(this)
        newFrag.id = ViewCompat.generateViewId()
        linearLayout.addView(newFrag)
        newFrag.layoutParams.height = fragHeight
        newFrag.layoutParams.width =  ActionBar.LayoutParams.MATCH_PARENT
        newFrag.setBackgroundColor(Color.rgb((128..255).random(), (128..255).random(), (128..255).random()))

        //add fragment to created frame
        when(fragmentType) {
            "testFragment" -> addFragment(testingFragment(), newFrag.id)
        }
    }


    //helper functions to add fragments more easily
    inline fun FragmentManager.inTransaction(func: FragmentTransaction.() -> Unit) {
        val fragmentTransaction = beginTransaction()
        fragmentTransaction.func()
        fragmentTransaction.commit()
    }
    fun AppCompatActivity.addFragment(fragment: Fragment, frameId: Int){
        supportFragmentManager.inTransaction { add(frameId, fragment) }
    }
    fun AppCompatActivity.replaceFragment(fragment: Fragment, frameId: Int) {
        supportFragmentManager.inTransaction{replace(frameId, fragment)}
    }
}
