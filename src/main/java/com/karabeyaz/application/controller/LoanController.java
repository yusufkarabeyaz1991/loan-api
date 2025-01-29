package com.karabeyaz.application.controller;

import com.karabeyaz.application.entity.Loan;
import com.karabeyaz.application.entity.LoanInstallment;
import com.karabeyaz.application.model.CreateLoanRequest;
import com.karabeyaz.application.model.PayLoanRequest;
import com.karabeyaz.application.model.PayLoanResponse;
import com.karabeyaz.application.service.LoanService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService;

    @GetMapping("/hello")
    public String sayHello(Authentication authentication, @RequestParam(value = "myName", defaultValue = "World") String name) {
        return String.format("Hello %s %s!", authentication.getAuthorities(), name);
    }

    @PostMapping("/create-loan")
    Loan createLoan(Authentication authentication, @RequestBody CreateLoanRequest request) {
        return loanService.createLoan(authentication, request);
    }

    @GetMapping("/list-loans/{tckn}")
    List<Loan> listLoans(Authentication authentication, @PathVariable String tckn) {
        return loanService.listLoans(authentication, tckn);
    }

    @GetMapping("/list-installments/{loanId}")
    List<LoanInstallment> listInstallments(Authentication authentication, @PathVariable String loanId) {
        return loanService.listInstallments(authentication, loanId);
    }

    @PostMapping("/pay-loan")
    PayLoanResponse payLoan(Authentication authentication, @RequestBody PayLoanRequest request) {
        return loanService.payLoan(authentication, request);
    }
}