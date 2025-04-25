package com.example.payment.repository;

import com.example.payment.model.PaymentAccount;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentAccountRepository extends R2dbcRepository<PaymentAccount, Long> {
} 