package com.example.tomas.carsecurity.service

import com.example.tomas.carsecurity.model.Event
import java.security.Principal

/**
 * Service used for sending Mail messages.
 */
interface MailService {

    /**
     * Method send email with specific [event] to user with given by [principal].
     *
     * @param event which will be send to user over mail.
     * @param principal which identifies receiver of email.
     */
    fun sendEvent(event: Event, principal: Principal)
}