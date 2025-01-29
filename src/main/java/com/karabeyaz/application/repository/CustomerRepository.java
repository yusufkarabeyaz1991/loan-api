package com.karabeyaz.application.repository;

import com.karabeyaz.application.entity.Customer;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends CrudRepository<Customer, Long> {

    Optional<Customer> findByTckn(String tckn);
}
