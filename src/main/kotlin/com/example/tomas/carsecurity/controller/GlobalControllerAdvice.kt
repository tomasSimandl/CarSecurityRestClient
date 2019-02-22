package com.example.tomas.carsecurity.controller

import org.slf4j.LoggerFactory
import org.springframework.core.annotation.AnnotationUtils
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import javax.servlet.http.HttpServletRequest

@ControllerAdvice
@RestController
class GlobalControllerAdvice: ResponseEntityExceptionHandler() {

    private val myLogger = LoggerFactory.getLogger(GlobalControllerAdvice::class.java)


    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(value = [Exception::class])
    @ResponseBody
    fun defaultErrorHandler(req: HttpServletRequest, e: Exception): ErrorBody {
        // If the exception is annotated with @ResponseStatus rethrow it and let
        // the framework handle it - like the OrderNotFoundException example
        // at the start of this post.
        // AnnotationUtils is a Spring Framework utility class.
        if (AnnotationUtils.findAnnotation(e.javaClass, ResponseStatus::class.java) != null)
            throw e

        myLogger.error("Request to: ${req.requestURL} failed: ", e)
        return ErrorBody("Request failed due to the exception ${e::class.simpleName}")
    }

    data class ErrorBody (
            val error: String
    )
}