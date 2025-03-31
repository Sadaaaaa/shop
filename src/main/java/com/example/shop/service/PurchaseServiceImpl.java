package com.example.shop.service;

import com.example.shop.model.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PurchaseServiceImpl implements PurchaseService {
    private final OrderService orderService;

    @Autowired
    public PurchaseServiceImpl(OrderService orderService) {
        this.orderService = orderService;
    }


}
