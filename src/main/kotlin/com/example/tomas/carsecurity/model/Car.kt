package com.example.tomas.carsecurity.model

import com.google.gson.JsonObject
import com.google.gson.JsonSerializer
import javax.persistence.*

@Entity(name = "car")
data class Car(

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        val id: Long = 0,

        @JoinColumn(nullable = false)
        val username: String,

        @OneToMany(mappedBy = "car", fetch = FetchType.LAZY)
        val routes: List<Route> = ArrayList(),

        @OneToMany(mappedBy = "car", fetch = FetchType.LAZY)
        val events: List<Event> = ArrayList(),

        @Column(nullable = false)
        val name: String = "",

        @Column(nullable = false)
        val icon: String = ""
) {

        companion object CarSerializer : GeneralSerializer() {

                private val serializer: JsonSerializer<Car> = JsonSerializer { car, _, _ ->
                        val jsonCar = JsonObject()

                        jsonCar.addProperty("id", car.id)
                        jsonCar.addProperty("username", car.username)
                        jsonCar.addProperty("routes", car.routes.size)
                        jsonCar.addProperty("events", car.events.size)
                        jsonCar.addProperty("name", car.name)
                        jsonCar.addProperty("icon", car.icon)
                        jsonCar
                }

                val gson = getGson(Car::class.java, serializer)
        }
}