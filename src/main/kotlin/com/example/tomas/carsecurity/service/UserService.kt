package com.example.tomas.carsecurity.service

import java.security.Principal

interface UserService {

    fun getUserEmail(principal: Principal): String
}