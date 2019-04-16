package com.example.tomas.carsecurity.controller


import com.example.tomas.carsecurity.anyKotlin
import com.example.tomas.carsecurity.model.Car
import com.example.tomas.carsecurity.model.dto.CarCreate
import com.example.tomas.carsecurity.model.dto.CarUpdate
import com.example.tomas.carsecurity.repository.CarRepository
import com.example.tomas.carsecurity.repository.DeleteUtil
import com.nhaarman.mockitokotlin2.argumentCaptor
import io.grpc.internal.JsonParser
import org.apache.http.auth.BasicUserPrincipal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.mockito.internal.verification.Times
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import java.util.*
import javax.servlet.http.HttpServletResponse

class CarControllerTest {

    @Mock
    private lateinit var carRepository: CarRepository
    @Mock
    private lateinit var deleteUtil: DeleteUtil

    private lateinit var carController: CarController


    private lateinit var request: MockHttpServletRequest
    private lateinit var response: MockHttpServletResponse


    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
        request = MockHttpServletRequest()
        response = MockHttpServletResponse()

        carController = CarController(carRepository, deleteUtil)
    }

    @Test
    fun `create car success`() {

        val carCreate = CarCreate("Trabant", "Bakelite car")
        val car = Car(123, "Leos Mares", ArrayList(), ArrayList(), "Trabant", "Bakelite car", "")
        val principal = BasicUserPrincipal("Leos Mares")

        val carCaptor = ArgumentCaptor.forClass(Car::class.java)
        doReturn(car).`when`(carRepository).save(carCaptor.capture())

        val jsonResponse = carController.createCar(principal, request, response, carCreate)

        assertEquals("Leos Mares", carCaptor.value.username)
        assertEquals("Trabant", carCaptor.value.name)
        assertEquals("Bakelite car", carCaptor.value.note)
        assertTrue(carCaptor.value.events.isEmpty())
        assertTrue(carCaptor.value.routes.isEmpty())

        val responseMap = (JsonParser.parse(jsonResponse) as Map<*, *>)
        assertTrue(responseMap.containsKey("car_id"))
        assertEquals("${car.id}", responseMap["car_id"])
        assertEquals(HttpServletResponse.SC_CREATED, response.status)
    }

    @Test
    fun `create car invalid principal`() {

        val carCreate = CarCreate("Trabant", "Bakelite car")
        val principal = mock(UsernamePasswordAuthenticationToken::class.java)

        doReturn(null).`when`(principal).name

        val jsonResponse = carController.createCar(principal, request, response, carCreate)

        verify(carRepository, Times(0)).save(anyKotlin(Car::class.java))
        assertEquals("", jsonResponse)
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.status)
    }

    @Test
    fun `create car blank name`() {

        val carCreate = CarCreate(" ", "Bakelite car")
        val principal = BasicUserPrincipal("Leos Mares")

        val jsonResponse = carController.createCar(principal, request, response, carCreate)

        verify(carRepository, Times(0)).save(anyKotlin(Car::class.java))
        assertTrue((JsonParser.parse(jsonResponse) as Map<*, *>).containsKey("error"))
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.status)
    }

    @Test
    fun `update car success`() {

        val carUpdate = CarUpdate(987, "Ferrari", "Flat car")
        val car = Car(987, "Leos Mares", ArrayList(), ArrayList(), "Trabant", "Bakelite car", "token")
        val principal = BasicUserPrincipal("Leos Mares")

        doReturn(Optional.of(car)).`when`(carRepository).findById(car.id)

        val jsonResponse = carController.updateCar(principal, request, response, carUpdate)

        val carCaptor = ArgumentCaptor.forClass(Car::class.java)
        verify(carRepository, Times(1)).save(carCaptor.capture())

        assertEquals("Leos Mares", carCaptor.value.username)
        assertEquals("Ferrari", carCaptor.value.name)
        assertEquals("Flat car", carCaptor.value.note)
        assertEquals("token", carCaptor.value.firebaseToken)
        assertTrue(carCaptor.value.events.isEmpty())
        assertTrue(carCaptor.value.routes.isEmpty())

        assertEquals("", jsonResponse)
        assertEquals(HttpServletResponse.SC_OK, response.status)
    }

    @Test
    fun `update car non existing car`() {

        val carUpdate = CarUpdate(567, "Trabant", "Bakelite car")
        val principal = BasicUserPrincipal("Leos Mares")
        val carOptional: Optional<Car> = Optional.empty()

        doReturn(carOptional).`when`(carRepository).findById(anyKotlin(Long::class.java))

        val jsonResponse = carController.updateCar(principal, request, response, carUpdate)

        verify(carRepository, Times(0)).save(anyKotlin(Car::class.java))
        assertTrue((JsonParser.parse(jsonResponse) as Map<*, *>).containsKey("error"))
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.status)
    }

    @Test
    fun `update car empty principal`() {

        val carUpdate = CarUpdate(567, "Trabant", "Bakelite car")
        val car = Car(username = "Leos Mares")
        val principal = mock(UsernamePasswordAuthenticationToken::class.java)

        doReturn(Optional.of(car)).`when`(carRepository).findById(anyKotlin(Long::class.java))
        doReturn(null).`when`(principal).name

        val jsonResponse = carController.updateCar(principal, request, response, carUpdate)

        verify(carRepository, Times(0)).save(anyKotlin(Car::class.java))
        assertEquals("", jsonResponse)
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.status)
    }

    @Test
    fun `update car not match principal name with car owner name`() {

        val carUpdate = CarUpdate(567, "Ferrari", "Flat car")
        val car = Car(username = "Leos Mares")
        val principal = BasicUserPrincipal("Kazma")

        doReturn(Optional.of(car)).`when`(carRepository).findById(anyKotlin(Long::class.java))

        val jsonResponse = carController.updateCar(principal, request, response, carUpdate)

        verify(carRepository, Times(0)).save(anyKotlin(Car::class.java))
        assertEquals("", jsonResponse)
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.status)
    }

    @Test
    fun `update car blank new car name`() {

        val carUpdate = CarUpdate(567, " ", "Blank car")
        val car = Car(username = "Kazma")
        val principal = BasicUserPrincipal("Kazma")

        doReturn(Optional.of(car)).`when`(carRepository).findById(anyKotlin(Long::class.java))

        val jsonResponse = carController.updateCar(principal, request, response, carUpdate)

        verify(carRepository, Times(0)).save(anyKotlin(Car::class.java))
        assertTrue((JsonParser.parse(jsonResponse) as Map<*, *>).containsKey("error"))
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.status)
    }

    @Test
    fun `delete car by id success`() {

        val carId = 567L
        val car = Car(username = "Leos Mares")
        val principal = BasicUserPrincipal("Leos Mares")

        doReturn(Optional.of(car)).`when`(carRepository).findById(anyKotlin(Long::class.java))

        val jsonResponse = carController.deleteCarById(principal, request, response, carId)

        assertEquals("", jsonResponse)
        assertEquals(HttpServletResponse.SC_OK, response.status)

        val carListCaptor = argumentCaptor<List<Car>>()
        verify(deleteUtil, Times(1)).deleteCars(carListCaptor.capture())
        assertEquals(1, carListCaptor.firstValue.size)
        assertEquals(car, carListCaptor.firstValue.first())
    }

    @Test
    fun `delete car by id non existing car`() {

        val carId = 876L
        val principal = BasicUserPrincipal("Leos Mares")
        val carOptional: Optional<Car> = Optional.empty()

        doReturn(carOptional).`when`(carRepository).findById(anyKotlin(Long::class.java))

        val jsonResponse = carController.deleteCarById(principal, request, response, carId)

        verify(deleteUtil, Times(0)).deleteCars(anyKotlin(emptyList<Car>()::class.java))
        assertTrue((JsonParser.parse(jsonResponse) as Map<*, *>).containsKey("error"))
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.status)
    }

    @Test
    fun `delete car by id empty principal`() {

        val carId = 4567L
        val car = Car(username = "Leos Mares")
        val principal = mock(UsernamePasswordAuthenticationToken::class.java)

        doReturn(Optional.of(car)).`when`(carRepository).findById(anyKotlin(Long::class.java))
        doReturn(null).`when`(principal).name

        val jsonResponse = carController.deleteCarById(principal, request, response, carId)

        verify(deleteUtil, Times(0)).deleteCars(anyKotlin(emptyList<Car>()::class.java))
        assertEquals("", jsonResponse)
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.status)
    }

    @Test
    fun `delete car by id not match principal name with car owner name`() {

        val carId = 567L
        val car = Car(username = "Leos Mares")
        val principal = BasicUserPrincipal("Kazma")

        doReturn(Optional.of(car)).`when`(carRepository).findById(anyKotlin(Long::class.java))

        val jsonResponse = carController.deleteCarById(principal, request, response, carId)

        verify(deleteUtil, Times(0)).deleteCars(anyKotlin(emptyList<Car>()::class.java))
        assertEquals("", jsonResponse)
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.status)
    }

    @Test
    fun `delete car by user success`() {

        val car1 = Car(username = "Leos Mares")
        val car2 = Car(username = "Leos Mares")
        val cars = listOf(car1, car2)
        val principal = BasicUserPrincipal("Leos Mares")

        doReturn(PageImpl(cars)).`when`(carRepository).findAllByUsername("Leos Mares", Pageable.unpaged())

        val jsonResponse = carController.deleteCarsByUser(principal, request, response)

        val carListCaptor = argumentCaptor<List<Car>>()
        verify(deleteUtil, Times(1)).deleteCars(carListCaptor.capture())

        assertEquals(2, carListCaptor.firstValue.size)
        assertEquals(cars.first(), carListCaptor.firstValue.first())
        assertEquals(cars.last(), carListCaptor.firstValue.last())
        assertEquals("", jsonResponse)
        assertEquals(HttpServletResponse.SC_OK, response.status)
    }

    @Test
    fun `delete car by user empty principal`() {

        val principal = mock(UsernamePasswordAuthenticationToken::class.java)
        doReturn(null).`when`(principal).name

        val jsonResponse = carController.deleteCarsByUser(principal, request, response)

        verify(deleteUtil, Times(0)).deleteCars(anyKotlin(emptyList<Car>()::class.java))
        assertEquals("", jsonResponse)
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.status)
    }

    @Test
    fun `delete car by user no cars`() {

        val cars = emptyList<Car>()
        val principal = BasicUserPrincipal("Leos Mares")

        doReturn(PageImpl(cars)).`when`(carRepository).findAllByUsername(com.nhaarman.mockitokotlin2.eq("Leos Mares"), com.nhaarman.mockitokotlin2.eq(Pageable.unpaged()))

        val jsonResponse = carController.deleteCarsByUser(principal, request, response)

        verify(deleteUtil, Times(0)).deleteCars(anyKotlin(emptyList<Car>()::class.java))
        assertEquals("", jsonResponse)
        assertEquals(HttpServletResponse.SC_OK, response.status)
    }

    @Test
    fun `get cars of logged user success`() {

        val principal = BasicUserPrincipal("Leos Mares")
        val car1 = Car(username = "Leos Mares")
        val car2 = Car(username = "Leos Mares")
        val cars = listOf(car1, car2)

        doReturn(PageImpl(cars)).`when`(carRepository).findAllByUsername(anyKotlin(String::class.java), anyKotlin(Pageable::class.java))

        val jsonResponse = carController.getCarsOfLogUser(principal, request, response, 0, 10)

        val pageableCaptor = argumentCaptor<Pageable>()
        verify(carRepository, Times(1)).findAllByUsername(com.nhaarman.mockitokotlin2.eq("Leos Mares"), pageableCaptor.capture())

        assertEquals(10, pageableCaptor.firstValue.pageSize)
        assertEquals(0, pageableCaptor.firstValue.pageNumber)

        assertEquals(Car.gson.toJson(cars), jsonResponse)
        assertEquals(HttpServletResponse.SC_OK, response.status)
    }


    @Test
    fun `get cars of logged user invalid page size -1`() {

        val principal = BasicUserPrincipal("Leos Mares")
        val car1 = Car(username = "Leos Mares")
        val car2 = Car(username = "Leos Mares")
        val cars = listOf(car1, car2)

        doReturn(PageImpl(cars)).`when`(carRepository).findAllByUsername(anyKotlin(String::class.java), anyKotlin(Pageable::class.java))

        val jsonResponse = carController.getCarsOfLogUser(principal, request, response, 0, -1)

        val pageableCaptor = argumentCaptor<Pageable>()
        verify(carRepository, Times(1)).findAllByUsername(com.nhaarman.mockitokotlin2.eq("Leos Mares"), pageableCaptor.capture())

        assertEquals(1, pageableCaptor.firstValue.pageSize)
        assertEquals(0, pageableCaptor.firstValue.pageNumber)

        assertEquals(Car.gson.toJson(cars), jsonResponse)
        assertEquals(HttpServletResponse.SC_OK, response.status)
    }

    @Test
    fun `get cars of logged user invalid page size 0`() {

        val principal = BasicUserPrincipal("Leos Mares")
        val car1 = Car(username = "Leos Mares")
        val car2 = Car(username = "Leos Mares")
        val cars = listOf(car1, car2)

        doReturn(PageImpl(cars)).`when`(carRepository).findAllByUsername(anyKotlin(String::class.java), anyKotlin(Pageable::class.java))

        val jsonResponse = carController.getCarsOfLogUser(principal, request, response, 0, 0)

        val pageableCaptor = argumentCaptor<Pageable>()
        verify(carRepository, Times(1)).findAllByUsername(com.nhaarman.mockitokotlin2.eq("Leos Mares"), pageableCaptor.capture())

        assertEquals(1, pageableCaptor.firstValue.pageSize)
        assertEquals(0, pageableCaptor.firstValue.pageNumber)

        assertEquals(Car.gson.toJson(cars), jsonResponse)
        assertEquals(HttpServletResponse.SC_OK, response.status)
    }


    @Test
    fun `get cars of logged user empty principal`() {

        val principal = mock(UsernamePasswordAuthenticationToken::class.java)
        doReturn(null).`when`(principal).name

        val jsonResponse = carController.getCarsOfLogUser(principal, request, response, 0, 10)

        verify(carRepository, Times(0)).findAllByUsername(anyKotlin(String::class.java), anyKotlin(Pageable::class.java))
        assertEquals("", jsonResponse)
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.status)
    }

    @Test
    fun `get car by id success`() {

        val carId = 567L
        val car = Car(username = "Leos Mares")
        val principal = BasicUserPrincipal("Leos Mares")

        doReturn(Optional.of(car)).`when`(carRepository).findById(anyKotlin(Long::class.java))

        val jsonResponse = carController.getCarById(principal, request, response, carId)

        assertEquals(Car.gson.toJson(car), jsonResponse)
        assertEquals(HttpServletResponse.SC_OK, response.status)
    }

    @Test
    fun `get car by id non existing car`() {

        val carId = 876L
        val principal = BasicUserPrincipal("Leos Mares")
        val carOptional: Optional<Car> = Optional.empty()

        doReturn(carOptional).`when`(carRepository).findById(anyKotlin(Long::class.java))

        val jsonResponse = carController.getCarById(principal, request, response, carId)

        assertTrue((JsonParser.parse(jsonResponse) as Map<*, *>).containsKey("error"))
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.status)
    }

    @Test
    fun `get car by id empty principal`() {

        val carId = 4567L
        val car = Car(username = "Leos Mares")
        val principal = mock(UsernamePasswordAuthenticationToken::class.java)

        doReturn(Optional.of(car)).`when`(carRepository).findById(anyKotlin(Long::class.java))
        doReturn(null).`when`(principal).name

        val jsonResponse = carController.getCarById(principal, request, response, carId)

        assertEquals("", jsonResponse)
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.status)
    }

    @Test
    fun `get car by id not match principal name with car owner name`() {

        val carId = 567L
        val car = Car(username = "Leos Mares")
        val principal = BasicUserPrincipal("Kazma")

        doReturn(Optional.of(car)).`when`(carRepository).findById(anyKotlin(Long::class.java))

        val jsonResponse = carController.getCarById(principal, request, response, carId)

        assertEquals("", jsonResponse)
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.status)
    }
}