package com.example.tomas.carsecurity.controller

import com.google.gson.Gson
import com.google.gson.JsonObject

/**
 * This object contains methods which are used across all controllers and mapping for every possible request.
 */


const val CAR_MAPPING = "/car"
const val ROUTE_MAPPING = "/route"
const val ROUTE_COUNT_MAPPING = "/route/count"
const val MAP_MAPPING = "/route/map"
const val ROUTE_EXPORT_MAPPING = "/route/export"
const val EVENT_MAPPING = "/event"
const val POSITION_MAPPING = "/position"
const val STATUS_MAPPING = "/status"
const val TOOL_MAPPING = "/tool"
const val FIREBASE_TOKEN_MAPPING = "/token"


/**
 * Method create simple json with only one attribute [key] and value [value].
 *
 * @param key is name of attribute
 * @param value is value of attribute
 * @return created json as a string.
 */
fun createJsonSingle(key: String, value: String): String {

    val jsonObject = JsonObject()
    jsonObject.addProperty(key, value)

    return Gson().toJson(jsonObject)
}