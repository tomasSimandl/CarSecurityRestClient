package com.example.tomas.carsecurity.model

import com.google.gson.JsonObject
import com.google.gson.JsonSerializer
import java.time.LocalDateTime
import javax.persistence.*

@Entity(name = "position")
@Table(uniqueConstraints = [UniqueConstraint(columnNames = ["latitude", "longitude", "altitude", "time", "accuracy"])])
data class Position(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        val id: Long = 0,

        @ManyToOne
        val route: Route?,

        @Column(nullable = false)
        val latitude: Float = 0f,

        @Column(nullable = false)
        val longitude: Float = 0f,

        @Column(nullable = false)
        val altitude: Float = 0f,

        @Column(nullable = false)
        val time: LocalDateTime,

        @Column(nullable = false)
        val accuracy: Float = 0f,

        @Column(nullable = false)
        val speed: Float = 0f
) {

    companion object PositionSerializer : GeneralSerializer() {

        private val serializer: JsonSerializer<Position> = JsonSerializer { position, _, _ ->
            val jsonPosition = JsonObject()

            jsonPosition.addProperty("id", position.id)
            jsonPosition.addProperty("accuracy", position.accuracy)
            jsonPosition.addProperty("altitude", position.altitude)
            jsonPosition.addProperty("latitude", position.latitude)
            jsonPosition.addProperty("longitude", position.longitude)
            jsonPosition.addProperty("speed", position.speed)
            jsonPosition.addProperty("time", position.time.toString())
            jsonPosition.addProperty("route_id", position.route?.id)

            jsonPosition
        }

        val gson = getGson(Position::class.java, serializer)
    }
}