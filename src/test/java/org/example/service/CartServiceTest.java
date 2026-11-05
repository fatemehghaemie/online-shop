package org.example.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;
import org.example.model.Account;
import org.example.model.Cart;
import org.example.model.Money;
import org.example.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CartServiceTest {

  private static final int STOCK = 10;

  private ShopFixture shop;
  private Account customer;
  private Product product;

  @BeforeEach
  void setUp() {
    shop = new ShopFixture();
    customer = shop.registerCustomer("buyer@example.com", "09121234567");
    product = shop.createProduct("Clean Code", "35.00", STOCK, shop.createCategory("Books"));
  }

  private int quantityInCart() {
    Cart cart = shop.cartService.getCart(customer.getId());
    return cart.getLines().get(0).getQuantity().getValue();
  }

  @Test
  void updateQuantityReplacesTheQuantityInsteadOfAddingToIt() {
    shop.cartService.addProduct(customer.getId(), product.getId(), 2);

    shop.cartService.updateQuantity(customer.getId(), product.getId(), 5);

    assertEquals(5, quantityInCart());
  }

  @Test
  void updateQuantityRejectsMoreThanTheStockAndKeepsTheCartUnchanged() {
    shop.cartService.addProduct(customer.getId(), product.getId(), 2);

    assertThrows(
        IllegalArgumentException.class,
        () -> shop.cartService.updateQuantity(customer.getId(), product.getId(), STOCK + 1));
    assertEquals(2, quantityInCart());
  }

  @Test
  void updateQuantityRejectsAProductThatIsNotInTheCart() {
    assertThrows(
        IllegalArgumentException.class,
        () -> shop.cartService.updateQuantity(customer.getId(), product.getId(), 1));
    assertThrows(
        IllegalArgumentException.class,
        () -> shop.cartService.updateQuantity(customer.getId(), UUID.randomUUID(), 1));
  }

  @Test
  void clearCartEmptiesTheCartAndZeroesTheTotal() {
    shop.cartService.addProduct(customer.getId(), product.getId(), 3);

    shop.cartService.clearCart(customer.getId());

    Cart cart = shop.cartService.getCart(customer.getId());
    assertTrue(cart.getLines().isEmpty());
    assertEquals(Money.zero(Money.DEFAULT_CURRENCY), cart.calculateTotal());
  }
}