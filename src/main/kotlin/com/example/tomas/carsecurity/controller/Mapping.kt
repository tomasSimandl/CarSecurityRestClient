package com.example.tomas.carsecurity.controller

import com.google.gson.Gson
import com.google.gson.JsonObject

const val ROUTE_MAPPING = "/route"


// TODO maybe move to some other file. This is Mapping file not JSON file
fun createJsonSingle(key: String, value: String): String{

    val jsonObject = JsonObject()
    jsonObject.addProperty(key, value)

    return Gson().toJson(jsonObject)
}