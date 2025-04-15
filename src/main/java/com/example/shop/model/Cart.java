package com.example.shop.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Table;

import java.util.ArrayList;
import java.util.List;

@Data
@Table("carts")
public class Cart {
    @Id
    private Long id;
    private Long userId;
    
    @Transient
    private List<CartItem> items = new ArrayList<>();
}
