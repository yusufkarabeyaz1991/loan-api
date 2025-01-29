package com.karabeyaz.application.model;

import java.io.Serializable;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateLoanRequest implements Serializable {

    private String customerTckn;
    private BigDecimal loanAmount;
    private BigDecimal interestRate;
    private int numberOfInstallment;

}
