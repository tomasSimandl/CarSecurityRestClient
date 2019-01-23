package com.example.tomas.carsecurity.controller

import com.example.tomas.carsecurity.repository.EventRepository
import com.google.gson.Gson
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
class EventController {

    private val logger = LoggerFactory.getLogger(EventController::class.java)

    @Autowired
    private lateinit var eventRepository: EventRepository

    @GetMapping(EVENT_MAPPING, params = ["event_id"])
    fun getEvent(@RequestParam(value = "event_id") eventId: Long): String {

        val event = eventRepository.findById(eventId)
        if (!event.isPresent) {
            logger.debug("Event does not exists.")
            return createJsonSingle("error", "Invalid parameters.")
        }

        return Gson().toJson(event.get())
    }


    @GetMapping(EVENT_MAPPING, params = ["car_id"])
    fun getEvents(@RequestParam(value = "car_id") carId: Long,
                  @RequestParam(value = "page", defaultValue = "0") page: Int,
                  @RequestParam(value = "limit", defaultValue = "15") limit: Int): String {

        // TODO It is necessary return all events data? Maybe use only (name, timestamp, note, ...)
        val events = eventRepository.findAllByCarId(carId, PageRequest.of(page, limit))
        return Gson().toJson(events.get())
    }
}