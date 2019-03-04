package com.example.tomas.carsecurity.model.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.google.gson.annotations.SerializedName

data class StatusCreate(

        val battery: Float,

        @JsonProperty("is_charging")
        @SerializedName("is_charging")
        val isCharging: Boolean,

        @JsonProperty("is_power_save_mode")
        @SerializedName("is_power_save_mode")
        val powerSaveMode: Boolean,

        val utils: Map<String, Boolean>,

        val time: Long,

        @JsonProperty("car_id")
        @SerializedName("car_id")
        val carId: Long
)