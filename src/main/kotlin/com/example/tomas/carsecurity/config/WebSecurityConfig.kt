package com.example.tomas.carsecurity.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationManager
import org.springframework.security.oauth2.provider.token.RemoteTokenServices
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices


@Configuration
@EnableWebSecurity
class WebSecurityConfig(
        @Value("\${oauth2.check.token.url}")
        private val checkTokenUrl: String,

        @Value("\${oauth2.client.client-id}")
        private val clientId: String,

        @Value("\${oauth2.client.client-secret}")
        private val clientSecret: String
) : WebSecurityConfigurerAdapter() {

    override fun configure(http: HttpSecurity) {
        http
                .authorizeRequests()
                .anyRequest()
                .authenticated()
    }

    @Bean
    fun tokenServices(): ResourceServerTokenServices {
        val tokenServices = RemoteTokenServices()
        tokenServices.setClientId(clientId)
        tokenServices.setClientSecret(clientSecret)
        tokenServices.setCheckTokenEndpointUrl(checkTokenUrl)
        return tokenServices
    }

    override fun authenticationManagerBean(): AuthenticationManager {
        val authenticationManager = OAuth2AuthenticationManager()
        authenticationManager.setTokenServices(tokenServices())
        return authenticationManager
    }
}