package com.example.capaproject

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment


class testingFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val inf = inflater.inflate(R.layout.fragment_testing, container, false)
        //val textV: TextView = inf.findViewById(R.id.tView)
        //val currentTime = Calendar.getInstance().time
        //textV.text = currentTime.toString()
        return inf
    }


}
