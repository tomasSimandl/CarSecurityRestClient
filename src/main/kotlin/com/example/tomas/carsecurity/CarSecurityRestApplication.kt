package com.example.tomas.carsecurity

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication


@SpringBootApplication
class CarSecurityRestApplication

/**
 * Method run whole application and initialize it.
 */
fun main(args: Array<String>) {
    runApplication<CarSecurityRestApplication>(*args)
}

