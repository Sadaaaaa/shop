package com.example.shop.controller;

import com.example.shop.model.Cart;
import com.example.shop.model.Product;
import com.example.shop.service.CartService;
import com.example.shop.service.ProductService;
import jakarta.servlet.http.HttpSession;
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
import java.util.Optional;

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

        // Получаем текущее количество товаров в корзине
//        Map<Long, Integer> cartItems = cartService.getCartItemsQuantity(MOCK_USER);
        Cart cart = cartService.getCart(MOCK_USER);

        model.addAttribute("products", products.getContent());
        model.addAttribute("totalPages", products.getTotalPages());
        model.addAttribute("currentPage", page);
//        model.addAttribute("cartItems", cartItems); // Добавляем счетчик товаров в модель
        model.addAttribute("cart", cart);



//        model.addAttribute("param", Map.of("search", search == null ? "" : search, "sort", sort  == null ? "" : sort, "size", size));
        Map<String, Object> params = new HashMap<>();
        params.put("search", search == null ? "" : search);
        params.put("sort", sort == null ? "" : sort);
        params.put("size", size); // int

        model.addAttribute("param", params);
        log.info("Current size: {}", params);
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
        // Получаем продукт по id
        Product productOpt = productService.findProductById(id);

        // Получаем корзину из сессии
        Cart cart = cartService.getCart(MOCK_USER);

        // Добавляем продукт и корзину в модель
        model.addAttribute("product", productOpt);
        model.addAttribute("cart", cart);

        return "item"; // Возвращаем шаблон item.html
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
