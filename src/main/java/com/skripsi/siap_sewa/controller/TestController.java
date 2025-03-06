package com.skripsi.siap_sewa.controller;

import com.skripsi.siap_sewa.dto.authentication.CustomerPrincipal;
import com.skripsi.siap_sewa.entity.CustomerEntity;
import com.skripsi.siap_sewa.repository.CustomerRepository;
import com.skripsi.siap_sewa.service.EmailService;
import com.skripsi.siap_sewa.service.JWTService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestController {

    private final EmailService emailService;
    private final JWTService jwtService;
    private final CustomerRepository customerRepository;

    @GetMapping("/hello")
    public String hello() {
        return "PINTU SEWA";
    }

    @GetMapping("/email")
    public void email(){
        emailService.sendEmailTest();
    }

    @PostMapping("/token")
    public String getToken(@RequestParam String id){
        CustomerEntity customer = customerRepository.findById(id).orElse(null);
        return jwtService.generateToken(new CustomerPrincipal(customer));
    }

}
