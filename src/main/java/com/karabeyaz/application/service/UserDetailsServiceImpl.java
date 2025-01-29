package com.karabeyaz.application.service;

import com.karabeyaz.application.entity.Customer;
import com.karabeyaz.application.repository.CustomerRepository;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final CustomerRepository customerRepository;

    @Override
    public UserDetails loadUserByUsername(String tckn) throws UsernameNotFoundException {
        Optional<Customer> customerOptional = customerRepository.findByTckn(tckn);
        if (customerOptional.isEmpty()) {
            throw new UsernameNotFoundException("Tckn %s does not exist".formatted(tckn));
        }
        Customer customer = customerOptional.get();

        return new User(customer.getTckn(), customer.getPassword(), getAuthorities(customer));
    }

    private Collection<? extends GrantedAuthority> getAuthorities(Customer customer) {
        return Collections.singletonList(new SimpleGrantedAuthority(customer.getRole().name()));
    }
}
