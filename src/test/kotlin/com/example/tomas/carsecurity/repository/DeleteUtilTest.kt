package com.example.tomas.carsecurity.repository


import com.example.tomas.carsecurity.anyKotlin
import com.example.tomas.carsecurity.model.*
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.*
import org.mockito.Mockito.*
import org.mockito.internal.verification.Times
import java.time.ZonedDateTime

class DeleteUtilTest {

    @Mock
    private lateinit var carRepository: CarRepository
    @Mock
    private lateinit var eventRepository: EventRepository
    @Mock
    private lateinit var routeRepository: RouteRepository
    @Mock
    private lateinit var positionRepository: PositionRepository

    @InjectMocks
    private lateinit var deleteUtil: DeleteUtil

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
    }

    @Test
    fun deleteEventsTest() {

        val event1 = Event(eventType = EventType(), time = ZonedDateTime.now(), car = Car(username = "John"))
        val event2 = Event(eventType = EventType(), time = ZonedDateTime.now(), car = Car(username = "Arya"))
        val events = listOf(event1, event2)

        deleteUtil.deleteEvents(events)

        val eventCaptor = ArgumentCaptor.forClass(events::class.java)
        verify(eventRepository, Times(1)).deleteAll(eventCaptor.capture())
        assertEquals(events, eventCaptor.value)
    }

    @Test
    fun deleteRoutesTest() {

        val routes = listOf(Route(time = ZonedDateTime.now(), car = Car(username = "John")))

        val position1 = Position(route = routes.first(), time = ZonedDateTime.now())
        val position2 = Position(route = routes.first(), time = ZonedDateTime.now())
        val position3 = Position(route = routes.first(), time = ZonedDateTime.now())
        val positions = listOf(position1, position2, position3)

        doReturn(positions).`when`(positionRepository).findAllByRouteIn(anyKotlin(routes::class.java))

        deleteUtil.deleteRoutes(routes)

        verify(positionRepository, Times(1)).findAllByRouteIn(routes)

        val positionCaptor = ArgumentCaptor.forClass(positions::class.java)
        verify(positionRepository, Times(1)).deleteAll(positionCaptor.capture())
        assertEquals(positions, positionCaptor.value)

        val routeCaptor = ArgumentCaptor.forClass(routes::class.java)
        verify(routeRepository, Times(1)).deleteAll(routeCaptor.capture())
        assertEquals(routes, routeCaptor.value)
    }

    @Test
    fun deleteCarsTest() {

        val car1 = Car(username = "John")
        val car2 = Car(username = "Arya")
        val cars = listOf(car1, car2)

        val event1 = Event(eventType = EventType(), time = ZonedDateTime.now(), car = car1)
        val event2 = Event(eventType = EventType(), time = ZonedDateTime.now(), car = car2)
        val events = listOf(event1, event2)

        val routes = listOf(Route(time = ZonedDateTime.now(), car = car1))

        val position1 = Position(route = routes.first(), time = ZonedDateTime.now())
        val position2 = Position(route = routes.first(), time = ZonedDateTime.now())
        val position3 = Position(route = routes.first(), time = ZonedDateTime.now())
        val positions = listOf(position1, position2, position3)

        doReturn(events).`when`(eventRepository).findAllByCarIn(cars)
        doReturn(routes).`when`(routeRepository).findAllByCarInAndPositionsIsNotNull(cars)
        doReturn(positions).`when`(positionRepository).findAllByRouteIn(routes)

        deleteUtil.deleteCars(cars)

        val inOrder = inOrder(eventRepository, routeRepository, positionRepository, carRepository)

        inOrder.verify(eventRepository, Times(1)).findAllByCarIn(cars)
        inOrder.verify(eventRepository, Times(1)).deleteAll(events)
        inOrder.verify(routeRepository, Times(1)).findAllByCarInAndPositionsIsNotNull(cars)
        inOrder.verify(positionRepository, Times(1)).findAllByRouteIn(routes)
        inOrder.verify(positionRepository, Times(1)).deleteAll(positions)
        inOrder.verify(routeRepository, Times(1)).deleteAll(routes)
        inOrder.verify(carRepository, Times(1)).deleteAll(cars)
    }
}