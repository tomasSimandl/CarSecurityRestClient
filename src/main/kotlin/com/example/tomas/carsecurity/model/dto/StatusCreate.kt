package com.example.tomas.carsecurity.model.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class StatusCreate(

        val battery: Float,

        @JsonProperty("is_charging")
        val isCharging: Boolean,

        @JsonProperty("is_power_save_mode")
        val powerSaveMode: Boolean,

        val utils: Map<String, Boolean>,

        val time: Long,

        @JsonProperty("car_id")
        val carId: Long
)