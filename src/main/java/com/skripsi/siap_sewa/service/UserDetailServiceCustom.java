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

@Component
@RequiredArgsConstructor
public class UserDetailServiceCustom implements UserDetailsService {

    private final CustomerRepository customerRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        List<CustomerEntity> customers = customerRepository.findByUsername(username);
        if (customers.isEmpty()) {
            throw new UsernameNotFoundException("User not found");
        } else if (customers.size() > 1) {
            throw new SecurityException("User is duplicate");
        }
        return new CustomerPrincipal(customers.getFirst());
    }

    public UserDetails loadUserById(String id) throws UsernameNotFoundException {
        CustomerEntity customer = customerRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));
        return new CustomerPrincipal(customer);
    }
}