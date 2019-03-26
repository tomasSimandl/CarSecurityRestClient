package com.example.tomas.carsecurity.controller

import org.slf4j.LoggerFactory
import org.springframework.core.annotation.AnnotationUtils
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import javax.servlet.http.HttpServletRequest

/**
 * This class is used for process every exception from every controller.
 */
@ControllerAdvice
@RestController
class GlobalControllerAdvice : ResponseEntityExceptionHandler() {

    /** Logger for this class */
    private val myLogger = LoggerFactory.getLogger(GlobalControllerAdvice::class.java)


    /**
     * Method catch all exceptions from controllers. Exception is logged and to user is returned only exception
     * message. When exception is annotated with ResponseStatus, exception is provide to user.
     *
     * @param req is original request
     * @param e is exception witch was thrown.
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(value = [Exception::class])
    @ResponseBody
    fun defaultErrorHandler(req: HttpServletRequest, e: Exception): ErrorBody {
        if (AnnotationUtils.findAnnotation(e.javaClass, ResponseStatus::class.java) != null)
            throw e

        myLogger.error("Request to: ${req.requestURL} failed: ", e)
        return ErrorBody("Request failed due to the exception ${e::class.simpleName}")
    }

    /**
     * Class which is used for returning error message in [defaultErrorHandler] method.
     */
    data class ErrorBody(
            /** Error message which is shown to user. */
            val error: String
    )
}