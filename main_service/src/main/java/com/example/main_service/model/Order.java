package com.example.main_service.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Table("orders")
public class Order {
    @Id
    private Long id;

    @Column("user_id")
    private Long userId;

    @Column("total_amount")
    private double totalAmount;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("status")
    private String status;

    @Transient
    private List<OrderItem> items;
}
