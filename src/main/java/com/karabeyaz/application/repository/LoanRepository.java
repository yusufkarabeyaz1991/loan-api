package com.karabeyaz.application.repository;

import com.karabeyaz.application.entity.Loan;
import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LoanRepository extends CrudRepository<Loan, Long> {

    List<Loan> findAllByCustomerId(long customerId);
}
