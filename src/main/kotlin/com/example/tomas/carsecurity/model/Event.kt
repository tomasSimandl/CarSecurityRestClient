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

        @ManyToOne
        @JoinColumn(nullable = false)
        val eventType: EventType,

        @Column(nullable = false)
        val time: ZonedDateTime,

        @OneToOne
        val position: Position?,

        @ManyToOne
        @JoinColumn(nullable = false)
        val car: Car,

        @Column(nullable = false)
        var note: String = ""
) {

    companion object EventSerializer : GeneralSerializer() {

        private val serializer: JsonSerializer<Event> = JsonSerializer { event, _, _ ->
            val jsonEvent = JsonObject()

            jsonEvent.addProperty("id", event.id)
            jsonEvent.addProperty("event_type_id", event.eventType.id)
            // TOOD timestamp
            jsonEvent.addProperty("position_id", event.position?.id)
            jsonEvent.addProperty("car_id", event.car.id)
            jsonEvent.addProperty("note", event.note)

            jsonEvent
        }

        val gson = getGson(Event::class.java, serializer)
    }
}