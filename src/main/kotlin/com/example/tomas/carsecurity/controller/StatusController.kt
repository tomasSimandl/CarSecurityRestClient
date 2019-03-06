package com.example.tomas.carsecurity.controller

import com.example.tomas.carsecurity.model.dto.StatusCreate
import com.example.tomas.carsecurity.repository.CarRepository
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import com.google.gson.Gson
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.context.request.async.DeferredResult
import java.security.Principal
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.ReentrantLock
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


@RestController
class StatusController(
        private val carRepository: CarRepository
) {

    private val logger = LoggerFactory.getLogger(StatusController::class.java)

    private companion object {
        private val lock: ReentrantLock = ReentrantLock()
        private val conditionMap: MutableMap<Long, Condition> = HashMap()
        private val statusMap: MutableMap<Long, StatusCreate> = HashMap()
    }

    @PostMapping(STATUS_MAPPING)
    fun createStatus(
            principal: Principal,
            request: HttpServletRequest,
            response: HttpServletResponse,
            @RequestBody status: StatusCreate
    ): String {

        val car = carRepository.findById(status.carId)
        if (!car.isPresent) {
            logger.debug("Can not create status for not existing car")
            response.status = HttpServletResponse.SC_BAD_REQUEST
            return createJsonSingle("error", "Car does not exists")
        }

        if (principal.name == null || principal.name != car.get().username) {
            logger.debug("Can not create status record. User is not logged in or is not owner of the car.")
            response.status = HttpServletResponse.SC_UNAUTHORIZED
            return ""
        }

        lock.lock()
        statusMap[status.carId] = status // store result data in map
        conditionMap[status.carId]?.signal() // wake up waiting process
        lock.unlock()

        response.status = HttpServletResponse.SC_CREATED
        return ""
    }


    @GetMapping(STATUS_MAPPING)
    fun getStatus(
            principal: Principal,
            request: HttpServletRequest,
            response: HttpServletResponse,
            @RequestParam("car_id") carId: Long
    ): DeferredResult<ResponseEntity<String>> {

        val result = DeferredResult<ResponseEntity<String>>()

        Thread {
            val car = carRepository.findById(carId)
            if (!car.isPresent) {
                logger.debug("Can not create status for not existing car")
                result.setResult(ResponseEntity(createJsonSingle("error", "Car does not exists"), HttpStatus.BAD_REQUEST))
            }

            if (principal.name == null || principal.name != car.get().username) {
                logger.debug("Can not create status record. User is not logged in or is not owner of the car.")
                result.setResult(ResponseEntity(HttpStatus.UNAUTHORIZED))
            }

            sendFirebaseStatus(car.get().firebaseToken, principal.name)

            lock.lock()
            conditionMap[carId] = lock.newCondition()
            conditionMap[carId]?.await(15, TimeUnit.SECONDS)
            conditionMap.remove(carId)
            val status = statusMap.remove(carId)
            lock.unlock()

            if (status == null){
                result.setResult(ResponseEntity(HttpStatus.REQUEST_TIMEOUT))
            } else {
                result.setResult(ResponseEntity.ok(Gson().toJson(status)))
            }

        }.start()

        return result
    }

    private fun sendFirebaseStatus(token: String, username: String){
        if (token.isBlank()) {
            logger.warn("Firebase token is empty.")
            return
        }

        val message = Message.builder()
                .putData("command", "Status")
                .putData("username", username)
                .setToken(token)
                .build()

        val response = FirebaseMessaging.getInstance().send(message)
        logger.debug("Successfully sent message: $response")
    }
}