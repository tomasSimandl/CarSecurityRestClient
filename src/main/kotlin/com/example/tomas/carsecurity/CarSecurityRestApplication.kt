package com.example.tomas.carsecurity

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.core.io.ClassPathResource


@SpringBootApplication
class CarSecurityRestApplication

/** Logger of this class. */
private val logger = LoggerFactory.getLogger(CarSecurityRestApplication::class.java)

/** URL of firebase database */
@Value("\${firebase.database.url}")
private val firebaseDatabaseUrl: String = ""

/** Relative path from resources to json configuration file */
@Value("\${firebase.configuration.file}")
private val firebaseConfigFile: String = ""

/**
 * Method run whole application and initialize service for communication with Firebase servers.
 */
fun main(args: Array<String>) {
    runApplication<CarSecurityRestApplication>(*args)

    try {
        val serviceAccount = ClassPathResource(firebaseConfigFile).inputStream
        val options = FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .setDatabaseUrl(firebaseDatabaseUrl)
                .build()

        FirebaseApp.initializeApp(options)
    } catch (e: Exception) {
        logger.error("Can not initialize Firebase.", e)
    }
}

