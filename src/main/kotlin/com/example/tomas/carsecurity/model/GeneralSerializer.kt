package com.example.tomas.carsecurity.model

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonSerializer
import java.lang.reflect.Type

/**
 * Parent class of object which is used for models serialization.
 */
open class GeneralSerializer {

    /**
     * Method return Gson which can be used for serialization.
     *
     * @param type type of object which will be serialized.
     * @param serializer which will be used for serialization.
     */
    fun getGson(type: Type, serializer: JsonSerializer<*>): Gson {
        val gsonBuilder = GsonBuilder()
        gsonBuilder.registerTypeAdapter(type, serializer)
        return gsonBuilder.create()
    }
}