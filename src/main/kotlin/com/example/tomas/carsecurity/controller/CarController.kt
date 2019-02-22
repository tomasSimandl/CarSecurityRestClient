package com.example.tomas.carsecurity.controller

import com.example.tomas.carsecurity.model.Car
import com.example.tomas.carsecurity.model.dto.CarCreate
import com.example.tomas.carsecurity.model.dto.CarUpdate
import com.example.tomas.carsecurity.repository.CarRepository
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.web.bind.annotation.*
import java.security.Principal
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


@RestController
class CarController(
        private val carRepository: CarRepository
) {

    private val logger = LoggerFactory.getLogger(CarController::class.java)

    @ResponseBody
    @PostMapping(CAR_MAPPING, produces = ["application/json; charset=utf-8"])
    fun createCar(
            principal: Principal,
            request: HttpServletRequest,
            response: HttpServletResponse,
            @RequestBody carCreate: CarCreate
    ): String {

        logger.info("Create new car request.")

        if (principal.name == null) {
            logger.debug("Can not create car. User is not logged in.")
            response.status = HttpServletResponse.SC_UNAUTHORIZED
            return ""
        }

        if (carCreate.name.isBlank()){
            logger.debug("Can not create car with empty name.")
            response.status = HttpServletResponse.SC_BAD_REQUEST
            return createJsonSingle("error", "Name can not be empty.")
        }

        var car = Car(username = principal.name, name = carCreate.name, icon = carCreate.icon)
        car = carRepository.save(car)

        logger.debug("New car created.")
        response.status = HttpServletResponse.SC_CREATED
        return createJsonSingle("car_id", car.id.toString())
    }

    @ResponseBody
    @PutMapping(CAR_MAPPING, produces = ["application/json; charset=utf-8"])
    fun updateCar(
            principal: Principal,
            request: HttpServletRequest,
            response: HttpServletResponse,
            @RequestBody carUpdate: CarUpdate
    ): String {

        logger.info("Update new car request.")

        val dbCar = carRepository.findById(carUpdate.id)
        if(!dbCar.isPresent) {
            logger.debug("Can not update not existing car")
            response.status = HttpServletResponse.SC_BAD_REQUEST
            return createJsonSingle("error", "Car does not exists")
        }

        if (principal.name == null || principal.name != dbCar.get().username) {
            logger.debug("Can not create car. User is not logged in or is not owner of the car.")
            response.status = HttpServletResponse.SC_UNAUTHORIZED
            return ""
        }

        if (carUpdate.name.isBlank()){
            logger.debug("Can not create car with empty name.")
            response.status = HttpServletResponse.SC_BAD_REQUEST
            return createJsonSingle("error", "Name can not be empty")
        }

        dbCar.get().name = carUpdate.name
        dbCar.get().icon = carUpdate.icon

        carRepository.save(dbCar.get())
        logger.debug("Car updated.")
        return ""
    }

    @ResponseBody
    @DeleteMapping(CAR_MAPPING, params = ["car_id"], produces = ["application/json; charset=utf-8"])
    fun deleteCarById(
            principal: Principal,
            request: HttpServletRequest,
            response: HttpServletResponse,
            @RequestParam(value = "car_id") carId: Long
    ): String {

        val car = carRepository.findById(carId)
        if (!car.isPresent) {
            logger.debug("Car does not exists.")
            response.status = HttpServletResponse.SC_BAD_REQUEST
            return createJsonSingle("error", "Car does not exists")
        }

        if (principal.name == null || car.get().username != principal.name) {
            logger.debug("User: ${principal.name} is not owner of requested car: $carId")
            response.status = HttpServletResponse.SC_UNAUTHORIZED
            return ""
        }

        carRepository.delete(car.get())
        return ""
    }

    @ResponseBody
    @GetMapping(CAR_MAPPING, produces = ["application/json; charset=utf-8"])
    fun getCarsOfLogUser(
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
        val cars = carRepository.findAllByUsername(principal.name, PageRequest.of(page, validLimit))
        return Car.gson.toJson(cars.content)
    }


    @ResponseBody
    @GetMapping(CAR_MAPPING, params = ["car_id"], produces = ["application/json; charset=utf-8"])
    fun getCarById(
            principal: Principal,
            request: HttpServletRequest,
            response: HttpServletResponse,
            @RequestParam(value = "car_id") carId: Long
    ): String {

        val car = carRepository.findById(carId)
        if (!car.isPresent) {
            logger.debug("Car does not exists.")
            response.status = HttpServletResponse.SC_BAD_REQUEST
            return createJsonSingle("error", "Car does not exists")
        }

        if (principal.name == null || car.get().username != principal.name) {
            logger.debug("User: ${principal.name} is not owner of requested car: $carId")
            response.status = HttpServletResponse.SC_UNAUTHORIZED
            return ""
        }

        return Car.gson.toJson(car.get())
    }
}