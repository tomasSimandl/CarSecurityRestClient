package com.example.tomas.carsecurity.model

import com.google.gson.JsonObject
import com.google.gson.JsonSerializer
import java.time.LocalDateTime
import java.time.ZonedDateTime
import javax.persistence.*

@Entity(name = "event")
data class Event(

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        val id: Long = 0,

        @ManyToOne(fetch = FetchType.EAGER)
        @JoinColumn(nullable = false)
        val eventType: EventType,

        @Column(nullable = false)
        val time: ZonedDateTime,

        @ManyToOne(fetch = FetchType.EAGER)
        @JoinColumn(nullable = false)
        val car: Car,

        @Column(nullable = false)
        var note: String = ""
) {

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