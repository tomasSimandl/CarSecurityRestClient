package com.example.tomas.carsecurity.model

import com.google.gson.JsonObject
import com.google.gson.JsonSerializer
import java.security.Timestamp
import javax.persistence.*

@Entity(name = "event")
data class Event(

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        val id: Long = 0,

        val name: String = "",

        @ManyToOne
        val eventType: EventType,

        val time: Timestamp?,

        @OneToOne
        val position: Position,

        @ManyToOne
        val car: Car,

        val note: String = ""
) {

    companion object EventSerializer : GeneralSerializer() {

        private val serializer: JsonSerializer<Event> = JsonSerializer { event, _, _ ->
            val jsonEvent = JsonObject()

            jsonEvent.addProperty("id", event.id)
            jsonEvent.addProperty("name", event.name)
            jsonEvent.addProperty("event_type_id", event.eventType.id)
            // TOOD timepstam
            jsonEvent.addProperty("position_id", event.position?.id)
            jsonEvent.addProperty("car_id", event.car.id)
            jsonEvent.addProperty("note", event.note)

            jsonEvent
        }

        val gson = getGson(Event::class.java, serializer)
    }
}