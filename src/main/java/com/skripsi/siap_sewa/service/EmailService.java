package com.skripsi.siap_sewa.service;

import com.skripsi.siap_sewa.utils.Constant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendEmail(String recepientEmail, String subject, String message) {
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setFrom(Constant.EMAIL_SENDER);
        simpleMailMessage.setTo(recepientEmail);
        simpleMailMessage.setSubject(subject);
        simpleMailMessage.setText(message);
        mailSender.send(simpleMailMessage);
        log.info("Email already sent into: {}", recepientEmail);
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
