package com.example.tomas.carsecurity.controller

import com.example.tomas.carsecurity.model.Event
import com.example.tomas.carsecurity.model.Position
import com.example.tomas.carsecurity.model.dto.EventCreate
import com.example.tomas.carsecurity.model.dto.EventUpdate
import com.example.tomas.carsecurity.repository.*
import com.example.tomas.carsecurity.service.MailService
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.web.bind.annotation.*
import java.security.Principal
import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@RestController
class EventController(
        private val eventRepository: EventRepository,
        private val eventTypeRepository: EventTypeRepository,
        private val carRepository: CarRepository,
        private val positionRepository: PositionRepository,
        private val deleteUtil: DeleteUtil,
        private val mailService: MailService
) {

    private val logger = LoggerFactory.getLogger(EventController::class.java)

    @PostMapping(EVENT_MAPPING, produces = ["application/json; charset=utf-8"])
    @ResponseBody
    fun createEvent(
            principal: Principal,
            request: HttpServletRequest,
            response: HttpServletResponse,
            @RequestBody eventCreate: EventCreate
    ): String {

        logger.info("Starts creating of new event.")

        val car = carRepository.findById(eventCreate.carId)
        if (!car.isPresent) {
            logger.debug("Car id does not exists.")
            response.status = HttpServletResponse.SC_BAD_REQUEST
            return createJsonSingle("error", "Car id does not exists.")
        }

        if (principal.name == null || car.get().username != principal.name){
            logger.debug("User: ${principal.name} can not create event for car: ${eventCreate.carId}")
            response.status = HttpServletResponse.SC_UNAUTHORIZED
            return ""
        }

        val eventType = eventTypeRepository.findById(eventCreate.eventTypeId)
        if (!eventType.isPresent) {
            logger.debug("EventType id does not exists.")
            response.status = HttpServletResponse.SC_BAD_REQUEST
            return createJsonSingle("error", "Selected event type does not exists.")
        }

        val instant = Instant.ofEpochMilli(eventCreate.time)
        val zonedDateTime = ZonedDateTime.ofInstant(instant, ZoneOffset.UTC)

        val event = Event(0, eventType.get(), zonedDateTime, car.get(), eventCreate.note)
        eventRepository.save(event)
        logger.debug("Created new event")

        mailService.sendEvent(event, principal)

        response.status = HttpServletResponse.SC_CREATED
        return ""
    }

    @PutMapping(EVENT_MAPPING, produces = ["application/json; charset=utf-8"])
    @ResponseBody
    fun updateEventNote(
            principal: Principal,
            request: HttpServletRequest,
            response: HttpServletResponse,
            @RequestBody eventUpdate: EventUpdate
    ): String {

        logger.info("Starts updating of event.")

        val dbEvent = eventRepository.findById(eventUpdate.id)
        if(!dbEvent.isPresent){
            logger.debug("Can not update not existing event")
            response.status = HttpServletResponse.SC_BAD_REQUEST
            return createJsonSingle("error", "Event does not exists")
        }

        if (principal.name == null || dbEvent.get().car.username != principal.name){
            logger.debug("User: ${principal.name} can not update event ${eventUpdate.id}. Not owner of the car.")
            response.status = HttpServletResponse.SC_UNAUTHORIZED
            return ""
        }

        dbEvent.get().note = eventUpdate.note
        eventRepository.save(dbEvent.get())
        logger.debug("Event updated")

        response.status = HttpServletResponse.SC_CREATED
        return ""
    }

    @ResponseBody
    @DeleteMapping(EVENT_MAPPING, params = ["event_id"], produces = ["application/json; charset=utf-8"])
    fun deleteEvent(
            principal: Principal,
            request: HttpServletRequest,
            response: HttpServletResponse,
            @RequestParam(value = "event_id") eventId: Long
    ): String {

        val event = eventRepository.findById(eventId)
        if (!event.isPresent) {
            logger.debug("Event does not exists.")
            response.status = HttpServletResponse.SC_BAD_REQUEST
            return createJsonSingle("error", "Event does not exists")
        }

        if (principal.name == null || event.get().car.username != principal.name){
            logger.debug("User: ${principal.name} can not get event: $eventId")
            response.status = HttpServletResponse.SC_UNAUTHORIZED
            return ""
        }

        deleteUtil.deleteEvents(listOf(event.get()))
        return ""
    }

    @ResponseBody
    @GetMapping(EVENT_MAPPING, params = ["event_id"], produces = ["application/json; charset=utf-8"])
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
            return createJsonSingle("error", "Event does not exists")
        }

        if (principal.name == null || event.get().car.username != principal.name){
            logger.debug("User: ${principal.name} can not get event: $eventId")
            response.status = HttpServletResponse.SC_UNAUTHORIZED
            return ""
        }

        return Event.gson.toJson(event.get())
    }

    @ResponseBody
    @GetMapping(EVENT_MAPPING, produces = ["application/json; charset=utf-8"])
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
        val events = eventRepository.findAllByCar_UsernameOrderByTimeDesc(principal.name, PageRequest.of(page, validLimit))
        return Event.gson.toJson(events.content)
    }

    @ResponseBody
    @GetMapping(EVENT_MAPPING, params = ["car_id"], produces = ["application/json; charset=utf-8"])
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
            return createJsonSingle("error", "Event does not exists")
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