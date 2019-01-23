package com.example.tomas.carsecurity.model

import com.google.gson.JsonObject
import com.google.gson.JsonSerializer
import javax.persistence.*

@Entity(name = "route")
data class Route(

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        val id: Long = 0,

        @OneToOne
        val startPosition: Position?,

        @OneToOne
        val endPosition: Position?,

        @OneToMany(mappedBy = "route", fetch = FetchType.LAZY)
        val positions: List<Position>,

        val length: Float = 0f,

        @ManyToOne
        val car: Car,

        val note: String = ""
) {

    companion object RouteSerializer {

        val serializer: JsonSerializer<Route> = JsonSerializer { route, _, _ ->
            val jsonRoute = JsonObject()

            jsonRoute.addProperty("id", route.id)
            jsonRoute.addProperty("length", route.length)
            jsonRoute.addProperty("note", route.note)
            jsonRoute.addProperty("car_id", route.car.id)
            jsonRoute.addProperty("end_position_id", route.endPosition?.id)
            jsonRoute.addProperty("start_position_id", route.startPosition?.id)

            jsonRoute
        }
    }
}