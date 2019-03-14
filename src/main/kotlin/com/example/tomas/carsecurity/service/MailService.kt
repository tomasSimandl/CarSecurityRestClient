package com.example.tomas.carsecurity.service

import com.example.tomas.carsecurity.model.Event
import java.security.Principal

interface MailService {

    fun sendEvent(event: Event, principal: Principal)
}