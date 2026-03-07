package com.example.starter.cart;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Session-scoped representation of the shopping cart.
 * Maps product ID to quantity.
 */
public class Cart {

  private final Map<Long, Integer> productIdToQuantity = new ConcurrentHashMap<>();

  public void add(long productId, int quantity) {
    productIdToQuantity.merge(productId, quantity, Integer::sum);
  }

  public void setQuantity(long productId, int quantity) {
    if (quantity <= 0) {
      productIdToQuantity.remove(productId);
    } else {
      productIdToQuantity.put(productId, quantity);
    }
  }

  public void remove(long productId) {
    productIdToQuantity.remove(productId);
  }

  public Map<Long, Integer> getProductIdToQuantity() {
    return new HashMap<>(productIdToQuantity);
  }

  public int getTotalItems() {
    return productIdToQuantity.values().stream().mapToInt(Integer::intValue).sum();
  }

  public boolean isEmpty() {
    return productIdToQuantity.isEmpty();
  }

  public void clear() {
    productIdToQuantity.clear();
  }
}
