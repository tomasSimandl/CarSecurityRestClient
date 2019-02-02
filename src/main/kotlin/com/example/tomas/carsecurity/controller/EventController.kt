package com.example.tomas.carsecurity.controller

import com.example.tomas.carsecurity.model.Event
import com.example.tomas.carsecurity.model.Position
import com.example.tomas.carsecurity.model.dto.EventCreate
import com.example.tomas.carsecurity.repository.CarRepository
import com.example.tomas.carsecurity.repository.EventRepository
import com.example.tomas.carsecurity.repository.EventTypeRepository
import com.example.tomas.carsecurity.repository.PositionRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

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

    @PostMapping(EVENT_MAPPING)
    fun createEvent(@RequestBody eventCreate: EventCreate, request: HttpServletRequest,
                    response: HttpServletResponse) {

        logger.info("Starts creating of new event.")

        val car = carRepository.findById(eventCreate.carId)
        if (!car.isPresent) {
            logger.debug("Car id does not exists.")
            response.status = HttpServletResponse.SC_BAD_REQUEST
            return
        }

        val eventType = eventTypeRepository.findById(eventCreate.eventTypeId)
        if (!eventType.isPresent) {
            logger.debug("EventType id does not exists.")
            response.status = HttpServletResponse.SC_BAD_REQUEST
            return
        }

        val position = if (eventCreate.position == null) {
            null
        } else {
            var newPosition = Position(0, null, eventCreate.position.latitude, eventCreate.position.longitude,
                    eventCreate.position.altitude, eventCreate.position.time, eventCreate.position.accuracy,
                    eventCreate.position.speed)
            newPosition = positionRepository.save(newPosition)
            logger.debug("Created new position.")

            newPosition
        }

        // TODO LocalDateTime can throw exception
        val event = Event(0, eventType.get(), LocalDateTime.parse(eventCreate.time), position, car.get(), eventCreate.note)
        eventRepository.save(event)
        logger.debug("Created new event")

        response.status = HttpServletResponse.SC_CREATED
        return
    }

    @ResponseBody
    @GetMapping(EVENT_MAPPING, params = ["event_id"])
    fun getEvent(@RequestParam(value = "event_id") eventId: Long, request: HttpServletRequest,
                 response: HttpServletResponse): String {

        val event = eventRepository.findById(eventId)
        if (!event.isPresent) {
            logger.debug("Event does not exists.")
            response.status = HttpServletResponse.SC_BAD_REQUEST
            return ""
        }

        return Event.gson.toJson(event.get())
    }


    @ResponseBody
    @GetMapping(EVENT_MAPPING, params = ["car_id"])
    fun getEvents(@RequestParam(value = "car_id") carId: Long,
                  @RequestParam(value = "page", defaultValue = "0") page: Int,
                  @RequestParam(value = "limit", defaultValue = "15") limit: Int): String {

        val validLimit = if (limit <= 0) 1 else limit
        val events = eventRepository.findAllByCarId(carId, PageRequest.of(page, validLimit))
        return Event.gson.toJson(events.content)
    }
}