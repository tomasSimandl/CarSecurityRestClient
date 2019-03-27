package com.example.tomas.carsecurity.service

import java.security.Principal

/**
 * Service used for getting resources from authorization server.
 */
interface UserService {

    /**
     * Method request user email from authorization server.
     *
     * @param principal identification of user of which email will be requested.
     * @return String with email address or empty string.
     */
    fun getUserEmail(principal: Principal): String
}