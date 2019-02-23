package com.example.tomas.carsecurity.controller

import com.google.gson.Gson
import com.google.gson.JsonObject

const val CAR_MAPPING = "/car"
const val ROUTE_MAPPING = "/route"
const val MAP_MAPPING = "/route/map"
const val EVENT_MAPPING = "/event"
const val POSITION_MAPPING = "/position"


// TODO maybe move to some other file. This is Mapping file not JSON file
fun createJsonSingle(key: String, value: String): String{

    val jsonObject = JsonObject()
    jsonObject.addProperty(key, value)

    return Gson().toJson(jsonObject)
}

fun addParams(url: String, params: Map<String, String>): String {
    var newUrl = url
    var appendChar = '?'

    for(value in params){
        newUrl = newUrl.plus("$appendChar${value.key}=${value.value}")
        appendChar = '&'
    }
    return newUrl
}