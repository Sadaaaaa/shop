package com.example.shop.controller;

import com.example.shop.model.Product;
import com.example.shop.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Controller
@RequestMapping("/admin")
public class AdminController {
    @Autowired
    private ProductService productService;

    @GetMapping
    public String adminPanel(Model model) {
        model.addAttribute("products", productService.getAllProducts());
        model.addAttribute("newProduct", new Product());
        return "admin/panel";
    }

    @PostMapping("/products/add")
    public String addProduct(@RequestParam("name") String name,
                             @RequestParam("description") String description,
                             @RequestParam("price") double price,
                             @RequestParam("image") MultipartFile image,
                             Model model) {
        try {
            Product product = Product.builder()
                    .name(name)
                    .description(description)
                    .price(price)
                    .image(image.isEmpty() ? null : image.getBytes())
                    .build();

            productService.saveProduct(product);
        } catch (IOException e) {
            model.addAttribute("error", "Ошибка загрузки изображения: " + e.getMessage());
            return "admin/panel";
        }
        return "redirect:/admin";
    }

    @PostMapping("/products/delete/{id}")
    public String deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return "redirect:/admin";
    }
} 