package org.example.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CartTest {

  private Cart cart;

  private static Money usd(String amount) {
    return new Money(new BigDecimal(amount), Money.DEFAULT_CURRENCY);
  }

  private static Product product(String name, String price) {
    return new Product(name, "", usd(price), new StockQuantity(100), UUID.randomUUID());
  }

  @BeforeEach
  void setUp() {
    cart = new Cart(UUID.randomUUID());
  }

  @Test
  void addingTheSameProductTwiceSumsQuantities() {
    Product book = product("Book", "10.00");

    cart.addOrUpdateProduct(book, new Quantity(2));
    cart.addOrUpdateProduct(book, new Quantity(3));

    assertEquals(1, cart.getLines().size());
    assertEquals(5, cart.getLines().get(0).getQuantity().getValue());
  }

  @Test
  void existingLineKeepsItsFirstUnitPrice() {
    Product book = product("Book", "10.00");
    cart.addOrUpdateProduct(book, new Quantity(1));
    book.updateInfo("Book", "", usd("12.00"));

    cart.addOrUpdateProduct(book, new Quantity(1));

    CartLine line = cart.getLines().get(0);
    assertEquals(usd("10.00"), line.getUnitPrice());
    assertEquals(2, line.getQuantity().getValue());
  }

  @Test
  void removeProductDeletesLineAndIgnoresMissingProduct() {
    Product book = product("Book", "10.00");
    cart.addOrUpdateProduct(book, new Quantity(1));

    cart.removeProduct(UUID.randomUUID());
    assertEquals(1, cart.getLines().size());

    cart.removeProduct(book.getId());
    assertTrue(cart.getLines().isEmpty());
  }

  @Test
  void calculateTotalSumsAllLines() {
    cart.addOrUpdateProduct(product("Book", "10.00"), new Quantity(2));
    cart.addOrUpdateProduct(product("Pen", "1.50"), new Quantity(4));

    assertEquals(usd("26.00"), cart.calculateTotal());
  }

  @Test
  void totalOfEmptyCartIsZeroInDefaultCurrency() {
    assertEquals(Money.zero(Money.DEFAULT_CURRENCY), cart.calculateTotal());
  }

  @Test
  void getLinesReturnsUnmodifiableSnapshot() {
    cart.addOrUpdateProduct(product("Book", "10.00"), new Quantity(1));

    List<CartLine> snapshot = cart.getLines();
    cart.clear();

    assertEquals(1, snapshot.size());
    assertThrows(UnsupportedOperationException.class, snapshot::clear);
  }
}