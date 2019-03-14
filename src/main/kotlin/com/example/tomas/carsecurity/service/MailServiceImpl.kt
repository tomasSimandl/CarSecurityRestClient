package com.example.tomas.carsecurity.service

import com.example.tomas.carsecurity.model.Event
import org.slf4j.LoggerFactory
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service
import java.security.Principal

@Service
class MailServiceImpl(
        private val mailSender: JavaMailSender,
        private val userService: UserService
): MailService {

    private val logger = LoggerFactory.getLogger(MailServiceImpl::class.java)

    override fun sendEvent(event: Event, principal: Principal) {

        when(event.eventType.id){
            3L,4L,5L,6L,7L -> {
                val subject = event.eventType.name
                val text = event.note

                sendMail(principal, subject, text)
            }
            else -> logger.info("Unsupported mail message")
        }
    }

    private fun sendMail(principal: Principal, subject: String, body: String) {

        Thread {
            logger.debug("Thread to send email started.")

            val to: String = userService.getUserEmail(principal)
            if (to.isBlank()) {
                logger.debug("Can not send email: Destination address is empty.")
                return@Thread
            }

            try {
                val message = SimpleMailMessage()
                message.setTo(to)
                message.setSubject(subject)
                message.setText(body)
                mailSender.send(message)
            } catch (e: Exception){
                logger.error("Can not send email", e)
            }
        }.start()
    }
}