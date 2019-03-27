package com.example.tomas.carsecurity.service

import com.example.tomas.carsecurity.model.Event
import org.slf4j.LoggerFactory
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service
import java.security.Principal

/**
 * Implementation of service which sending mail messages.
 *
 * @param mailSender is used for sending Mail messages.
 * @param userService which is used for getting users email address.
 */
@Service
class MailServiceImpl(
        private val mailSender: JavaMailSender,
        private val userService: UserService
) : MailService {

    /** Logger of this class. */
    private val logger = LoggerFactory.getLogger(MailServiceImpl::class.java)

    /**
     * Method sends event to user specified by [principal]. Event is send only when event type id is in [3,4,5,6,7],
     * other events are ignored.
     *
     * @param event which will be send to user over mail.
     * @param principal which identifies receiver of email.
     */
    override fun sendEvent(event: Event, principal: Principal) {

        when (event.eventType.id) {
            3L, 4L, 5L, 6L, 7L -> {
                val subject = event.eventType.name
                val text = event.note

                sendMail(principal, subject, text)
            }
            else -> logger.info("Unsupported mail message")
        }
    }

    /**
     * Method get users email address over [UserService] and send mail message with given [subject] and [body].
     *
     * @param principal which identifies receiver of mail.
     * @param subject of mail message.
     * @param body of mail message.
     */
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
            } catch (e: Exception) {
                logger.error("Can not send email", e)
            }
        }.start()
    }
}