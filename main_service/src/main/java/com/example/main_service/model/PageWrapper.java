package com.example.main_service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageWrapper<T> {
    private List<T> content;
    private long totalElements;
    private int number;
    private int size;

    public static <T> PageWrapper<T> fromPage(Page<T> page) {
        return new PageWrapper<>(
                page.getContent(),
                page.getTotalElements(),
                page.getNumber(),
                page.getSize()
        );
    }

    public Page<T> toPage() {
        return new PageImpl<>(content, PageRequest.of(number, size), totalElements);
    }
} 