package com.example.starter.order;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, updatable = false)
  private Instant createdAt;

  @PrePersist
  void createdAt() {
    if (this.createdAt == null) this.createdAt = Instant.now();
  }

  @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<OrderItem> items = new ArrayList<>();

  public BigDecimal getTotal() {
    return items.stream()
        .map(OrderItem::getLineTotal)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }
}
