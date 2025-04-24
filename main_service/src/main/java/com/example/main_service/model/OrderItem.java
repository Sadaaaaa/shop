package com.example.main_service.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table("order_items")
public class OrderItem {
    @Id
    private Long id;
    
    @Column("order_id")
    private Long orderId;
    
    @Column("product_id")
    private Long productId;
    
    private Integer quantity;
    private Double price;
    
    @Transient
    private Product product;

}