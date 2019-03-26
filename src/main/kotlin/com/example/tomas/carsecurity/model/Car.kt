package com.example.tomas.carsecurity.model

import com.google.gson.JsonObject
import com.google.gson.JsonSerializer
import javax.persistence.*

/**
 * Database entity of car.
 */
@Entity(name = "car")
data class Car(

        /** Identification number of car in database. */
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        val id: Long = 0,

        /** Username of user which is owner of this car. */
        @JoinColumn(nullable = false)
        val username: String,

        /** List of routes which was created by this car. */
        @OneToMany(mappedBy = "car", fetch = FetchType.LAZY)
        val routes: List<Route> = ArrayList(),

        /** List of event which was created by this car. */
        @OneToMany(mappedBy = "car", fetch = FetchType.LAZY)
        val events: List<Event> = ArrayList(),

        /** Name of car. */
        @Column(nullable = false)
        var name: String = "",

        /** Note about car. */
        @Column(nullable = false)
        var note: String = "",

        /** Token which identifies car in Firebase database. */
        @Column(name = "firebase_token", nullable = false)
        var firebaseToken: String = ""
) {

    /**
     * Companion object for serialization for car.
     */
    companion object CarSerializer : GeneralSerializer() {
        private val serializer: JsonSerializer<Car> = JsonSerializer { car, _, _ ->
            val jsonCar = JsonObject()

            jsonCar.addProperty("id", car.id)
            jsonCar.addProperty("username", car.username)
            jsonCar.addProperty("routes", car.routes.size)
            jsonCar.addProperty("events", car.events.size)
            jsonCar.addProperty("name", car.name)
            jsonCar.addProperty("note", car.note)
            jsonCar
        }

        val gson = getGson(Car::class.java, serializer)
    }
}