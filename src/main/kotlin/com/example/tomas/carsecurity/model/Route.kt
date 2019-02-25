package com.example.tomas.carsecurity.model

import com.example.tomas.carsecurity.repository.PositionRepository
import com.google.gson.JsonObject
import com.google.gson.JsonSerializer
import java.time.ZonedDateTime
import javax.persistence.*

@Entity(name = "route")
data class Route(

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        val id: Long = 0,

        @OneToMany(mappedBy = "route", fetch = FetchType.LAZY)
        val positions: List<Position> = ArrayList(),

        @Column(nullable = false)
        var length: Float = -1f,

        @Column(name = "avg_speed",nullable = false)
        var avgSpeed: Float = -1f,

        @Column(name = "seconds_of_travel", nullable = false)
        var secondsOfTravel: Long = -1,

        @Column(nullable = false)
        val time: ZonedDateTime,

        @ManyToOne
        @JoinColumn(nullable = false)
        val car: Car,

        @Column(nullable = false)
        var note: String = ""
) {

    fun removeStatistics(){
        length = -1f
        avgSpeed = -1f
        secondsOfTravel = -1
    }

    fun updateStatistics(positionRepository: PositionRepository): Boolean{

        if (avgSpeed == -1f || length == -1f || secondsOfTravel == -1L) {
            val avgSpeedOptional = positionRepository.avgSpeedOfRoute(id)
            if (!avgSpeedOptional.isPresent) return false
            val distanceOptional = positionRepository.sumDistanceOfRoute(id)
            if (!distanceOptional.isPresent) return false
            val timeOptional = positionRepository.travelTimeOfRoute(id)
            if (!timeOptional.isPresent) return false

            avgSpeed = avgSpeedOptional.get()
            length = distanceOptional.get()
            secondsOfTravel = timeOptional.get()
            return true
        }

        return false
    }

    companion object RouteSerializer : GeneralSerializer() {

        private val serializer: JsonSerializer<Route> = JsonSerializer { route, _, _ ->
            val jsonRoute = JsonObject()

            jsonRoute.addProperty("id", route.id)
            jsonRoute.addProperty("length", route.length)
            jsonRoute.addProperty("avg_speed", route.avgSpeed)
            jsonRoute.addProperty("seconds_of_travel", route.secondsOfTravel)
            jsonRoute.addProperty("time", route.time.toEpochSecond())
            jsonRoute.addProperty("car_id", route.car.id)
            jsonRoute.addProperty("car_name", route.car.name)
            jsonRoute.addProperty("note", route.note)

            jsonRoute
        }

        val gson = getGson(Route::class.java, serializer)
    }
}