package org.example.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.example.model.Account;
import org.example.model.Address;
import org.example.model.Order;
import org.example.model.OrderStatus;
import org.example.model.Payment;
import org.example.model.PaymentStatus;
import org.example.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CheckoutFlowTest {

  private static final int INITIAL_STOCK = 10;

  private ShopFixture shop;
  private Account customer;
  private Product product;

  @BeforeEach
  void setUp() {
    shop = new ShopFixture();
    customer = shop.registerCustomer("buyer@example.com", "09121234567");
    product =
        shop.createProduct("Clean Code", "35.00", INITIAL_STOCK, shop.createCategory("Books"));
  }

  @Test
  void checkoutCreatesPendingOrderReducesStockAndEmptiesCart() {
    shop.cartService.addProduct(customer.getId(), product.getId(), 2);

    Order order = shop.orderService.checkout(customer.getId(), ShopFixture.ADDRESS);

    assertEquals(OrderStatus.PENDING, order.getStatus());
    assertEquals(ShopFixture.usd("70.00"), order.getTotalAmount());
    assertEquals(INITIAL_STOCK - 2, product.getStock().getValue());
    assertTrue(shop.cartService.getCart(customer.getId()).getLines().isEmpty());
    assertEquals(1, shop.orderService.getOrderHistory(customer.getId()).size());
  }

  @Test
  void checkoutOfEmptyCartFails() {
    assertThrows(
        IllegalStateException.class,
        () -> shop.orderService.checkout(customer.getId(), ShopFixture.ADDRESS));
  }

  @Test
  void checkoutRejectsAnAddressThatIsNotSavedInTheAccount() {
    shop.cartService.addProduct(customer.getId(), product.getId(), 1);
    Address foreign = new Address("Fars", "Shiraz", "Zand St", "7134567890");

    assertThrows(
        IllegalArgumentException.class,
        () -> shop.orderService.checkout(customer.getId(), foreign));
    assertEquals(INITIAL_STOCK, product.getStock().getValue());
  }

  @Test
  void cannotAddMoreThanTheStockEvenInTwoSteps() {
    shop.cartService.addProduct(customer.getId(), product.getId(), 6);

    assertThrows(
        IllegalArgumentException.class,
        () -> shop.cartService.addProduct(customer.getId(), product.getId(), 5));
    assertEquals(
        6, shop.cartService.getCart(customer.getId()).getLines().get(0).getQuantity().getValue());
  }

  @Test
  void paymentWithTheExactAmountMarksOrderPaid() {
    Order order = shop.placeOrder(customer, product, 1);

    Payment payment = shop.paymentService.processPayment(order.getId(), order.getTotalAmount());

    assertEquals(PaymentStatus.SUCCESSFUL, payment.getStatus());
    assertEquals(OrderStatus.PAID, order.getStatus());
    assertTrue(shop.paymentService.verifyPayment(order.getId(), payment.getId()));
  }

  @Test
  void paymentWithAWrongAmountFailsAndOrderStaysPending() {
    Order order = shop.placeOrder(customer, product, 1);

    Payment payment = shop.paymentService.processPayment(order.getId(), ShopFixture.usd("1.00"));

    assertEquals(PaymentStatus.FAILED, payment.getStatus());
    assertEquals(OrderStatus.PENDING, order.getStatus());
    assertFalse(shop.paymentService.verifyPayment(order.getId(), payment.getId()));
  }

  @Test
  void failedGatewayResultIsRecordedAndTheOrderCanStillBePaid() {
    Order order = shop.placeOrder(customer, product, 1);

    Payment failed =
        shop.paymentService.recordFailedPayment(
            order.getId(), order.getTotalAmount(), "Declined by the gateway");

    assertEquals(PaymentStatus.FAILED, failed.getStatus());
    assertEquals("Declined by the gateway", failed.getFailureReason());
    assertEquals(OrderStatus.PENDING, order.getStatus());
    Payment retry = shop.paymentService.processPayment(order.getId(), order.getTotalAmount());
    assertEquals(PaymentStatus.SUCCESSFUL, retry.getStatus());
  }

  @Test
  void aPaidOrderCannotBePaidAgain() {
    Order order = shop.placePaidOrder(customer, product, 1);

    assertThrows(
        IllegalStateException.class,
        () -> shop.paymentService.processPayment(order.getId(), order.getTotalAmount()));
  }

  @Test
  void adminCanListAllOrdersAndFilterThemByStatus() {
    shop.placePaidOrder(customer, product, 1);
    shop.placeOrder(customer, product, 1);
    java.util.UUID adminId = shop.admin.getId();

    assertEquals(2, shop.orderService.getAllOrders(adminId).size());
    assertEquals(1, shop.orderService.getOrdersByStatus(adminId, OrderStatus.PAID).size());
    assertEquals(1, shop.orderService.getOrdersByStatus(adminId, OrderStatus.PENDING).size());
    assertEquals(0, shop.orderService.getOrdersByStatus(adminId, OrderStatus.SHIPPED).size());
  }

  @Test
  void customerCannotListTheOrdersOfAllCustomers() {
    assertThrows(
        SecurityException.class, () -> shop.orderService.getAllOrders(customer.getId()));
    assertThrows(
        SecurityException.class,
        () -> shop.orderService.getOrdersByStatus(customer.getId(), OrderStatus.PAID));
  }
}