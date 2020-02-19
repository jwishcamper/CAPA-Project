package com.example.capaproject

import android.content.ComponentName
import android.util.Log
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

abstract class CNmixin{
    @JsonCreator
    constructor(@JsonProperty("className") cls: String,@JsonProperty("packageName") pkg: String){

    }

}