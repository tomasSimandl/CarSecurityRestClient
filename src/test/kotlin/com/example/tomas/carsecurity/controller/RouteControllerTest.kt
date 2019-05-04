package com.example.tomas.carsecurity.controller


import com.example.tomas.carsecurity.model.Car
import com.example.tomas.carsecurity.model.Position
import com.example.tomas.carsecurity.model.Route
import com.example.tomas.carsecurity.model.dto.RouteUpdate
import com.example.tomas.carsecurity.repository.CarRepository
import com.example.tomas.carsecurity.repository.DeleteUtil
import com.example.tomas.carsecurity.repository.PositionRepository
import com.example.tomas.carsecurity.repository.RouteRepository
import com.nhaarman.mockitokotlin2.argumentCaptor
import io.grpc.internal.JsonParser
import org.apache.http.auth.BasicUserPrincipal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.mockito.internal.verification.Times
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.*
import javax.servlet.http.HttpServletResponse

class RouteControllerTest {

    @Mock
    private lateinit var routeRepository: RouteRepository
    @Mock
    private lateinit var positionRepository: PositionRepository
    @Mock
    private lateinit var carRepository: CarRepository
    @Mock
    private lateinit var deleteUtil: DeleteUtil


    private lateinit var routeController: RouteController

    private lateinit var request: MockHttpServletRequest
    private lateinit var response: MockHttpServletResponse

    private val positions = ArrayList<Position>()
    private val car = Car(id = 48, username = "Emanuel")
    private val route = Route(id = 823, time = ZonedDateTime.now(), car = car, positions = positions)


    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
        request = MockHttpServletRequest()
        response = MockHttpServletResponse()

        routeController = RouteController(routeRepository, positionRepository, carRepository, deleteUtil)

        val positionTime1 = ZonedDateTime.of(1111, 11, 11, 11, 11, 11, 0, ZoneOffset.UTC)
        val positionTime2 = ZonedDateTime.of(2222, 2, 22, 22, 22, 22, 0, ZoneOffset.UTC)
        val positionTime3 = ZonedDateTime.of(3333, 3, 3, 3, 33, 33, 0, ZoneOffset.UTC)

        positions.add(Position(1, route, 1f, 2f, 3f, positionTime1, 4f, 5f, 6f))
        positions.add(Position(2, route, 7f, 8f, 9f, positionTime2, 10f, 11f, 12f))
        positions.add(Position(3, route, 13f, 14f, 15f, positionTime3, 16f, 17f, 18f))
    }

    @Test
    fun `create route success`() {

        val time = ZonedDateTime.of(1234, 5, 6, 7, 8, 9, 0, ZoneOffset.UTC)
        val principal = BasicUserPrincipal("Emanuel")

        val routeCaptor = argumentCaptor<Route>()
        doReturn(Optional.of(car)).`when`(carRepository).findById(car.id)
        doReturn(route).`when`(routeRepository).save(routeCaptor.capture())

        val jsonResponse = routeController.createRoute(principal, request, response, car.id, time.toEpochSecond() * 1000)

        assertEquals(time, routeCaptor.firstValue.time)
        assertEquals(car, routeCaptor.firstValue.car)
        assertTrue(routeCaptor.firstValue.positions.isEmpty())

        val mapResult = JsonParser.parse(jsonResponse) as Map<*, *>
        assertTrue(mapResult.containsKey("route_id"))
        assertEquals("${route.id}", mapResult["route_id"])
        assertEquals(HttpServletResponse.SC_CREATED, response.status)
    }

    @Test
    fun `create route invalid car`() {

        val principal = BasicUserPrincipal("Emanuel")
        val carOptional: Optional<Car> = Optional.empty()

        doReturn(carOptional).`when`(carRepository).findById(car.id)

        val jsonResponse = routeController.createRoute(principal, request, response, car.id, 1000L)

        verify(routeRepository, Times(0)).save(ArgumentMatchers.any())
        assertTrue((JsonParser.parse(jsonResponse) as Map<*, *>).containsKey("error"))
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.status)
    }

    @Test
    fun `create route null principal`() {

        val principal = mock(UsernamePasswordAuthenticationToken::class.java)

        doReturn(null).`when`(principal).name
        doReturn(Optional.of(car)).`when`(carRepository).findById(car.id)

        val jsonResponse = routeController.createRoute(principal, request, response, car.id, 1000L)

        verify(routeRepository, Times(0)).save(ArgumentMatchers.any())
        assertEquals("", jsonResponse)
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.status)
    }

    @Test
    fun `create route principal not car owner`() {

        val principal = BasicUserPrincipal("Steel")

        doReturn(Optional.of(car)).`when`(carRepository).findById(car.id)

        val jsonResponse = routeController.createRoute(principal, request, response, car.id, 1000L)

        verify(routeRepository, Times(0)).save(ArgumentMatchers.any())
        assertEquals("", jsonResponse)
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.status)
    }

    @Test
    fun `update routes note success`() {

        val principal = BasicUserPrincipal("Emanuel")
        val routeUpdate = RouteUpdate(route.id, "Cool note.")

        doReturn(Optional.of(route)).`when`(routeRepository).findById(route.id)

        val jsonResponse = routeController.updateRoutesNote(principal, request, response, routeUpdate)

        val routeCaptor = argumentCaptor<Route>()
        verify(routeRepository).save(routeCaptor.capture())

        assertEquals(route.id, routeCaptor.firstValue.id)
        assertEquals(route.car, routeCaptor.firstValue.car)
        assertEquals(route.time, routeCaptor.firstValue.time)
        assertEquals("Cool note.", routeCaptor.firstValue.note)

        assertEquals("", jsonResponse)
        assertEquals(HttpServletResponse.SC_OK, response.status)
    }

    @Test
    fun `update routes note invalid route id`() {

        val principal = BasicUserPrincipal("Emanuel")
        val routeUpdate = RouteUpdate(5, "Cool note.")

        val routeOptional: Optional<Route> = Optional.empty()
        doReturn(routeOptional).`when`(routeRepository).findById(route.id)

        val jsonResponse = routeController.updateRoutesNote(principal, request, response, routeUpdate)

        verify(routeRepository, Times(0)).save(ArgumentMatchers.any())
        assertTrue((JsonParser.parse(jsonResponse) as Map<*, *>).containsKey("error"))
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.status)
    }

    @Test
    fun `update routes note null principal`() {

        val principal = mock(UsernamePasswordAuthenticationToken::class.java)
        val routeUpdate = RouteUpdate(route.id, "Cool note.")

        doReturn(null).`when`(principal).name
        doReturn(Optional.of(route)).`when`(routeRepository).findById(route.id)

        val jsonResponse = routeController.updateRoutesNote(principal, request, response, routeUpdate)

        verify(routeRepository, Times(0)).save(ArgumentMatchers.any())
        assertEquals("", jsonResponse)
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.status)
    }

    @Test
    fun `update routes note not owner of route`() {

        val principal = BasicUserPrincipal("František")
        val routeUpdate = RouteUpdate(route.id, "Cool note.")

        doReturn(Optional.of(route)).`when`(routeRepository).findById(route.id)

        val jsonResponse = routeController.updateRoutesNote(principal, request, response, routeUpdate)

        verify(routeRepository, Times(0)).save(ArgumentMatchers.any())
        assertEquals("", jsonResponse)
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.status)
    }

    @Test
    fun `delete route by id success`() {

        val principal = BasicUserPrincipal("Emanuel")

        doReturn(Optional.of(route)).`when`(routeRepository).findById(route.id)

        val jsonResponse = routeController.deleteRouteById(principal, request, response, route.id)

        val routeCaptor = argumentCaptor<List<Route>>()
        verify(deleteUtil).deleteRoutes(routeCaptor.capture())
        assertEquals(1, routeCaptor.firstValue.size)
        assertEquals(route, routeCaptor.firstValue.first())
        assertEquals("", jsonResponse)
        assertEquals(HttpServletResponse.SC_OK, response.status)
    }

    @Test
    fun `delete route by id invalid route id`() {

        val principal = BasicUserPrincipal("Emanuel")
        val routeOptional: Optional<Route> = Optional.empty()

        doReturn(routeOptional).`when`(routeRepository).findById(route.id)

        val jsonResponse = routeController.deleteRouteById(principal, request, response, route.id)

        verify(deleteUtil, Times(0)).deleteRoutes(com.nhaarman.mockitokotlin2.any())
        assertTrue((JsonParser.parse(jsonResponse) as Map<*, *>).containsKey("error"))
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.status)
    }

    @Test
    fun `delete route by id null principal`() {

        val principal = mock(UsernamePasswordAuthenticationToken::class.java)

        doReturn(null).`when`(principal).name
        doReturn(Optional.of(route)).`when`(routeRepository).findById(route.id)

        val jsonResponse = routeController.deleteRouteById(principal, request, response, route.id)

        verify(deleteUtil, Times(0)).deleteRoutes(com.nhaarman.mockitokotlin2.any())
        assertEquals("", jsonResponse)
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.status)
    }

    @Test
    fun `delete route by id not owner of route`() {

        val principal = BasicUserPrincipal("Paul")

        doReturn(Optional.of(route)).`when`(routeRepository).findById(route.id)

        val jsonResponse = routeController.deleteRouteById(principal, request, response, route.id)

        verify(deleteUtil, Times(0)).deleteRoutes(com.nhaarman.mockitokotlin2.any())
        assertEquals("", jsonResponse)
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.status)
    }

    @Test
    fun `get routes of log user success`() {
        val principal = BasicUserPrincipal("Paul")
        val route2 = Route(id = 32, car = car, time = ZonedDateTime.now())
        val routes = listOf(route, route2)

        val pageableCaptor = argumentCaptor<Pageable>()
        doReturn(PageImpl(routes)).`when`(routeRepository).findDistinctByCar_UsernameAndPositionsIsNotNullOrderByTimeDesc(
                com.nhaarman.mockitokotlin2.eq("Paul"), pageableCaptor.capture())

        doReturn(Optional.of(1f)).`when`(positionRepository).avgSpeedOfRoute(route.id)
        doReturn(Optional.of(2f)).`when`(positionRepository).sumDistanceOfRoute(route.id)
        doReturn(Optional.of(3)).`when`(positionRepository).travelTimeOfRoute(route.id)
        doReturn(Optional.of(4f)).`when`(positionRepository).avgSpeedOfRoute(route2.id)
        doReturn(Optional.of(5f)).`when`(positionRepository).sumDistanceOfRoute(route2.id)
        doReturn(Optional.of(6)).`when`(positionRepository).travelTimeOfRoute(route2.id)

        val jsonResponse = routeController.getRoutesOfLogUser(principal, request, response, 6, 99)

        assertEquals(99, pageableCaptor.firstValue.pageSize)
        assertEquals(6, pageableCaptor.firstValue.pageNumber)

        val jsonExpected = Route.gson.toJson(listOf(
                route.copy(avgSpeed = 1f, length = 2f, secondsOfTravel = 3),
                route2.copy(avgSpeed = 4f, length = 5f, secondsOfTravel = 6)))

        assertEquals(jsonExpected, jsonResponse)
        assertEquals(HttpServletResponse.SC_OK, response.status)
    }

    @Test
    fun `get routes of log user not update statistics`() {
        val principal = BasicUserPrincipal("Paul")
        val route2 = Route(id = 32, car = car, time = ZonedDateTime.now())
        val routes = listOf(route, route2)

        val pageableCaptor = argumentCaptor<Pageable>()
        doReturn(PageImpl(routes)).`when`(routeRepository).findDistinctByCar_UsernameAndPositionsIsNotNullOrderByTimeDesc(
                com.nhaarman.mockitokotlin2.eq("Paul"), pageableCaptor.capture())

        val floatOptional: Optional<Float> = Optional.empty()
        doReturn(floatOptional).`when`(positionRepository).avgSpeedOfRoute(com.nhaarman.mockitokotlin2.any())

        val jsonResponse = routeController.getRoutesOfLogUser(principal, request, response, 6, 99)

        assertEquals(99, pageableCaptor.firstValue.pageSize)
        assertEquals(6, pageableCaptor.firstValue.pageNumber)

        val jsonExpected = Route.gson.toJson(routes)

        assertEquals(jsonExpected, jsonResponse)
        assertEquals(HttpServletResponse.SC_OK, response.status)
    }

    @Test
    fun `get routes of log user null principal`() {
        val principal = mock(UsernamePasswordAuthenticationToken::class.java)
        doReturn(null).`when`(principal).name

        val jsonResponse = routeController.getRoutesOfLogUser(principal, request, response, 6, 99)

        verify(routeRepository, Times(0)).findDistinctByCar_UsernameAndPositionsIsNotNullOrderByTimeDesc(com.nhaarman.mockitokotlin2.any(), com.nhaarman.mockitokotlin2.any())
        assertEquals("", jsonResponse)
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.status)
    }

    @Test
    fun `get routes of log user invalid limit -3`() {
        val principal = BasicUserPrincipal("Paul")
        val route2 = Route(id = 32, car = car, time = ZonedDateTime.now())
        val routes = listOf(route, route2)

        val pageableCaptor = argumentCaptor<Pageable>()
        doReturn(PageImpl(routes)).`when`(routeRepository).findDistinctByCar_UsernameAndPositionsIsNotNullOrderByTimeDesc(
                com.nhaarman.mockitokotlin2.eq("Paul"), pageableCaptor.capture())

        val floatOptional: Optional<Float> = Optional.empty()
        doReturn(floatOptional).`when`(positionRepository).avgSpeedOfRoute(com.nhaarman.mockitokotlin2.any())

        val jsonResponse = routeController.getRoutesOfLogUser(principal, request, response, 6, -3)

        assertEquals(1, pageableCaptor.firstValue.pageSize)
        assertEquals(6, pageableCaptor.firstValue.pageNumber)

        val jsonExpected = Route.gson.toJson(routes)

        assertEquals(jsonExpected, jsonResponse)
        assertEquals(HttpServletResponse.SC_OK, response.status)
    }

    @Test
    fun `get routes of log user invalid limit 0`() {
        val principal = BasicUserPrincipal("Paul")
        val route2 = Route(id = 32, car = car, time = ZonedDateTime.now())
        val routes = listOf(route, route2)

        val pageableCaptor = argumentCaptor<Pageable>()
        doReturn(PageImpl(routes)).`when`(routeRepository).findDistinctByCar_UsernameAndPositionsIsNotNullOrderByTimeDesc(
                com.nhaarman.mockitokotlin2.eq("Paul"), pageableCaptor.capture())

        val floatOptional: Optional<Float> = Optional.empty()
        doReturn(floatOptional).`when`(positionRepository).avgSpeedOfRoute(com.nhaarman.mockitokotlin2.any())

        val jsonResponse = routeController.getRoutesOfLogUser(principal, request, response, 6, 0)

        assertEquals(1, pageableCaptor.firstValue.pageSize)
        assertEquals(6, pageableCaptor.firstValue.pageNumber)

        val jsonExpected = Route.gson.toJson(routes)

        assertEquals(jsonExpected, jsonResponse)
        assertEquals(HttpServletResponse.SC_OK, response.status)
    }

    @Test
    fun `get route by id success`() {
        val principal = BasicUserPrincipal("Emanuel")
        doReturn(Optional.of(route)).`when`(routeRepository).findById(route.id)
        doReturn(Optional.of(1f)).`when`(positionRepository).avgSpeedOfRoute(route.id)
        doReturn(Optional.of(2f)).`when`(positionRepository).sumDistanceOfRoute(route.id)
        doReturn(Optional.of(3)).`when`(positionRepository).travelTimeOfRoute(route.id)

        val jsonResult = routeController.getRouteById(principal, request, response, route.id)

        val jsonExpected = Route.gson.toJson(route.copy(avgSpeed = 1f, length = 2f, secondsOfTravel = 3))
        assertEquals(jsonExpected, jsonResult)
        assertEquals(HttpServletResponse.SC_OK, response.status)
    }

    @Test
    fun `get route by id invalid route id`() {
        val principal = BasicUserPrincipal("Emanuel")
        val routeOptional: Optional<Route> = Optional.empty()
        doReturn(routeOptional).`when`(routeRepository).findById(123456)

        val jsonResult = routeController.getRouteById(principal, request, response, 123456)

        assertTrue((JsonParser.parse(jsonResult) as Map<*, *>).containsKey("error"))
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.status)
    }

    @Test
    fun `get route by id null principal`() {
        val principal = mock(UsernamePasswordAuthenticationToken::class.java)
        doReturn(null).`when`(principal).name
        doReturn(Optional.of(route)).`when`(routeRepository).findById(route.id)

        val jsonResult = routeController.getRouteById(principal, request, response, route.id)

        assertEquals("", jsonResult)
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.status)
    }

    @Test
    fun `get route by id not owner of route`() {
        val principal = BasicUserPrincipal("Aladin")
        doReturn(Optional.of(route)).`when`(routeRepository).findById(route.id)

        val jsonResult = routeController.getRouteById(principal, request, response, route.id)

        assertEquals("", jsonResult)
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.status)
    }

    @Test
    fun `get route by car id success`() {
        val principal = BasicUserPrincipal("Emanuel")
        doReturn(Optional.of(car)).`when`(carRepository).findById(car.id)
        doReturn(Optional.of(route)).`when`(routeRepository).findById(route.id)
        doReturn(Optional.of(1f)).`when`(positionRepository).avgSpeedOfRoute(route.id)
        doReturn(Optional.of(2f)).`when`(positionRepository).sumDistanceOfRoute(route.id)
        doReturn(Optional.of(3)).`when`(positionRepository).travelTimeOfRoute(route.id)

        val pageableCaptor = argumentCaptor<Pageable>()
        doReturn(PageImpl(listOf(route))).`when`(routeRepository).findDistinctByCar_IdAndPositionsIsNotNullOrderByTimeDesc(
                com.nhaarman.mockitokotlin2.eq(car.id), pageableCaptor.capture())

        val jsonResult = routeController.getRoutesByCarId(principal, request, response, car.id, 2, 29)

        assertEquals(29, pageableCaptor.firstValue.pageSize)
        assertEquals(2, pageableCaptor.firstValue.pageNumber)

        val jsonExpected = Route.gson.toJson(listOf(route.copy(avgSpeed = 1f, length = 2f, secondsOfTravel = 3)))
        assertEquals(jsonExpected, jsonResult)
        assertEquals(HttpServletResponse.SC_OK, response.status)
    }

    @Test
    fun `get route by car id invalid page limit -23`() {
        val principal = BasicUserPrincipal("Emanuel")
        doReturn(Optional.of(car)).`when`(carRepository).findById(car.id)
        doReturn(Optional.of(route)).`when`(routeRepository).findById(route.id)
        doReturn(Optional.of(1f)).`when`(positionRepository).avgSpeedOfRoute(route.id)
        doReturn(Optional.of(2f)).`when`(positionRepository).sumDistanceOfRoute(route.id)
        doReturn(Optional.of(3)).`when`(positionRepository).travelTimeOfRoute(route.id)

        val pageableCaptor = argumentCaptor<Pageable>()
        doReturn(PageImpl(listOf(route))).`when`(routeRepository).findDistinctByCar_IdAndPositionsIsNotNullOrderByTimeDesc(
                com.nhaarman.mockitokotlin2.eq(car.id), pageableCaptor.capture())

        val jsonResult = routeController.getRoutesByCarId(principal, request, response, car.id, 2, -23)

        assertEquals(1, pageableCaptor.firstValue.pageSize)
        assertEquals(2, pageableCaptor.firstValue.pageNumber)

        val jsonExpected = Route.gson.toJson(listOf(route.copy(avgSpeed = 1f, length = 2f, secondsOfTravel = 3)))
        assertEquals(jsonExpected, jsonResult)
        assertEquals(HttpServletResponse.SC_OK, response.status)
    }

    @Test
    fun `get route by car id invalid page limit 0`() {
        val principal = BasicUserPrincipal("Emanuel")
        doReturn(Optional.of(car)).`when`(carRepository).findById(car.id)
        doReturn(Optional.of(route)).`when`(routeRepository).findById(route.id)
        doReturn(Optional.of(1f)).`when`(positionRepository).avgSpeedOfRoute(route.id)
        doReturn(Optional.of(2f)).`when`(positionRepository).sumDistanceOfRoute(route.id)
        doReturn(Optional.of(3)).`when`(positionRepository).travelTimeOfRoute(route.id)

        val pageableCaptor = argumentCaptor<Pageable>()
        doReturn(PageImpl(listOf(route))).`when`(routeRepository).findDistinctByCar_IdAndPositionsIsNotNullOrderByTimeDesc(
                com.nhaarman.mockitokotlin2.eq(car.id), pageableCaptor.capture())

        val jsonResult = routeController.getRoutesByCarId(principal, request, response, car.id, 2, 0)

        assertEquals(1, pageableCaptor.firstValue.pageSize)
        assertEquals(2, pageableCaptor.firstValue.pageNumber)

        val jsonExpected = Route.gson.toJson(listOf(route.copy(avgSpeed = 1f, length = 2f, secondsOfTravel = 3)))
        assertEquals(jsonExpected, jsonResult)
        assertEquals(HttpServletResponse.SC_OK, response.status)
    }

    @Test
    fun `get route by car id invalid car id`() {
        val principal = BasicUserPrincipal("Emanuel")
        val carOptional: Optional<Car> = Optional.empty()
        doReturn(carOptional).`when`(carRepository).findById(5345)

        val jsonResult = routeController.getRoutesByCarId(principal, request, response, 5345, 2, 34)

        verify(routeRepository, Times(0)).findDistinctByCar_UsernameAndPositionsIsNotNullOrderByTimeDesc(
                com.nhaarman.mockitokotlin2.any(), com.nhaarman.mockitokotlin2.any())
        assertTrue((JsonParser.parse(jsonResult) as Map<*, *>).containsKey("error"))
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.status)
    }

    @Test
    fun `get route by car id null principal`() {
        val principal = mock(UsernamePasswordAuthenticationToken::class.java)
        doReturn(null).`when`(principal).name
        doReturn(Optional.of(car)).`when`(carRepository).findById(car.id)

        val jsonResult = routeController.getRoutesByCarId(principal, request, response, car.id, 2, 12)

        verify(routeRepository, Times(0)).findDistinctByCar_UsernameAndPositionsIsNotNullOrderByTimeDesc(
                com.nhaarman.mockitokotlin2.any(), com.nhaarman.mockitokotlin2.any())
        assertEquals("", jsonResult)
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.status)
    }

    @Test
    fun `get route by car id not owner of car`() {
        val principal = BasicUserPrincipal("Zebra")
        doReturn(Optional.of(car)).`when`(carRepository).findById(car.id)

        val jsonResult = routeController.getRoutesByCarId(principal, request, response, car.id, 2, 12)

        verify(routeRepository, Times(0)).findDistinctByCar_UsernameAndPositionsIsNotNullOrderByTimeDesc(
                com.nhaarman.mockitokotlin2.any(), com.nhaarman.mockitokotlin2.any())
        assertEquals("", jsonResult)
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.status)
    }

    @Test
    fun `count routes of log user success`() {
        val principal = BasicUserPrincipal("David")
        doReturn(32L).`when`(routeRepository).countDistinctByCar_UsernameAndPositionsIsNotNull("David")

        val jsonResult = routeController.countRoutesOfLogUser(principal, request, response)

        assertEquals("{\"count\":\"32\"}", jsonResult)
        assertEquals(HttpServletResponse.SC_OK, response.status)
    }

    @Test
    fun `count routes of log user null principal`() {
        val principal = mock(UsernamePasswordAuthenticationToken::class.java)
        doReturn(null).`when`(principal).name

        val jsonResult = routeController.countRoutesOfLogUser(principal, request, response)

        verify(routeRepository, Times(0)).countDistinctByCar_UsernameAndPositionsIsNotNull(com.nhaarman.mockitokotlin2.any())
        assertEquals("", jsonResult)
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.status)
    }

    @Test
    fun `count routes by car success`() {
        val principal = BasicUserPrincipal("Harry")
        doReturn(0L).`when`(routeRepository).countDistinctByCar_IdAndPositionsIsNotNull(34)

        val jsonResult = routeController.countRoutesByCar(principal, request, response, 34)

        assertEquals("{\"count\":\"0\"}", jsonResult)
        assertEquals(HttpServletResponse.SC_OK, response.status)
    }

    @Test
    fun `count routes by car null principal`() {
        val principal = mock(UsernamePasswordAuthenticationToken::class.java)
        doReturn(null).`when`(principal).name

        val jsonResult = routeController.countRoutesOfLogUser(principal, request, response)

        verify(routeRepository, Times(0)).countDistinctByCar_IdAndPositionsIsNotNull(com.nhaarman.mockitokotlin2.any())
        assertEquals("", jsonResult)
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.status)
    }
}