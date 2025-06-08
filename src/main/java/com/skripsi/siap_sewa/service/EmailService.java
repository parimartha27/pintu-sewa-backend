package com.skripsi.siap_sewa.service;

import com.skripsi.siap_sewa.exception.EmailSendingException;
import com.skripsi.siap_sewa.utils.CommonUtils;
import com.skripsi.siap_sewa.utils.Constant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    @Value("${spring.mail.username}")
    private String emailSender;

    private final JavaMailSender mailSender;


    @Async("taskExecutor")
    public void sendEmail(String recipientEmail, int subject, String otpOrShopName) {

        validateInput(recipientEmail, otpOrShopName);
        log.info("Attempting to send email to: {}, subject: {}", recipientEmail, subject);

        try {
            SimpleMailMessage message = createEmailMessage(recipientEmail, subject, otpOrShopName);
            mailSender.send(message);

            log.info("Email successfully sent to: {}", recipientEmail);

        } catch (MailException ex) {
            log.error("Failed to send email to {}: {}", recipientEmail, ex.getMessage(), ex);
            throw new EmailSendingException(ex.getMessage());
        } catch (Exception ex) {
            log.error("Unexpected error while sending email to {}: {}", recipientEmail, ex.getMessage(), ex);
            throw new EmailSendingException(ex.getMessage());
        }
    }

    private void validateInput(String recipientEmail, String otpOrShopName) {
        if (!StringUtils.hasText(recipientEmail)) {
            log.error("Recipient email cannot be null or empty");
            throw new IllegalArgumentException("Recipient email cannot be null or empty");
        }

        if (!StringUtils.hasText(otpOrShopName)) {
            log.error("OTP cannot be null or empty");
            throw new IllegalArgumentException("OTP cannot be null or empty");
        }
    }

    private SimpleMailMessage createEmailMessage(String recipientEmail, int subject, String otpOrShopName) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(emailSender);
        message.setTo(recipientEmail);
        message.setSubject(getSubjectText(subject));
        message.setText(getBodyEmail(subject, otpOrShopName));
        return message;
    }

    private String getSubjectText(int subject) {
        return subject == 0 ?
                Constant.SUBJECT_EMAIL_REGISTER :
                Constant.SUBJECT_EMAIL_CREATE_SHOP;
    }

    private String getBodyEmail(int subject, String otpOrShopName) {
        return subject == 0?
                CommonUtils.generateOtpMessage(otpOrShopName) :
                CommonUtils.generateGreeting(otpOrShopName);
    }
}
