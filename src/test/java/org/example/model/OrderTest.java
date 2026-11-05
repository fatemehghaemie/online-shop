package org.example.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class OrderTest {

  private static final Address ADDRESS =
      new Address("Tehran", "Tehran", "Valiasr St", "1234567890");

  private static Money usd(String amount) {
    return new Money(new BigDecimal(amount), Money.DEFAULT_CURRENCY);
  }

  private static Order newOrder() {
    OrderLine books = new OrderLine(UUID.randomUUID(), "Book", usd("10.00"), new Quantity(2));
    OrderLine pen = new OrderLine(UUID.randomUUID(), "Pen", usd("5.50"), new Quantity(1));
    return new Order(UUID.randomUUID(), List.of(books, pen), ADDRESS.toSnapshot());
  }

  @Test
  void newOrderIsPendingAndTotalIsComputedFromLines() {
    Order order = newOrder();

    assertEquals(OrderStatus.PENDING, order.getStatus());
    assertEquals(usd("25.50"), order.getTotalAmount());
  }

  @Test
  void orderMovesThroughTheWholeLifecycle() {
    Order order = newOrder();

    order.markAsPaid();
    order.updateStatus(OrderStatus.PROCESSING);
    order.updateStatus(OrderStatus.SHIPPED);
    order.updateStatus(OrderStatus.DELIVERED);

    assertEquals(OrderStatus.DELIVERED, order.getStatus());
  }

  @Test
  void stageCannotBeSkippedAndStatusStaysUnchanged() {
    Order order = newOrder();

    assertThrows(IllegalStateException.class, () -> order.updateStatus(OrderStatus.SHIPPED));
    assertEquals(OrderStatus.PENDING, order.getStatus());
  }

  @Test
  void orderCannotBePaidTwice() {
    Order order = newOrder();
    order.markAsPaid();

    assertThrows(IllegalStateException.class, order::markAsPaid);
  }

  @Test
  void pendingOrderCanBeCancelled() {
    Order order = newOrder();

    order.cancel();

    assertEquals(OrderStatus.CANCELLED, order.getStatus());
  }

  @Test
  void shippedOrderCannotBeCancelled() {
    Order order = newOrder();
    order.markAsPaid();
    order.updateStatus(OrderStatus.PROCESSING);
    order.updateStatus(OrderStatus.SHIPPED);

    assertThrows(IllegalStateException.class, order::cancel);
  }

  @Test
  void orderWithoutLinesIsRejected() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new Order(UUID.randomUUID(), List.of(), ADDRESS.toSnapshot()));
  }
}