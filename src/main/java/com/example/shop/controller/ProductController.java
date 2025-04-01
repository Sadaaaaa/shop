package com.example.shop.controller;

import com.example.shop.model.Cart;
import com.example.shop.model.Product;
import com.example.shop.service.CartService;
import com.example.shop.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/")
public class ProductController {

    private static final Long MOCK_USER = 1L;


    @Autowired
    private ProductService productService;

    @Autowired
    private CartService cartService;

    @GetMapping
    public String showProducts(@RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "10") int size,
                               @RequestParam(required = false) String sort,
                               @RequestParam(required = false) String search,
                               Model model) {

        Pageable pageable = PageRequest.of(page, size, getSort(sort));
        Page<Product> products;

        if (search != null && !search.isEmpty()) {
            products = productService.findProductsByNameOrDescription(search, pageable);
        } else {
            products = productService.findAllProducts(pageable);
        }

        Cart cart = cartService.getCart(MOCK_USER);

        model.addAttribute("products", products.getContent());
        model.addAttribute("totalPages", products.getTotalPages());
        model.addAttribute("currentPage", page);
        model.addAttribute("cart", cart);

        Map<String, Object> params = new HashMap<>();
        params.put("search", search == null ? "" : search);
        params.put("sort", sort == null ? "" : sort);
        params.put("size", size);

        model.addAttribute("param", params);
        return "products";
    }

    private Sort getSort(String sort) {
        if (sort == null) return Sort.unsorted();
        return switch (sort) {
            case "name_asc" -> Sort.by("name").ascending();
            case "name_desc" -> Sort.by("name").descending();
            case "price_asc" -> Sort.by("price").ascending();
            case "price_desc" -> Sort.by("price").descending();
            default -> Sort.unsorted();
        };
    }

    @GetMapping("/products/{id}")
    public String viewProduct(@PathVariable Long id, Model model) {
        Product productOpt = productService.findProductById(id);

        Cart cart = cartService.getCart(MOCK_USER);

        model.addAttribute("product", productOpt);
        model.addAttribute("cart", cart);

        return "item";
    }

    @GetMapping("/products/image/{id}")
    public ResponseEntity<byte[]> getProductImage(@PathVariable Long id) {
        Product product = productService.findProductById(id);
        if (product.getImage() != null) {
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(product.getImage());
        }
        return ResponseEntity.notFound().build();
    }
}
