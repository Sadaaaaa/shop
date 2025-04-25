package com.example.payment.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table("payment_account")
public class PaymentAccount {
    @Id
    private Long id;
    
    @Column("user_id")
    private Long userId;
    
    private Double amount;
} 