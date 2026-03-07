package com.example.starter.product;

import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigInteger;

@Controller
@RequestMapping("/products")
public class ProductController {

  private final ProductRepository productRepository;

  public ProductController(ProductRepository productRepository) {
    this.productRepository = productRepository;
  }

  @GetMapping
  public String listProducts(Model model) {
    model.addAttribute("products", productRepository.findAll());
    return "products";
  }

  @GetMapping("/new")
  public String newProductForm(Model model) {
    model.addAttribute("product", new Product());
    return "new-product";
  }

  @PostMapping
  @Transactional
  public String createProduct(@ModelAttribute Product product, BindingResult bindingResult) {
    validateQuantity(product.getQuantity(), bindingResult);
    if (bindingResult.hasErrors()) {
      return "new-product";
    }
    productRepository.save(product);
    return "redirect:/products";
  }

  private void validateQuantity(BigInteger quantity, BindingResult bindingResult) {
    if (quantity == null) {
      bindingResult.rejectValue("quantity", "quantity.required", "Stock quantity is required.");
      return;
    }
    if (quantity.signum() < 0) {
      bindingResult.rejectValue("quantity", "quantity.min", "Quantity must be 0 or greater.");
    }
  }
}
