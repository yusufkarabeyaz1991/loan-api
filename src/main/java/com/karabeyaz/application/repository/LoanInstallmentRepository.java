package com.karabeyaz.application.repository;

import com.karabeyaz.application.entity.LoanInstallment;
import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LoanInstallmentRepository extends CrudRepository<LoanInstallment, Long> {
    List<LoanInstallment> findAllByLoanId(long loanId);
}
