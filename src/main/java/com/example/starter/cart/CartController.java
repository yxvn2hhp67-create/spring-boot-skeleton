package com.example.starter.cart;

import com.example.starter.order.Order;
import com.example.starter.order.OrderItem;
import com.example.starter.order.OrderRepository;
import com.example.starter.product.Product;
import com.example.starter.product.ProductRepository;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/cart")
@SessionAttributes("cart")
public class CartController {

  private final ProductRepository productRepository;
  private final OrderRepository orderRepository;

  public CartController(ProductRepository productRepository, OrderRepository orderRepository) {
    this.productRepository = productRepository;
    this.orderRepository = orderRepository;
  }

  @ModelAttribute("cart")
  public Cart cart(@SessionAttribute(name = "cart", required = false) Cart sessionCart) {
    return sessionCart != null ? sessionCart : new Cart();
  }

  @GetMapping
  public String viewCart(@ModelAttribute("cart") Cart cart, Model model) {
    syncCartToStock(cart);
    model.addAttribute("cartEntries", buildCartEntries(cart));
    return "cart";
  }

  @PostMapping("/add/{productId}")
  public String addToCart(
      @PathVariable Long productId,
      @RequestParam(defaultValue = "1") int quantity,
      @ModelAttribute("cart") Cart cart,
      RedirectAttributes redirectAttributes) {
    if (quantity < 1) quantity = 1;
    Product product = productRepository.findById(productId).orElse(null);
    if (product == null) {
      redirectAttributes.addFlashAttribute("error", "Product not found.");
      return "redirect:/products";
    }
    int stock = toInt(product.getQuantity());
    int inCart = cart.getProductIdToQuantity().getOrDefault(productId, 0);
    int canAdd = Math.min(quantity, Math.max(0, stock - inCart));
    if (canAdd <= 0) {
      redirectAttributes.addFlashAttribute("error",
          inCart > 0
              ? product.getName() + ": only " + stock + " in stock (you have " + inCart + " in cart)."
              : product.getName() + ": out of stock.");
      return "redirect:/products";
    }
    cart.add(productId, canAdd);
    String msg = canAdd < quantity
        ? product.getName() + ": only " + stock + " in stock; added " + canAdd + "."
        : product.getName() + " added to cart.";
    redirectAttributes.addFlashAttribute("message", msg);
    return "redirect:/products";
  }

  @PostMapping("/update")
  public String updateCart(
      @RequestParam Map<String, String> quantities,
      @ModelAttribute("cart") Cart cart,
      RedirectAttributes redirectAttributes) {
    for (Map.Entry<String, String> e : quantities.entrySet()) {
      if (!e.getKey().startsWith("qty_")) continue;
      try {
        long productId = Long.parseLong(e.getKey().substring(4));
        int requested = Integer.parseInt(e.getValue() != null ? e.getValue() : "0");
        int capped = productRepository.findById(productId)
            .map(p -> Math.min(requested, toInt(p.getQuantity())))
            .orElse(0);
        cart.setQuantity(productId, capped);
      } catch (NumberFormatException ignored) {}
    }
    redirectAttributes.addFlashAttribute("message", "Cart updated.");
    return "redirect:/cart";
  }

  @PostMapping("/remove/{productId}")
  public String removeFromCart(
      @PathVariable Long productId,
      @ModelAttribute("cart") Cart cart,
      RedirectAttributes redirectAttributes) {
    cart.remove(productId);
    redirectAttributes.addFlashAttribute("message", "Item removed from cart.");
    return "redirect:/cart";
  }

  @PostMapping("/place-order")
  @Transactional
  public String placeOrder(
      @ModelAttribute("cart") Cart cart,
      Model model,
      RedirectAttributes redirectAttributes) {
    if (cart.isEmpty()) {
      redirectAttributes.addFlashAttribute("error", "Your cart is empty.");
      return "redirect:/cart";
    }
    List<CartEntry> entries = buildCartEntries(cart);
    List<String> errors = new ArrayList<>();
    for (CartEntry e : entries) {
      if (e.getProduct().getQuantity().compareTo(BigInteger.valueOf(e.getQuantity())) < 0) {
        errors.add(e.getProduct().getName() + ": not enough stock (available: " + e.getProduct().getQuantity() + ").");
      }
    }
    if (!errors.isEmpty()) {
      model.addAttribute("cartEntries", entries);
      model.addAttribute("errors", errors);
      return "cart";
    }
    Order order = Order.builder().build();
    for (CartEntry e : entries) {
      OrderItem item = OrderItem.builder()
          .order(order)
          .product(e.getProduct())
          .quantity(e.getQuantity())
          .unitPrice(e.getProduct().getPrice())
          .build();
      order.getItems().add(item);
      Product p = e.getProduct();
      p.setQuantity(p.getQuantity().subtract(BigInteger.valueOf(e.getQuantity())));
      productRepository.save(p);
    }
    order = orderRepository.save(order);
    cart.clear();
    redirectAttributes.addFlashAttribute("orderId", order.getId());
    redirectAttributes.addFlashAttribute("message", "Order #" + order.getId() + " placed successfully.");
    return "redirect:/cart/confirmation";
  }

  @GetMapping("/confirmation")
  public String confirmation() {
    return "order-confirmation";
  }

  /** Ensures no cart line exceeds current product stock. */
  private void syncCartToStock(Cart cart) {
    for (Map.Entry<Long, Integer> e : new ArrayList<>(cart.getProductIdToQuantity().entrySet())) {
      productRepository.findById(e.getKey()).ifPresent(product -> {
        int stock = toInt(product.getQuantity());
        if (e.getValue() > stock) {
          cart.setQuantity(e.getKey(), stock);
        }
      });
    }
  }

  private static int toInt(BigInteger value) {
    if (value == null) return 0;
    return value.min(BigInteger.valueOf(Integer.MAX_VALUE)).intValue();
  }

  private List<CartEntry> buildCartEntries(Cart cart) {
    List<CartEntry> entries = new ArrayList<>();
    for (Map.Entry<Long, Integer> e : cart.getProductIdToQuantity().entrySet()) {
      productRepository.findById(e.getKey()).ifPresent(product ->
          entries.add(new CartEntry(product, e.getValue()))
      );
    }
    return entries;
  }

  public static class CartEntry {
    private final Product product;
    private final int quantity;

    public CartEntry(Product product, int quantity) {
      this.product = product;
      this.quantity = quantity;
    }

    public Product getProduct() { return product; }
    public int getQuantity() { return quantity; }
    public boolean isAtMax() {
      return product.getQuantity().compareTo(BigInteger.ZERO) > 0
          && product.getQuantity().compareTo(BigInteger.valueOf(quantity)) <= 0;
    }
    public BigDecimal getLineTotal() {
      return product.getPrice().multiply(BigDecimal.valueOf(quantity));
    }
  }
}
