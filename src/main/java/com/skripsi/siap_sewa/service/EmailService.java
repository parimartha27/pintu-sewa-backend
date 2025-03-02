package com.skripsi.siap_sewa.service;

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

    public void sendEmail( ) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("sender@gmail.com");
        message.setTo("penerima.com");
        message.setText("Test send email");
        message.setSubject("SIAP-SEWA Register");
        mailSender.send(message);
        log.info("Mail Send...");

    }

}
