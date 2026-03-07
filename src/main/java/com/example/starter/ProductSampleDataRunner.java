package com.example.starter;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.example.starter.product.Product;
import com.example.starter.product.ProductRepository;

import java.math.BigDecimal;
import java.math.BigInteger;

@Component
public class ProductSampleDataRunner implements CommandLineRunner {

  private final ProductRepository productRepository;

  public ProductSampleDataRunner(ProductRepository productRepository) {
    this.productRepository = productRepository;
  }

  @Override
  public void run(String... args) {
    if (productRepository.count() > 0) {
      return;
    }
    productRepository.save(Product.builder()
        .name("Handmade Candle")
        .description("A hand-poured candle with natural wax.")
        .price(new BigDecimal("12.99"))
        .quantity(BigInteger.valueOf(50))
        .build());
    productRepository.save(Product.builder()
        .name("Lavender Candle")
        .description("Soothing lavender-scented candle.")
        .price(new BigDecimal("14.99"))
        .quantity(BigInteger.valueOf(30))
        .build());
    productRepository.save(Product.builder()
        .name("Vanilla Candle")
        .description("Warm vanilla fragrance.")
        .price(new BigDecimal("11.99"))
        .quantity(BigInteger.valueOf(40))
        .build());
  }
}
