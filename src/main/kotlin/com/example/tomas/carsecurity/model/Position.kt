package com.example.tomas.carsecurity.model

import com.example.tomas.carsecurity.model.dto.PositionCreate
import com.google.gson.JsonObject
import com.google.gson.JsonSerializer
import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime
import javax.persistence.*

/**
 * Database entity of position.
 */
@Entity(name = "position")
@Table(uniqueConstraints = [UniqueConstraint(columnNames = ["latitude", "longitude", "altitude", "time", "accuracy"])])
data class Position(

        /** Identification number of position in database. */
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        val id: Long = 0,

        /** Route on which was created this position. */
        @ManyToOne
        val route: Route?,

        /** Latitude of record */
        @Column(nullable = false)
        val latitude: Float = 0f,

        /** Longitude of record */
        @Column(nullable = false)
        val longitude: Float = 0f,

        /** Altitude of record */
        @Column(nullable = false)
        val altitude: Float = 0f,

        /** Time when record was created. */
        @Column(nullable = false)
        val time: ZonedDateTime,

        /** Sensor accuracy of record */
        @Column(nullable = false)
        val accuracy: Float = 0f,

        /** Distance from last record */
        @Column(nullable = false)
        val distance: Float = 0f,

        /** Actual speed when record was created */
        @Column(nullable = false)
        val speed: Float = 0f
) {

    /**
     * Constructor which convert [PositionCreate] object to this model.
     *
     * @param positionCreate is object which will be transform to this object.
     * @param route on which was position created.
     */
    constructor(positionCreate: PositionCreate, route: Route?) : this(
            id = 0,
            route = route,
            latitude = positionCreate.latitude,
            longitude = positionCreate.longitude,
            altitude = positionCreate.altitude,
            time = ZonedDateTime.ofInstant(Instant.ofEpochMilli(positionCreate.time), ZoneOffset.UTC),
            accuracy = positionCreate.accuracy,
            distance = positionCreate.distance,
            speed = positionCreate.speed)

    /**
     * Companion object for serialization for position.
     */
    companion object PositionSerializer : GeneralSerializer() {
        private val serializer: JsonSerializer<Position> = JsonSerializer { position, _, _ ->
            val jsonPosition = JsonObject()

            jsonPosition.addProperty("id", position.id)
            jsonPosition.addProperty("accuracy", position.accuracy)
            jsonPosition.addProperty("altitude", position.altitude)
            jsonPosition.addProperty("latitude", position.latitude)
            jsonPosition.addProperty("longitude", position.longitude)
            jsonPosition.addProperty("speed", position.speed)
            jsonPosition.addProperty("time", position.time.toEpochSecond())
            jsonPosition.addProperty("distance", position.distance)
            jsonPosition.addProperty("route_id", position.route?.id)

            jsonPosition
        }

        val gson = getGson(Position::class.java, serializer)
    }
}