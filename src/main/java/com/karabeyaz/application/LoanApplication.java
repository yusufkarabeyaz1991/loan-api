package com.karabeyaz.application;

import com.karabeyaz.application.entity.Customer;
import com.karabeyaz.application.enums.UserRole;
import com.karabeyaz.application.repository.CustomerRepository;
import java.math.BigDecimal;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class LoanApplication {

    public static void main(String[] args) {
        SpringApplication.run(LoanApplication.class, args);
    }

    @Bean
    public CommandLineRunner run(CustomerRepository customerRepository) {
        return (String[] args) -> {
            Customer adminCustomer = Customer.builder().role(UserRole.ADMIN).tckn("11111111111")
                    .password("$2a$12$aVdFEibSW52mi9O11zVD4exPcudlZuSATLsJPPM7jgWEP5Ottyi7K").name("admin").surname("admin")
                    .creditLimit(BigDecimal.valueOf(1_000_000)).usedCreditLimit(BigDecimal.ZERO).build();
            Customer customer1 = Customer.builder().role(UserRole.USER).tckn("11111111112")
                    .password("$2a$12$YiuQgoTEMRGyR3/PHZHfYe6Pj/M13/ddxEaOjn3A7R8nJqaX90dOS").name("yusuf").surname("karabeyaz")
                    .creditLimit(BigDecimal.valueOf(1_000_000)).usedCreditLimit(BigDecimal.ZERO).build();
            Customer customer2 = Customer.builder().role(UserRole.USER).tckn("11111111113")
                    .password("$2a$12$SXcR3SYKWHGsladwNcVJ7e0ybsrSfaq2AePQC/gbDvmZ24KVb7KDe").name("birce").surname("karabeyaz")
                    .creditLimit(BigDecimal.valueOf(2_000_000)).usedCreditLimit(BigDecimal.ZERO).build();
            customerRepository.save(adminCustomer);
            customerRepository.save(customer1);
            customerRepository.save(customer2);
            customerRepository.findAll().forEach(System.out::println);
        };
    }
}
