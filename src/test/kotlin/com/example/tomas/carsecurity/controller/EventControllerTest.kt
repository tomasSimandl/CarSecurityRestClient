package com.example.tomas.carsecurity.controller


import com.example.tomas.carsecurity.anyKotlin
import com.example.tomas.carsecurity.model.Car
import com.example.tomas.carsecurity.model.Event
import com.example.tomas.carsecurity.model.EventType
import com.example.tomas.carsecurity.model.dto.EventCreate
import com.example.tomas.carsecurity.model.dto.EventUpdate
import com.example.tomas.carsecurity.repository.CarRepository
import com.example.tomas.carsecurity.repository.DeleteUtil
import com.example.tomas.carsecurity.repository.EventRepository
import com.example.tomas.carsecurity.repository.EventTypeRepository
import com.example.tomas.carsecurity.service.MailService
import com.nhaarman.mockitokotlin2.argumentCaptor
import io.grpc.internal.JsonParser
import org.apache.http.auth.BasicUserPrincipal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.mockito.internal.verification.Times
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import java.security.Principal
import java.time.ZonedDateTime
import java.util.*
import javax.servlet.http.HttpServletResponse

class EventControllerTest {

    @Mock
    private lateinit var eventRepository: EventRepository
    @Mock
    private lateinit var eventTypeRepository: EventTypeRepository
    @Mock
    private lateinit var carRepository: CarRepository
    @Mock
    private lateinit var deleteUtil: DeleteUtil
    @Mock
    private lateinit var mailService: MailService

    private lateinit var eventController: EventController

    private lateinit var request: MockHttpServletRequest
    private lateinit var response: MockHttpServletResponse


    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
        request = MockHttpServletRequest()
        response = MockHttpServletResponse()

        eventController = EventController(eventRepository, eventTypeRepository, carRepository, deleteUtil, mailService)
    }

    @Test
    fun `create event success`() {
        val car = Car(123, "Leos Mares", ArrayList(), ArrayList(), "Trabant", "Bakelite car", "")
        val principal = BasicUserPrincipal("Leos Mares")
        val eventType = EventType(10, "type", "desc")
        val eventCreate = EventCreate(10L, 123456789L, 123L, "new event")

        doReturn(Optional.of(car)).`when`(carRepository).findById(123)
        doReturn(Optional.of(eventType)).`when`(eventTypeRepository).findById(10)

        val jsonResponse = eventController.createEvent(principal, request, response, eventCreate)

        val eventCaptor = argumentCaptor<Event>()
        verify(eventRepository).save(eventCaptor.capture())

        assertEquals(eventType, eventCaptor.firstValue.eventType)
        assertEquals(123456L, eventCaptor.firstValue.time.toEpochSecond())
        assertEquals(car, eventCaptor.firstValue.car)
        assertEquals(eventCreate.note, eventCaptor.firstValue.note)

        val mailEventCaptor = argumentCaptor<Event>()
        verify(mailService).sendEvent(mailEventCaptor.capture(), com.nhaarman.mockitokotlin2.eq(principal))

        assertEquals(eventCaptor.firstValue, mailEventCaptor.firstValue)

        assertEquals("", jsonResponse)
        assertEquals(HttpServletResponse.SC_CREATED, response.status)
    }

    @Test
    fun `create event not existing car`() {

        val principal = BasicUserPrincipal("Leos Mares")
        val eventCreate = EventCreate(10L, 123456789L, 123L, "new event")

        val carOptional: Optional<Car> = Optional.empty()

        doReturn(carOptional).`when`(carRepository).findById(123)

        val jsonResponse = eventController.createEvent(principal, request, response, eventCreate)

        verify(eventRepository, Times(0)).save(anyKotlin(Event::class.java))
        verify(mailService, Times(0)).sendEvent(anyKotlin(Event::class.java), anyKotlin(Principal::class.java))

        assertTrue((JsonParser.parse(jsonResponse) as Map<*, *>).containsKey("error"))
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.status)
    }

    @Test
    fun `create event empty principal`() {
        val car = Car(123, "Leos Mares", ArrayList(), ArrayList(), "Trabant", "Bakelite car", "")
        val principal = mock(UsernamePasswordAuthenticationToken::class.java)
        val eventCreate = EventCreate(10L, 123456789L, 123L, "new event")

        doReturn(Optional.of(car)).`when`(carRepository).findById(123)
        doReturn(null).`when`(principal).name

        val jsonResponse = eventController.createEvent(principal, request, response, eventCreate)

        verify(eventRepository, Times(0)).save(anyKotlin(Event::class.java))
        verify(mailService, Times(0)).sendEvent(anyKotlin(Event::class.java), anyKotlin(Principal::class.java))

        assertEquals("", jsonResponse)
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.status)
    }

    @Test
    fun `create event not car owner`() {
        val car = Car(123, "Peter", ArrayList(), ArrayList(), "Trabant", "Bakelite car", "")
        val principal = BasicUserPrincipal("David")
        val eventCreate = EventCreate(10L, 123456789L, 123L, "new event")

        doReturn(Optional.of(car)).`when`(carRepository).findById(123)

        val jsonResponse = eventController.createEvent(principal, request, response, eventCreate)

        verify(eventRepository, Times(0)).save(anyKotlin(Event::class.java))
        verify(mailService, Times(0)).sendEvent(anyKotlin(Event::class.java), anyKotlin(Principal::class.java))

        assertEquals("", jsonResponse)
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.status)
    }

    @Test
    fun `create event invalid event type`() {
        val car = Car(123, "Leos Mares", ArrayList(), ArrayList(), "Trabant", "Bakelite car", "")
        val principal = BasicUserPrincipal("Leos Mares")
        val eventCreate = EventCreate(10L, 123456789L, 123L, "new event")
        val eventTypeOptional: Optional<EventType> = Optional.empty()

        doReturn(Optional.of(car)).`when`(carRepository).findById(123)
        doReturn(eventTypeOptional).`when`(eventTypeRepository).findById(10)

        val jsonResponse = eventController.createEvent(principal, request, response, eventCreate)

        verify(eventRepository, Times(0)).save(anyKotlin(Event::class.java))
        verify(mailService, Times(0)).sendEvent(anyKotlin(Event::class.java), anyKotlin(Principal::class.java))

        assertTrue((JsonParser.parse(jsonResponse) as Map<*, *>).containsKey("error"))
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.status)
    }

    @Test
    fun `update event note success`() {
        val car = Car(123, "Leos Mares", ArrayList(), ArrayList(), "Trabant", "Bakelite car", "")
        val principal = BasicUserPrincipal("Leos Mares")
        val eventType = EventType(10, "type", "desc")
        val eventUpdate = EventUpdate(183, "new super note")
        val time = ZonedDateTime.now()
        val event = Event(183, eventType, time, car, "Old note")

        doReturn(Optional.of(event)).`when`(eventRepository).findById(event.id)

        val jsonResponse = eventController.updateEventNote(principal, request, response, eventUpdate)

        val eventCaptor = argumentCaptor<Event>()
        verify(eventRepository).save(eventCaptor.capture())

        assertEquals(eventType, eventCaptor.firstValue.eventType)
        assertEquals(time, eventCaptor.firstValue.time)
        assertEquals(car, eventCaptor.firstValue.car)
        assertEquals(eventUpdate.note, eventCaptor.firstValue.note)

        assertEquals("", jsonResponse)
        assertEquals(HttpServletResponse.SC_CREATED, response.status)
    }

    @Test
    fun `update event note invalid event`() {
        val principal = BasicUserPrincipal("Leos Mares")
        val eventUpdate = EventUpdate(183, "new super note")
        val eventOptional: Optional<Event> = Optional.empty()

        doReturn(eventOptional).`when`(eventRepository).findById(eventUpdate.id)

        val jsonResponse = eventController.updateEventNote(principal, request, response, eventUpdate)

        verify(eventRepository, Times(0)).save(anyKotlin(Event::class.java))
        assertTrue((JsonParser.parse(jsonResponse) as Map<*, *>).containsKey("error"))
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.status)
    }

    @Test
    fun `update event note null principal`() {
        val car = Car(123, "Leos Mares", ArrayList(), ArrayList(), "Trabant", "Bakelite car", "")
        val principal = mock(UsernamePasswordAuthenticationToken::class.java)
        val eventType = EventType(10, "type", "desc")
        val eventUpdate = EventUpdate(183, "new super note")
        val time = ZonedDateTime.now()
        val event = Event(183, eventType, time, car, "Old note")

        doReturn(null).`when`(principal).name
        doReturn(Optional.of(event)).`when`(eventRepository).findById(event.id)

        val jsonResponse = eventController.updateEventNote(principal, request, response, eventUpdate)

        verify(eventRepository, Times(0)).save(anyKotlin(Event::class.java))
        assertEquals("", jsonResponse)
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.status)
    }

    @Test
    fun `update event note not owner`() {
        val car = Car(123, "Franta", ArrayList(), ArrayList(), "Trabant", "Bakelite car", "")
        val principal = BasicUserPrincipal("Leos Mares")
        val eventType = EventType(10, "type", "desc")
        val eventUpdate = EventUpdate(183, "new super note")
        val time = ZonedDateTime.now()
        val event = Event(183, eventType, time, car, "Old note")

        doReturn(Optional.of(event)).`when`(eventRepository).findById(event.id)

        val jsonResponse = eventController.updateEventNote(principal, request, response, eventUpdate)

        verify(eventRepository, Times(0)).save(anyKotlin(Event::class.java))
        assertEquals("", jsonResponse)
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.status)
    }

    @Test
    fun `delete event success`() {
        val car = Car(123, "Leos Mares", ArrayList(), ArrayList(), "Trabant", "Bakelite car", "")
        val principal = BasicUserPrincipal("Leos Mares")
        val eventType = EventType(10, "type", "desc")
        val time = ZonedDateTime.now()
        val event = Event(183, eventType, time, car, "Old note")

        doReturn(Optional.of(event)).`when`(eventRepository).findById(event.id)

        val jsonResponse = eventController.deleteEvent(principal, request, response, event.id)

        val eventCaptor = argumentCaptor<List<Event>>()
        verify(deleteUtil).deleteEvents(eventCaptor.capture())

        assertEquals(1, eventCaptor.firstValue.size)
        assertEquals(event, eventCaptor.firstValue.first())

        assertEquals("", jsonResponse)
        assertEquals(HttpServletResponse.SC_OK, response.status)
    }

    @Test
    fun `delete event invalid event id`() {
        val principal = BasicUserPrincipal("Leos Mares")

        val eventOptional: Optional<Event> = Optional.empty()
        doReturn(eventOptional).`when`(eventRepository).findById(anyKotlin(Long::class.java))

        val jsonResponse = eventController.deleteEvent(principal, request, response, 12)

        verify(deleteUtil, Times(0)).deleteEvents(com.nhaarman.mockitokotlin2.any())

        assertTrue((JsonParser.parse(jsonResponse) as Map<*, *>).containsKey("error"))
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.status)
    }

    @Test
    fun `delete event null principal`() {
        val car = Car(123, "Leos Mares", ArrayList(), ArrayList(), "Trabant", "Bakelite car", "")
        val principal = mock(UsernamePasswordAuthenticationToken::class.java)
        val eventType = EventType(10, "type", "desc")
        val time = ZonedDateTime.now()
        val event = Event(183, eventType, time, car, "Old note")


        doReturn(null).`when`(principal).name
        doReturn(Optional.of(event)).`when`(eventRepository).findById(event.id)

        val jsonResponse = eventController.deleteEvent(principal, request, response, event.id)

        verify(deleteUtil, Times(0)).deleteEvents(com.nhaarman.mockitokotlin2.any())

        assertEquals("", jsonResponse)
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.status)
    }

    @Test
    fun `delete event not owner`() {
        val car = Car(123, "Pepek namornik", ArrayList(), ArrayList(), "Trabant", "Bakelite car", "")
        val principal = BasicUserPrincipal("Leos Mares")
        val eventType = EventType(10, "type", "desc")
        val time = ZonedDateTime.now()
        val event = Event(183, eventType, time, car, "Old note")

        doReturn(Optional.of(event)).`when`(eventRepository).findById(event.id)

        val jsonResponse = eventController.deleteEvent(principal, request, response, event.id)

        verify(deleteUtil, Times(0)).deleteEvents(com.nhaarman.mockitokotlin2.any())

        assertEquals("", jsonResponse)
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.status)
    }

    @Test
    fun `get event success`() {
        val car = Car(123, "Leos Mares", ArrayList(), ArrayList(), "Trabant", "Bakelite car", "")
        val principal = BasicUserPrincipal("Leos Mares")
        val eventType = EventType(10, "type", "desc")
        val time = ZonedDateTime.now()
        val event = Event(183, eventType, time, car, "Old note")

        doReturn(Optional.of(event)).`when`(eventRepository).findById(event.id)

        val jsonResponse = eventController.getEvent(principal, request, response, event.id)

        assertEquals(Event.gson.toJson(event), jsonResponse)
        assertEquals(HttpServletResponse.SC_OK, response.status)
    }

    @Test
    fun `get event invalid event id`() {
        val principal = BasicUserPrincipal("Leos Mares")

        val eventOptional: Optional<Event> = Optional.empty()
        doReturn(eventOptional).`when`(eventRepository).findById(anyKotlin(Long::class.java))

        val jsonResponse = eventController.getEvent(principal, request, response, 12)

        assertTrue((JsonParser.parse(jsonResponse) as Map<*, *>).containsKey("error"))
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.status)
    }

    @Test
    fun `get event null principal`() {
        val car = Car(123, "Leos Mares", ArrayList(), ArrayList(), "Trabant", "Bakelite car", "")
        val principal = mock(UsernamePasswordAuthenticationToken::class.java)
        val eventType = EventType(10, "type", "desc")
        val time = ZonedDateTime.now()
        val event = Event(183, eventType, time, car, "Old note")

        doReturn(null).`when`(principal).name
        doReturn(Optional.of(event)).`when`(eventRepository).findById(event.id)

        val jsonResponse = eventController.getEvent(principal, request, response, event.id)

        assertEquals("", jsonResponse)
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.status)
    }

    @Test
    fun `get event not owner`() {
        val car = Car(123, "Pepek namornik", ArrayList(), ArrayList(), "Trabant", "Bakelite car", "")
        val principal = BasicUserPrincipal("Leos Mares")
        val eventType = EventType(10, "type", "desc")
        val time = ZonedDateTime.now()
        val event = Event(183, eventType, time, car, "Old note")

        doReturn(Optional.of(event)).`when`(eventRepository).findById(event.id)

        val jsonResponse = eventController.getEvent(principal, request, response, event.id)

        assertEquals("", jsonResponse)
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.status)
    }

    @Test
    fun `get event of log user success`() {
        val car = Car(123, "Leos Mares", ArrayList(), ArrayList(), "Trabant", "Bakelite car", "")
        val principal = BasicUserPrincipal("Leos Mares")
        val eventType = EventType(10, "type", "desc")
        val event1 = Event(182, eventType, ZonedDateTime.now(), car, "Old note")
        val event2 = Event(183, eventType, ZonedDateTime.now(), car, "Old note")
        val events = listOf(event1, event2)

        val pageableCaptor = argumentCaptor<Pageable>()
        doReturn(PageImpl(events)).`when`(eventRepository).findAllByCar_UsernameOrderByTimeDesc(com.nhaarman.mockitokotlin2.eq("Leos Mares"), pageableCaptor.capture())

        val jsonResponse = eventController.getEventsOfLogUser(principal, request, response, 0, 8)

        assertEquals(0, pageableCaptor.firstValue.pageNumber)
        assertEquals(8, pageableCaptor.firstValue.pageSize)
        assertEquals(Event.gson.toJson(events), jsonResponse)
        assertEquals(HttpServletResponse.SC_OK, response.status)
    }

    @Test
    fun `get event of log user null principal`() {
        val principal = mock(UsernamePasswordAuthenticationToken::class.java)

        doReturn(null).`when`(principal).name

        val jsonResponse = eventController.getEventsOfLogUser(principal, request, response, 0, 8)

        verify(eventRepository, Times(0)).findAllByCar_UsernameOrderByTimeDesc(anyKotlin(String::class.java), com.nhaarman.mockitokotlin2.any())

        assertEquals("", jsonResponse)
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.status)
    }

    @Test
    fun `get event of log user invalid limit -1`() {
        val car = Car(123, "Leos Mares", ArrayList(), ArrayList(), "Trabant", "Bakelite car", "")
        val principal = BasicUserPrincipal("Leos Mares")
        val eventType = EventType(10, "type", "desc")
        val event1 = Event(182, eventType, ZonedDateTime.now(), car, "Old note")
        val event2 = Event(183, eventType, ZonedDateTime.now(), car, "Old note")
        val events = listOf(event1, event2)

        val pageableCaptor = argumentCaptor<Pageable>()
        doReturn(PageImpl(events)).`when`(eventRepository).findAllByCar_UsernameOrderByTimeDesc(com.nhaarman.mockitokotlin2.eq("Leos Mares"), pageableCaptor.capture())

        val jsonResponse = eventController.getEventsOfLogUser(principal, request, response, 0, -1)

        assertEquals(0, pageableCaptor.firstValue.pageNumber)
        assertEquals(1, pageableCaptor.firstValue.pageSize)
        assertEquals(Event.gson.toJson(events), jsonResponse)
        assertEquals(HttpServletResponse.SC_OK, response.status)
    }

    @Test
    fun `get event of log user invalid limit 0`() {
        val car = Car(123, "Leos Mares", ArrayList(), ArrayList(), "Trabant", "Bakelite car", "")
        val principal = BasicUserPrincipal("Leos Mares")
        val eventType = EventType(10, "type", "desc")
        val event1 = Event(182, eventType, ZonedDateTime.now(), car, "Old note")
        val event2 = Event(183, eventType, ZonedDateTime.now(), car, "Old note")
        val events = listOf(event1, event2)

        val pageableCaptor = argumentCaptor<Pageable>()
        doReturn(PageImpl(events)).`when`(eventRepository).findAllByCar_UsernameOrderByTimeDesc(com.nhaarman.mockitokotlin2.eq("Leos Mares"), pageableCaptor.capture())

        val jsonResponse = eventController.getEventsOfLogUser(principal, request, response, 0, 0)

        assertEquals(0, pageableCaptor.firstValue.pageNumber)
        assertEquals(1, pageableCaptor.firstValue.pageSize)
        assertEquals(Event.gson.toJson(events), jsonResponse)
        assertEquals(HttpServletResponse.SC_OK, response.status)
    }

    @Test
    fun `get events success`() {
        val car = Car(123, "Leos Mares", ArrayList(), ArrayList(), "Trabant", "Bakelite car", "")
        val principal = BasicUserPrincipal("Leos Mares")
        val eventType = EventType(10, "type", "desc")
        val event1 = Event(182, eventType, ZonedDateTime.now(), car, "Old note")
        val event2 = Event(183, eventType, ZonedDateTime.now(), car, "Old note")
        val events = listOf(event1, event2)

        val pageableCaptor = argumentCaptor<Pageable>()
        doReturn(PageImpl(events)).`when`(eventRepository).findAllByCarId(com.nhaarman.mockitokotlin2.eq(123), pageableCaptor.capture())
        doReturn(Optional.of(car)).`when`(carRepository).findById(123)

        val jsonResponse = eventController.getEvents(principal, request, response, 123, 1, 3)

        assertEquals(1, pageableCaptor.firstValue.pageNumber)
        assertEquals(3, pageableCaptor.firstValue.pageSize)
        assertEquals(Event.gson.toJson(events), jsonResponse)
        assertEquals(HttpServletResponse.SC_OK, response.status)
    }

    @Test
    fun `get events invalid car id`() {

        val principal = BasicUserPrincipal("Leos Mares")
        val carOptional: Optional<Car> = Optional.empty()

        doReturn(carOptional).`when`(carRepository).findById(123)

        val jsonResponse = eventController.getEvents(principal, request, response, 123, 1, 3)

        verify(eventRepository, Times(0)).findAllByCarId(com.nhaarman.mockitokotlin2.any(), com.nhaarman.mockitokotlin2.any())
        assertTrue((JsonParser.parse(jsonResponse) as Map<*, *>).containsKey("error"))
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.status)
    }

    @Test
    fun `get events null principal`() {
        val car = Car(123, "Leos Mares", ArrayList(), ArrayList(), "Trabant", "Bakelite car", "")
        val principal = mock(UsernamePasswordAuthenticationToken::class.java)

        doReturn(null).`when`(principal).name
        doReturn(Optional.of(car)).`when`(carRepository).findById(123)

        val jsonResponse = eventController.getEvents(principal, request, response, 123, 1, 3)

        verify(eventRepository, Times(0)).findAllByCarId(com.nhaarman.mockitokotlin2.any(), com.nhaarman.mockitokotlin2.any())
        assertEquals("", jsonResponse)
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.status)
    }

    @Test
    fun `get events not owner`() {
        val car = Car(123, "Leos Mares", ArrayList(), ArrayList(), "Trabant", "Bakelite car", "")
        val principal = BasicUserPrincipal("Michal David")

        doReturn(Optional.of(car)).`when`(carRepository).findById(123)

        val jsonResponse = eventController.getEvents(principal, request, response, 123, 1, 3)

        verify(eventRepository, Times(0)).findAllByCarId(com.nhaarman.mockitokotlin2.any(), com.nhaarman.mockitokotlin2.any())
        assertEquals("", jsonResponse)
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.status)
    }

    @Test
    fun `get events invalid limit -2`() {
        val car = Car(123, "Leos Mares", ArrayList(), ArrayList(), "Trabant", "Bakelite car", "")
        val principal = BasicUserPrincipal("Leos Mares")
        val eventType = EventType(10, "type", "desc")
        val event1 = Event(182, eventType, ZonedDateTime.now(), car, "Old note")
        val event2 = Event(183, eventType, ZonedDateTime.now(), car, "Old note")
        val events = listOf(event1, event2)

        val pageableCaptor = argumentCaptor<Pageable>()
        doReturn(PageImpl(events)).`when`(eventRepository).findAllByCarId(com.nhaarman.mockitokotlin2.eq(123), pageableCaptor.capture())
        doReturn(Optional.of(car)).`when`(carRepository).findById(123)

        val jsonResponse = eventController.getEvents(principal, request, response, 123, 1, -2)

        assertEquals(1, pageableCaptor.firstValue.pageNumber)
        assertEquals(1, pageableCaptor.firstValue.pageSize)
        assertEquals(Event.gson.toJson(events), jsonResponse)
        assertEquals(HttpServletResponse.SC_OK, response.status)
    }

    @Test
    fun `get events invalid limit 0`() {
        val car = Car(123, "Leos Mares", ArrayList(), ArrayList(), "Trabant", "Bakelite car", "")
        val principal = BasicUserPrincipal("Leos Mares")
        val eventType = EventType(10, "type", "desc")
        val event1 = Event(182, eventType, ZonedDateTime.now(), car, "Old note")
        val event2 = Event(183, eventType, ZonedDateTime.now(), car, "Old note")
        val events = listOf(event1, event2)

        val pageableCaptor = argumentCaptor<Pageable>()
        doReturn(PageImpl(events)).`when`(eventRepository).findAllByCarId(com.nhaarman.mockitokotlin2.eq(123), pageableCaptor.capture())
        doReturn(Optional.of(car)).`when`(carRepository).findById(123)

        val jsonResponse = eventController.getEvents(principal, request, response, 123, 1, 0)

        assertEquals(1, pageableCaptor.firstValue.pageNumber)
        assertEquals(1, pageableCaptor.firstValue.pageSize)
        assertEquals(Event.gson.toJson(events), jsonResponse)
        assertEquals(HttpServletResponse.SC_OK, response.status)
    }
}