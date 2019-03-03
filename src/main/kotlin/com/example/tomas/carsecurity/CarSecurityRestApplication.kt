package com.example.tomas.carsecurity

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import com.google.firebase.FirebaseApp
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseOptions
import org.slf4j.LoggerFactory
import org.springframework.core.io.ClassPathResource
import java.io.FileInputStream



@SpringBootApplication
class CarSecurityRestApplication

private val logger = LoggerFactory.getLogger(CarSecurityRestApplication::class.java)

fun main(args: Array<String>) {
	runApplication<CarSecurityRestApplication>(*args)

	try {
		val serviceAccount = ClassPathResource("carsecurity-firebase.json").inputStream
		val options = FirebaseOptions.Builder()
				.setCredentials(GoogleCredentials.fromStream(serviceAccount))
				.setDatabaseUrl("https://carsecurity-72699.firebaseio.com")
				.build()

		FirebaseApp.initializeApp(options)
	}catch (e: Exception){
		logger.error("Can not initialize Firebase.", e)
	}
}

