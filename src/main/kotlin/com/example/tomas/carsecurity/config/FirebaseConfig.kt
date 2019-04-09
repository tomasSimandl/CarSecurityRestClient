package com.example.tomas.carsecurity.config

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.core.io.ClassPathResource
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.stereotype.Service

/**
 * Class is used for configuration of Firebase SDK
 */
@Service
class FirebaseConfig(

        /** URL of firebase database */
        @Value("\${firebase.database.url}")
        private val firebaseDatabaseUrl: String = "",

        /** Relative path from resources to json configuration file */
        @Value("\${firebase.configuration.file}")
        private val firebaseConfigFile: String = ""

) : WebSecurityConfigurerAdapter() {

    /** Logger of this class. */
    private val logger = LoggerFactory.getLogger(WebSecurityConfigurerAdapter::class.java)

    /**
     * Initialization of firebase SDK.
     */
    @Bean
    fun firebaseConfiguration() {
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
}