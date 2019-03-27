package com.example.tomas.carsecurity.service

import com.google.gson.JsonParser
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.security.oauth2.provider.OAuth2Authentication
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.security.Principal

/**
 * Implementation of service which is used for getting resources from authorization server.
 *
 * @param authorizationServerUrl url address of authorization server.
 * @param restTemplate is used for sending requests to authorization server.
 */
@Service
class UserServiceImpl(

        @Value("\${auth.server.url}")
        private val authorizationServerUrl: String,

        private val restTemplate: RestTemplate
) : UserService {

    /** Logger of this class. */
    private val logger = LoggerFactory.getLogger(UserServiceImpl::class.java)

    /**
     * Method request user email from authorization server.
     *
     * @param principal identification of user of which email will be requested.
     * @return String with email address or empty string.
     */
    override fun getUserEmail(principal: Principal): String {

        try {
            if (principal !is OAuth2Authentication || principal.details !is OAuth2AuthenticationDetails) {
                logger.warn("Can not get users principal")
                return ""
            }

            val header = HttpHeaders()
            header.setBearerAuth((principal.details as OAuth2AuthenticationDetails).tokenValue)
            val httpEntity = HttpEntity<Any>(header)

            logger.debug("Sending get user request to authorization server.")
            val entity = restTemplate.exchange(
                    "$authorizationServerUrl/user?username=${principal.name}",
                    HttpMethod.GET,
                    httpEntity,
                    String::class.java)

            val json = JsonParser().parse(entity.body)
            return json.asJsonObject["email"].asString

        } catch (e: Exception) {
            logger.error("Can not get user details", e)
            return ""
        }
    }
}