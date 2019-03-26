package com.example.tomas.carsecurity.model

import com.google.gson.JsonObject
import com.google.gson.JsonSerializer
import java.time.ZonedDateTime
import javax.persistence.*

/**
 * Database entity of event.
 */
@Entity(name = "event")
data class Event(

        /** Identification number of event in database. */
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        val id: Long = 0,

        /** Identification number of event type of which is this event. */
        @ManyToOne(fetch = FetchType.EAGER)
        @JoinColumn(nullable = false)
        val eventType: EventType,

        /** Time when event was created. */
        @Column(nullable = false)
        val time: ZonedDateTime,

        /** Car which create this event. */
        @ManyToOne(fetch = FetchType.EAGER)
        @JoinColumn(nullable = false)
        val car: Car,

        /** Note about this car. */
        @Column(nullable = false)
        var note: String = ""
) {

    /**
     * Companion object for serialization for event.
     */
    companion object EventSerializer : GeneralSerializer() {
        private val serializer: JsonSerializer<Event> = JsonSerializer { event, _, _ ->
            val jsonEvent = JsonObject()

            jsonEvent.addProperty("id", event.id)
            jsonEvent.addProperty("event_type_name", event.eventType.name)
            jsonEvent.addProperty("time", event.time.toEpochSecond())
            jsonEvent.addProperty("car_name", event.car.name)
            jsonEvent.addProperty("note", event.note)

            jsonEvent
        }

        val gson = getGson(Event::class.java, serializer)
    }
}