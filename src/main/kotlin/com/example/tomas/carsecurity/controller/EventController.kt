package com.example.tomas.carsecurity.controller

import com.example.tomas.carsecurity.model.Event
import com.example.tomas.carsecurity.model.Position
import com.example.tomas.carsecurity.model.dto.EventCreate
import com.example.tomas.carsecurity.repository.CarRepository
import com.example.tomas.carsecurity.repository.EventRepository
import com.example.tomas.carsecurity.repository.EventTypeRepository
import com.example.tomas.carsecurity.repository.PositionRepository
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import java.security.Principal
import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Controller
class EventController(
        private val eventRepository: EventRepository,
        private val eventTypeRepository: EventTypeRepository,
        private val carRepository: CarRepository,
        private val positionRepository: PositionRepository
) {

    private val logger = LoggerFactory.getLogger(EventController::class.java)

    @PostMapping(EVENT_MAPPING)
    fun createEvent(
            principal: Principal,
            request: HttpServletRequest,
            response: HttpServletResponse,
            @RequestBody eventCreate: EventCreate
    ) {

        logger.info("Starts creating of new event.")

        val car = carRepository.findById(eventCreate.carId)
        if (!car.isPresent) {
            logger.debug("Car id does not exists.")
            response.status = HttpServletResponse.SC_BAD_REQUEST
            return
        }

        if (principal.name == null || car.get().username != principal.name){
            logger.debug("User: ${principal.name} can not create event for car: ${eventCreate.carId}")
            response.status = HttpServletResponse.SC_UNAUTHORIZED
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
            val instant = Instant.ofEpochMilli(eventCreate.position.time)
            val zonedDateTime = ZonedDateTime.ofInstant(instant, ZoneOffset.UTC)

            var newPosition = Position(0, null, eventCreate.position.latitude, eventCreate.position.longitude,
                    eventCreate.position.altitude, zonedDateTime, eventCreate.position.accuracy,
                    eventCreate.position.speed)
            newPosition = positionRepository.save(newPosition)
            logger.debug("Created new position.")

            newPosition
        }

        val instant = Instant.ofEpochMilli(eventCreate.time)
        val zonedDateTime = ZonedDateTime.ofInstant(instant, ZoneOffset.UTC)

        val event = Event(0, eventType.get(), zonedDateTime, position, car.get(), eventCreate.note)
        eventRepository.save(event)
        logger.debug("Created new event")

        response.status = HttpServletResponse.SC_CREATED
        return
    }

    @ResponseBody
    @GetMapping(EVENT_MAPPING, params = ["event_id"])
    fun getEvent(
            principal: Principal,
            request: HttpServletRequest,
            response: HttpServletResponse,
            @RequestParam(value = "event_id") eventId: Long
    ): String {

        val event = eventRepository.findById(eventId)
        if (!event.isPresent) {
            logger.debug("Event does not exists.")
            response.status = HttpServletResponse.SC_BAD_REQUEST
            return ""
        }

        if (principal.name == null || event.get().car.username != principal.name){
            logger.debug("User: ${principal.name} can not get event: $eventId")
            response.status = HttpServletResponse.SC_UNAUTHORIZED
            return ""
        }

        return Event.gson.toJson(event.get())
    }

    @ResponseBody
    @GetMapping(EVENT_MAPPING)
    fun getEventsOfLogUser(
            principal: Principal,
            request: HttpServletRequest,
            response: HttpServletResponse,
            @RequestParam(value = "page", defaultValue = "0") page: Int,
            @RequestParam(value = "limit", defaultValue = "15") limit: Int
    ): String {

        if (principal.name == null) {
            logger.debug("Principal is null.")
            response.status = HttpServletResponse.SC_UNAUTHORIZED
            return ""
        }

        val validLimit = if (limit <= 0) 1 else limit
        val events = eventRepository.findAllByCar_Username(principal.name, PageRequest.of(page, validLimit))
        return Event.gson.toJson(events.content)
    }

    @ResponseBody
    @GetMapping(EVENT_MAPPING, params = ["car_id"])
    fun getEvents(
            principal: Principal,
            request: HttpServletRequest,
            response: HttpServletResponse,
            @RequestParam(value = "car_id") carId: Long,
            @RequestParam(value = "page", defaultValue = "0") page: Int,
            @RequestParam(value = "limit", defaultValue = "15") limit: Int
    ): String {

        val car = carRepository.findById(carId)
        if (!car.isPresent) {
            logger.debug("Car id does not exists.")
            response.status = HttpServletResponse.SC_BAD_REQUEST
            return ""
        }

        if (principal.name == null || car.get().username != principal.name) {
            logger.debug("User: ${principal.name} is not owner of car: $carId")
            response.status = HttpServletResponse.SC_UNAUTHORIZED
            return ""
        }

        val validLimit = if (limit <= 0) 1 else limit
        val events = eventRepository.findAllByCarId(carId, PageRequest.of(page, validLimit))
        return Event.gson.toJson(events.content)
    }
}