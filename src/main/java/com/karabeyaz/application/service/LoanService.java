package com.karabeyaz.application.service;

import com.karabeyaz.application.constant.ErrorConstants;
import com.karabeyaz.application.entity.Customer;
import com.karabeyaz.application.entity.Loan;
import com.karabeyaz.application.entity.LoanInstallment;
import com.karabeyaz.application.model.CreateLoanRequest;
import com.karabeyaz.application.model.PayLoanRequest;
import com.karabeyaz.application.model.PayLoanResponse;
import com.karabeyaz.application.repository.CustomerRepository;
import com.karabeyaz.application.repository.LoanInstallmentRepository;
import com.karabeyaz.application.repository.LoanRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LoanService {

    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    private final CustomerRepository customerRepository;
    private final LoanRepository loanRepository;
    private final LoanInstallmentRepository loanInstallmentRepository;

    private final String TIMEZONE_OF_ISTANBUL = "Europe/Istanbul";

    public static boolean userHasAuthority(Authentication authentication, String authority) {
        return authentication.getAuthorities().stream().anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals(authority));
    }

    public Loan createLoan(Authentication authentication, CreateLoanRequest request) {

        if (!checkRole(authentication, request.getCustomerTckn())) {
            throw new RuntimeException(ErrorConstants.PERMISSION_ERROR);
        }

        Customer customer = customerRepository.findByTckn(request.getCustomerTckn()).orElseThrow(() -> new RuntimeException(ErrorConstants.CUSTOMER_NOT_FOUND));

        Loan loan = createLoan(request, customer);
        createLoanInstallments(loan);

        return loan;
    }

    private void createLoanInstallments(Loan loan) {

        BigDecimal totalLoanAmount = loan.getLoanAmount().multiply(BigDecimal.ONE.add(loan.getInterestRate()));
        BigDecimal loanInstallmentAmount = totalLoanAmount.divide(BigDecimal.valueOf(loan.getNumberOfInstallment()), RoundingMode.CEILING);

        for (int i = 1; i <= loan.getNumberOfInstallment(); i++) {
            ZonedDateTime istZonedTime = loan.getCreatedDate().withDayOfMonth(1).plusMonths(i);
            LoanInstallment loanInstallment = LoanInstallment.builder().loanId(loan.getId()).amount(loanInstallmentAmount).paidAmount(BigDecimal.ZERO)
                    .dueDate(istZonedTime).isPaid(false).build();
            loanInstallmentRepository.save(loanInstallment);
        }
    }

    private Loan createLoan(CreateLoanRequest request, Customer customer) {
        if (!checkLimit(request.getLoanAmount(), customer)) {
            throw new RuntimeException(ErrorConstants.INSUFFICIENT_LIMIT);
        }
        Loan loan = Loan.builder().customerId(customer.getId()).loanAmount(request.getLoanAmount()).interestRate(request.getInterestRate())
                .numberOfInstallment(request.getNumberOfInstallment()).createdDate(Instant.now().atZone(ZoneId.of(TIMEZONE_OF_ISTANBUL))).isPaid(false).build();
        return loanRepository.save(loan);

    }

    private boolean checkLimit(BigDecimal loanAmount, Customer customer) {

        return loanAmount.compareTo(customer.getCreditLimit().subtract(customer.getUsedCreditLimit())) <= 0;
    }

    private boolean checkRole(Authentication authentication, String customerTckn) {

        if (userHasAuthority(authentication, ROLE_ADMIN)) {
            return true;
        }

        return authentication.getName().equals(customerTckn);
    }

    public List<Loan> listLoans(Authentication authentication, String tckn) {

        if (!checkRole(authentication, tckn)) {
            throw new RuntimeException(ErrorConstants.PERMISSION_ERROR);
        }
        Customer customer = customerRepository.findByTckn(tckn).orElseThrow(() -> new RuntimeException(ErrorConstants.CUSTOMER_NOT_FOUND));
        return loanRepository.findAllByCustomerId(customer.getId());
    }

    public List<LoanInstallment> listInstallments(Authentication authentication, String loanId) {
        Loan loan = loanRepository.findById(Long.parseLong(loanId)).orElseThrow(() -> new RuntimeException(ErrorConstants.LOAN_NOT_FOUND));
        return getLoanInstallments(authentication, loan);
    }

    private List<LoanInstallment> getLoanInstallments(Authentication authentication, Loan loan) {

        Customer customer = customerRepository.findById(loan.getCustomerId()).orElseThrow(() -> new RuntimeException(ErrorConstants.CUSTOMER_NOT_FOUND));
        if (!checkRole(authentication, customer.getTckn())) {
            throw new RuntimeException(ErrorConstants.PERMISSION_ERROR);
        }
        return loanInstallmentRepository.findAllByLoanId(loan.getId());
    }

    @Transactional(rollbackFor=Exception.class)
    public PayLoanResponse payLoan(Authentication authentication, PayLoanRequest request) {

        Loan loan = loanRepository.findById(request.getLoanId()).orElseThrow(() -> new RuntimeException(ErrorConstants.LOAN_NOT_FOUND));
        List<LoanInstallment> loanInstallments = getLoanInstallments(authentication, loan);
        List<LoanInstallment> unpaidInstallments = loanInstallments.stream().filter(l -> !l.isPaid())
                .sorted(Comparator.comparing(LoanInstallment::getDueDate)).toList();

        ZonedDateTime nowAtTr = Instant.now().atZone(ZoneId.of(TIMEZONE_OF_ISTANBUL));

        BigDecimal totalSpentAmount = BigDecimal.ZERO;
        int paidInstallmentCount = 0;
        for (LoanInstallment loanInstallment : unpaidInstallments) {
            BigDecimal loanAmountForNow = calcLoanForNow(nowAtTr, loanInstallment);
            if (checkAmount(request, totalSpentAmount, loanAmountForNow) && checkDueDate(nowAtTr, loanInstallment.getDueDate())) {
                totalSpentAmount = totalSpentAmount.add(loanAmountForNow);
                paidInstallmentCount++;
                payInstallment(nowAtTr, loanInstallment);
            } else {
                return new PayLoanResponse(paidInstallmentCount, totalSpentAmount, false);
            }
        }
        loan.setPaid(true);
        loanRepository.save(loan);
        return new PayLoanResponse(paidInstallmentCount, totalSpentAmount, true);
    }

    private void payInstallment(ZonedDateTime nowAtTr, LoanInstallment loanInstallment) {
        loanInstallment.setPaidAmount(loanInstallment.getAmount());
        loanInstallment.setPaymentDate(nowAtTr);
        loanInstallment.setPaid(true);
        loanInstallmentRepository.save(loanInstallment);
    }

    private BigDecimal calcLoanForNow(ZonedDateTime nowAtTr, LoanInstallment loanInstallment) {
        long days = ChronoUnit.DAYS.between(nowAtTr, loanInstallment.getDueDate());
        return loanInstallment.getAmount().subtract(loanInstallment.getAmount().multiply(BigDecimal.valueOf(days)).multiply(BigDecimal.valueOf(0.001)));
    }

    private static boolean checkAmount(PayLoanRequest request, BigDecimal totalSpentAmount, BigDecimal loanAmountForNow) {
        return totalSpentAmount.add(loanAmountForNow).compareTo(request.getAmount()) <= 0;
    }

    private boolean checkDueDate(ZonedDateTime now, ZonedDateTime dueDate) {

        return now.plusMonths(3).isAfter(dueDate);
    }
}
