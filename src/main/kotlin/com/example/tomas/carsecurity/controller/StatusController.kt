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

/**
 * Class is used for handle status request from web application.
 *
 * @param carRepository is repository used for access cars in database.
 */
@RestController
class StatusController(
        private val carRepository: CarRepository
) {

    /** Logger for this class */
    private val logger = LoggerFactory.getLogger(StatusController::class.java)

    /**
     * Object used for keep variables static across all requests.
     */
    private companion object {
        /** Lock for access [conditionMap] and [statusMap] */
        private val lock: ReentrantLock = ReentrantLock()
        /** Map contains condition variables on which request waiting on response from Android */
        private val conditionMap: MutableMap<Long, Condition> = HashMap()
        /** Map of statuses which were get from android and not send to back to web client yet. */
        private val statusMap: MutableMap<Long, StatusCreate> = HashMap()
    }

    /**
     * Method create new status in [statusMap] and wait web request which waiting on this status
     * Returned status code can be CREATED, UNAUTHORIZED, BAD_REQUEST
     *
     * @param principal of actual logged user.
     * @param request for creating status.
     * @param response to creating status request.
     * @param status which will be created in [statusMap].
     * @return Empty string or json with error message on BAD_REQUEST.
     */
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

    /**
     * Method init Firebase request which is send to device and sleep on condition which is stored in [conditionMap].
     * On wake up or timeout method check [statusMap] if there is some data for it and return REQUEST_TIMOUT or status.
     * Returned status code can be OK, UNAUTHORIZED, BAD_REQUEST, REQUEST_TIMEOUT
     *
     * @param principal of actual logged user.
     * @param request for getting status.
     * @param response to getting status request.
     * @param carId is identification of car of which status is requested.
     * @return Json with status, json with error message on BAD_REQUEST or empty string.
     */
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

            if (status == null) {
                result.setResult(ResponseEntity(HttpStatus.REQUEST_TIMEOUT))
            } else {
                result.setResult(ResponseEntity.ok(Gson().toJson(status)))
            }

        }.start()

        return result
    }

    /**
     * Method send get status command to Firebase which deliver message to device identified by [token].
     *
     * @param token which identified device.
     * @param username which is append to message for additional security.
     */
    private fun sendFirebaseStatus(token: String, username: String) {
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