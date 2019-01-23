package com.example.tomas.carsecurity.controller

import com.example.tomas.carsecurity.model.Event
import com.example.tomas.carsecurity.model.Position
import com.example.tomas.carsecurity.model.dto.EventCreate
import com.example.tomas.carsecurity.repository.CarRepository
import com.example.tomas.carsecurity.repository.EventRepository
import com.example.tomas.carsecurity.repository.EventTypeRepository
import com.example.tomas.carsecurity.repository.PositionRepository
import com.google.gson.Gson
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody

@Controller
class EventController {

    private val logger = LoggerFactory.getLogger(EventController::class.java)

    @Autowired
    private lateinit var eventRepository: EventRepository

    @Autowired
    private lateinit var eventTypeRepository: EventTypeRepository

    @Autowired
    private lateinit var carRepository: CarRepository

    @Autowired
    private lateinit var positionRepository: PositionRepository

    @ResponseBody
    @PostMapping(EVENT_MAPPING)
    fun createEvent(@RequestParam(value = "event") eventCreate: EventCreate): String {

        logger.info("Creating new event.")

        val car = carRepository.findById(eventCreate.carId)
        if (!car.isPresent) {
            logger.debug("Car id does not exists.")
            return createJsonSingle("error", "Invalid parameters.")
        }

        val eventType = eventTypeRepository.findById(eventCreate.eventTypeId)
        if (!eventType.isPresent) {
            logger.debug("EventType id does not exists.")
            return createJsonSingle("error", "Invalid parameters.")
        }

        val position = Position(0, null, eventCreate.position.latitude, eventCreate.position.longitude,
                eventCreate.position.altitude, null, eventCreate.position.accuracy, eventCreate.position.speed)
        positionRepository.save(position) // TODO check if is created
        logger.debug("Created new position.")

        val event = Event(0, eventCreate.name, eventType.get(), null, position, car.get(), eventCreate.note)
        eventRepository.save(event) // TODO check if is created
        logger.debug("Created new event")

        return createJsonSingle("success", "") // TODO empty
    }

    @ResponseBody
    @GetMapping(EVENT_MAPPING, params = ["event_id"])
    fun getEvent(@RequestParam(value = "event_id") eventId: Long): String {

        val event = eventRepository.findById(eventId)
        if (!event.isPresent) {
            logger.debug("Event does not exists.")
            return createJsonSingle("error", "Invalid parameters.")
        }

        return Gson().toJson(event.get())
    }


    @ResponseBody
    @GetMapping(EVENT_MAPPING, params = ["car_id"])
    fun getEvents(@RequestParam(value = "car_id") carId: Long,
                  @RequestParam(value = "page", defaultValue = "0") page: Int,
                  @RequestParam(value = "limit", defaultValue = "15") limit: Int): String {

        // TODO It is necessary return all events data? Maybe use only (name, timestamp, note, ...)
        val events = eventRepository.findAllByCarId(carId, PageRequest.of(page, limit))
        return Gson().toJson(events.get())
    }
}