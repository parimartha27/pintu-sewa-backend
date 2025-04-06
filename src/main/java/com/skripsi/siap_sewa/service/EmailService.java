package com.skripsi.siap_sewa.service;

import com.skripsi.siap_sewa.enums.ErrorMessageEnum;
import com.skripsi.siap_sewa.exception.EmailSendingException;
import com.skripsi.siap_sewa.utils.Constant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    @Value("${spring.mail.username}")
    private String emailSender;

    private final JavaMailSender mailSender;

    public void sendEmail(String recipientEmail, String subject, String message) {
        try {
            log.info("Attempting to send email to: {}, subject: {}", recipientEmail, subject);

            if (recipientEmail == null || recipientEmail.isEmpty()) {
                log.error("Recipient email cannot be null or empty");
                throw new IllegalArgumentException("Recipient email cannot be null or empty");
            }

            SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
            simpleMailMessage.setFrom(emailSender);
            simpleMailMessage.setTo(recipientEmail);
            simpleMailMessage.setSubject(subject);
            simpleMailMessage.setText(message);

            mailSender.send(simpleMailMessage);
            log.info("Email successfully sent to: {}", recipientEmail);

        } catch (MailException ex) {
            log.error("Failed to send email to {}: {}", recipientEmail, ex.getMessage(), ex);
            throw new EmailSendingException("Failed to send email to " + recipientEmail);
        } catch (IllegalArgumentException ex) {
            log.error("Invalid email parameters: {}", ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            log.error("Unexpected error while sending email to {}: {}", recipientEmail, ex.getMessage(), ex);
            throw new EmailSendingException("Unexpected error while sending email");
        }
    }

    public void sendEmailTest( ) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("paparimartha27@gmail.com");
        message.setTo("aguspari86@gmail.com");
        message.setText("Test send email");
        message.setSubject("SIAP-SEWA Register");
        mailSender.send(message);
        log.info("Mail Send...");

    }
}


