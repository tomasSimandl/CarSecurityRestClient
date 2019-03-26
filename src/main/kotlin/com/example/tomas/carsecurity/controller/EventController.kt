package com.example.tomas.carsecurity.controller

import com.example.tomas.carsecurity.model.Event
import com.example.tomas.carsecurity.model.dto.EventCreate
import com.example.tomas.carsecurity.model.dto.EventUpdate
import com.example.tomas.carsecurity.repository.CarRepository
import com.example.tomas.carsecurity.repository.DeleteUtil
import com.example.tomas.carsecurity.repository.EventRepository
import com.example.tomas.carsecurity.repository.EventTypeRepository
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

/**
 * This controller is used for access and manage events in database. User is authorized to access only his own events.
 *
 * @param eventRepository is repository for access events in database.
 * @param eventTypeRepository is repository fo access event types in database.
 * @param carRepository is repository for access cars in database.
 * @param deleteUtil is util used for delete events from database.
 * @param mailService is used for sending mail messages.
 */
@RestController
class EventController(
        private val eventRepository: EventRepository,
        private val eventTypeRepository: EventTypeRepository,
        private val carRepository: CarRepository,
        private val deleteUtil: DeleteUtil,
        private val mailService: MailService
) {

    /** Logger of this class */
    private val logger = LoggerFactory.getLogger(EventController::class.java)

    /**
     * Method create new event in database.
     * Returned status code can be CREATED, UNAUTHORIZED, BAD_REQUEST
     *
     * Created event is send to mailService where can be send to user via mail.
     *
     * @param principal of actual logged user.
     * @param request for create event.
     * @param response to create event request.
     * @param eventCreate event which will be stored in database.
     * @return Empty String or json with error message on BAD_REQUEST.
     */
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

        if (principal.name == null || car.get().username != principal.name) {
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

        val event = Event(0, eventType.get(), zonedDateTime,
                car.get(), eventCreate.note)
        eventRepository.save(event)
        logger.debug("Created new event")

        mailService.sendEvent(event, principal)

        response.status = HttpServletResponse.SC_CREATED
        return ""
    }

    /**
     * Method update note of given event in database. Id of [eventUpdate] must be set.
     * Logged user must be owner car which is associated with updated event.
     *
     * Returned status code can be CREATED, UNAUTHORIZED, BAD_REQUEST
     *
     * @param principal of actual logged user.
     * @param request for update event.
     * @param response on update event request.
     * @param eventUpdate event which will be updated.
     * @return Empty String or json with error message on BAD_REQUEST.
     */
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
        if (!dbEvent.isPresent) {
            logger.debug("Can not update not existing event")
            response.status = HttpServletResponse.SC_BAD_REQUEST
            return createJsonSingle("error", "Event does not exists")
        }

        if (principal.name == null || dbEvent.get().car.username != principal.name) {
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

    /**
     * Method delete vent in database given by [eventId].
     * Logged user must be owner of car which is associated with deleted event.
     *
     * Returned status code can be OK, UNAUTHORIZED, BAD_REQUEST
     *
     * @param principal of actual logged user.
     * @param request for delete event.
     * @param response on delete event request.
     * @param eventId of event which will be deleted
     * @return Empty String or json with error message on BAD_REQUEST.
     */
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

        if (principal.name == null || event.get().car.username != principal.name) {
            logger.debug("User: ${principal.name} can not get event: $eventId")
            response.status = HttpServletResponse.SC_UNAUTHORIZED
            return ""
        }

        deleteUtil.deleteEvents(listOf(event.get()))
        return ""
    }

    /**
     * Method return requested event from database.
     * Logged user must be owner car which is associated with requested event.
     *
     * Returned status code can be OK, UNAUTHORIZED, BAD_REQUEST
     *
     * @param principal of actual logged user.
     * @param request for car
     * @param response on car request.
     * @param eventId identification number of requested event.
     * @return Json of event from database, json with error message on BAD_REQUEST or empty string on UNAUTHORIZED.
     */
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

        if (principal.name == null || event.get().car.username != principal.name) {
            logger.debug("User: ${principal.name} can not get event: $eventId")
            response.status = HttpServletResponse.SC_UNAUTHORIZED
            return ""
        }

        return Event.gson.toJson(event.get())
    }

    /**
     * Method return events of logged user from database.
     * Returned status code can be OK, UNAUTHORIZED.
     *
     * @param principal of actual logged user.
     * @param request for events of logged users.
     * @param response on event request.
     * @param page of events divided by [limit].
     * @param limit of events per page.
     * @return Json of events from database or empty string on UNAUTHORIZED.
     */
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

    /**
     * Method return events of given car from database.
     * User must be owner of given car.
     *
     * Returned status code can be OK, UNAUTHORIZED, BAD_REQUEST.
     *
     * @param principal of actual logged user.
     * @param request for events of logged users.
     * @param response on event request.
     * @param page of events divided by [limit].
     * @param limit of events per page.
     * @return Json of events from database, json with error message on BAD_REQUEST or empty string on UNAUTHORIZED.
     */
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