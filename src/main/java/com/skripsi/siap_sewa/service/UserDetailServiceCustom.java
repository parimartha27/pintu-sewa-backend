package com.skripsi.siap_sewa.service;

import com.skripsi.siap_sewa.dto.authentication.CustomerPrincipal;
import com.skripsi.siap_sewa.entity.CustomerEntity;
import com.skripsi.siap_sewa.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserDetailServiceCustom implements UserDetailsService {

    private final CustomerRepository customerRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<CustomerEntity> customer = customerRepository.findByEmail(email);
        if (customer.isEmpty()) {
            throw new UsernameNotFoundException("User not found");
        }
        return new CustomerPrincipal(customer.get());
    }

    public UserDetails loadUserById(String id) throws UsernameNotFoundException {
        CustomerEntity customer = customerRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));
        return new CustomerPrincipal(customer);
    }
}