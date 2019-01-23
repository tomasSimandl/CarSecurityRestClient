package com.example.tomas.carsecurity.model

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonSerializer
import java.lang.reflect.Type

open class GeneralSerializer {

    fun getGson(type: Type, serializer: JsonSerializer<*>) : Gson {
        val gsonBuilder = GsonBuilder()
        gsonBuilder.registerTypeAdapter(type, serializer)
        return gsonBuilder.create()
    }
}