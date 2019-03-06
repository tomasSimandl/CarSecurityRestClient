package com.example.tomas.carsecurity.controller

import com.example.tomas.carsecurity.repository.CarRepository
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.security.Principal
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@RestController
class ToolController(
        private val carRepository: CarRepository
) {

    private val logger = LoggerFactory.getLogger(ToolController::class.java)

    @PostMapping("$TOOL_MAPPING/{action}")
    fun switchTool(
            principal: Principal,
            request: HttpServletRequest,
            response: HttpServletResponse,
            @RequestParam("car_id") carId: Long,
            @RequestParam("tool") tool: String,
            @PathVariable action: String
    ): String {

        if (tool.isBlank() || action.isBlank()){
            logger.debug("Empty input parameters")
            response.status = HttpServletResponse.SC_BAD_REQUEST
            return createJsonSingle("error", "Invalid parameters")
        }

        val car = carRepository.findById(carId)
        if (!car.isPresent) {
            logger.debug("Can not switch tool for not existing car")
            response.status = HttpServletResponse.SC_BAD_REQUEST
            return createJsonSingle("error", "Car does not exists")
        }

        if (principal.name == null || principal.name != car.get().username) {
            logger.debug("Can not switch tool. User is not logged in or is not owner of the car.")
            response.status = HttpServletResponse.SC_UNAUTHORIZED
            return ""
        }

        sendFirebaseSwitch(action, principal.name, tool, car.get().firebaseToken)

        response.status = HttpServletResponse.SC_CREATED
        return ""
    }

    private fun sendFirebaseSwitch(command: String, username: String, tool: String, token: String){
        if (token.isBlank()) {
            logger.warn("Firebase token is empty.")
            return
        }

        val message = Message.builder()
                .putData("command", command.capitalize())
                .putData("tool", tool.capitalize())
                .putData("username", username)
                .setToken(token)
                .build()
        
        val response = FirebaseMessaging.getInstance().send(message)
        logger.debug("Successfully sent message: $response")
    }
}