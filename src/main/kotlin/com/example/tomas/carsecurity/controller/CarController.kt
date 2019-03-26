package com.example.tomas.carsecurity.controller

import com.example.tomas.carsecurity.model.Car
import com.example.tomas.carsecurity.model.dto.CarCreate
import com.example.tomas.carsecurity.model.dto.CarUpdate
import com.example.tomas.carsecurity.repository.CarRepository
import com.example.tomas.carsecurity.repository.DeleteUtil
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.*
import java.security.Principal
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * This controller is used for access and manage cars in database. User is authorized to access only his own cars.
 *
 * @param carRepository is repository for access cars in database.
 * @param deleteUtil is util used for delete cars and all his events and routes.
 */
@RestController
class CarController(
        private val carRepository: CarRepository,
        private val deleteUtil: DeleteUtil
) {

    /** Logger of this class */
    private val logger = LoggerFactory.getLogger(CarController::class.java)

    /**
     * Method create new car in database.
     * Returned status code can be CREATED, UNAUTHORIZED, BAD_REQUEST
     *
     * @param principal of actual logged user.
     * @param request for create car
     * @param response on create car request
     * @param carCreate car which will be created in database.
     * @return Json with created car_id on CREATED, Json with error message on BAD_REQUEST
     *          or empty String on UNAUTHORIZED.
     */
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

        if (carCreate.name.isBlank()) {
            logger.debug("Can not create car with empty name.")
            response.status = HttpServletResponse.SC_BAD_REQUEST
            return createJsonSingle("error", "Name can not be empty.")
        }

        var car = Car(username = principal.name, name = carCreate.name, note = carCreate.note)
        car = carRepository.save(car)

        logger.debug("New car created.")
        response.status = HttpServletResponse.SC_CREATED
        return createJsonSingle("car_id", car.id.toString())
    }

    /**
     * Method update given car in database. Id of [carUpdate] must be set.
     * Logged user must be owner of updated car.
     *
     * Returned status code can be OK, UNAUTHORIZED, BAD_REQUEST
     *
     * @param principal of actual logged user.
     * @param request for update car.
     * @param response on update car request.
     * @param carUpdate car which will be updated.
     * @return Empty String or json with error message on BAD_REQUEST.
     */
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
        if (!dbCar.isPresent) {
            logger.debug("Can not update not existing car")
            response.status = HttpServletResponse.SC_BAD_REQUEST
            return createJsonSingle("error", "Car does not exists")
        }

        if (principal.name == null || principal.name != dbCar.get().username) {
            logger.debug("Can not create car. User is not logged in or is not owner of the car.")
            response.status = HttpServletResponse.SC_UNAUTHORIZED
            return ""
        }

        if (carUpdate.name.isBlank()) {
            logger.debug("Can not create car with empty name.")
            response.status = HttpServletResponse.SC_BAD_REQUEST
            return createJsonSingle("error", "Name can not be empty")
        }

        dbCar.get().name = carUpdate.name
        dbCar.get().note = carUpdate.note

        carRepository.save(dbCar.get())
        logger.debug("Car updated.")
        return ""
    }

    /**
     * Method delete car from database given car id. Logged user must be owner of deleted car. With car will be
     * deleted also all events and routes.
     *
     * Returned status code can be OK, UNAUTHORIZED, BAD_REQUEST
     *
     * @param principal of actual logged user.
     * @param request for delete car.
     * @param response on delete car request.
     * @param carId identification number of car.
     * @return Empty string or json with error message on BAD_REQUEST.
     */
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

        deleteUtil.deleteCars(listOf(car.get()))
        return ""
    }

    /**
     * Method delete all cars of logged user from database.
     *
     * Returned status code can be OK, UNAUTHORIZED.
     *
     * @param principal of actual logged user.
     * @param request for delete users car.
     * @param response on delete users car request.
     * @return Empty String.
     */
    @ResponseBody
    @DeleteMapping(CAR_MAPPING, produces = ["application/json; charset=utf-8"])
    fun deleteCarsByUser(
            principal: Principal,
            request: HttpServletRequest,
            response: HttpServletResponse
    ): String {

        if (principal.name == null) {
            logger.debug("Principal is null.")
            response.status = HttpServletResponse.SC_UNAUTHORIZED
            return ""
        }

        val cars = carRepository.findAllByUsername(principal.name, Pageable.unpaged())
        if (cars.isEmpty) {
            logger.debug("No cars to delete.")
            return ""
        }

        deleteUtil.deleteCars(cars.content)
        return ""
    }

    /**
     * Method return list of cars from database which are associated with logged user.
     *
     * Returned status code can be OK, UNAUTHORIZED
     *
     * @param principal of actual logged user.
     * @param request for get users cars.
     * @param response on get users cars request.
     * @param page of cars divided by [limit].
     * @param limit of cars per page.
     * @return Json of requested  cars or empty String on UNAUTHORIZED.
     */
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


    /**
     * Method return car from database with given id. Logged user must be owner of requested car.
     *
     * Returned status code can be OK, UNAUTHORIZED, BAD_REQUEST
     *
     * @param principal of actual logged user.
     * @param request for get car by id.
     * @param response on get car by id request.
     * @param carId identification number of car.
     * @return Json of requested  car, Json with error message on BAD_REQUEST or empty String on UNAUTHORIZED.
     */
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