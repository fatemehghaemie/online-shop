package org.example.service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.example.model.Money;
import org.example.model.Order;
import org.example.model.OrderStatus;
import org.example.model.Payment;
import org.example.model.PaymentStatus;

/**
 * Use cases for paying orders and checking payments.
 *
 * <p>No external provider is called; a payment succeeds when the amount equals the order total,
 * and a failed gateway result is reported through {@link #recordFailedPayment}.
 */
public final class PaymentService {

  private static final String REFERENCE_PREFIX = "PAY-";
  private static final String AMOUNT_MISMATCH_REASON = "Amount does not match the order total";

  // AF: the recorded payments are the values of paymentsById, oldest first.
  // RI: no value is null and every key equals the id of its payment.
  private final Map<UUID, Payment> paymentsById = new LinkedHashMap<>();
  private final OrderRepository orderRepository;

  /**
   * Creates the service.
   *
   * @param orderRepository where orders are stored; must not be null
   */
  public PaymentService(OrderRepository orderRepository) {
    this.orderRepository =
        Objects.requireNonNull(orderRepository, "orderRepository must not be null");
  }

  /**
   * Pays a pending order.
   *
   * <p>REQUIRES: orderId belongs to an existing order with status PENDING and amount is
   * non-null.
   *
   * <p>MODIFIES: the recorded payments and, on success, the order and the order repository.
   *
   * <p>EFFECTS: If amount equals the order total, records and returns a SUCCESSFUL payment and
   * the order becomes PAID. Otherwise records and returns a FAILED payment and the order stays
   * PENDING. If a requirement is not met, an exception is thrown and nothing is recorded.
   *
   * @param orderId the order to pay; must not be null
   * @param amount the amount offered
   * @return the recorded payment
   * @throws IllegalArgumentException if the order does not exist
   * @throws IllegalStateException if the order is not pending
   */
  public Payment processPayment(UUID orderId, Money amount) {
    Objects.requireNonNull(amount, "amount must not be null");
    Order order = orderRepository.getById(orderId);
    requirePending(order);
    Payment payment = new Payment(orderId, amount);
    if (amount.equals(order.getTotalAmount())) {
      payment.markSuccessful(REFERENCE_PREFIX + UUID.randomUUID());
      order.markAsPaid();
      orderRepository.save(order);
    } else {
      payment.markFailed(AMOUNT_MISMATCH_REASON);
    }
    return record(payment);
  }

  /**
   * Records that the payment gateway reported an unsuccessful result for an order.
   *
   * <p>REQUIRES: orderId belongs to an existing order with status PENDING, amount is non-null
   * and reason is non-blank.
   *
   * <p>MODIFIES: the recorded payments.
   *
   * <p>EFFECTS: Records and returns a FAILED payment holding the reason. The order stays
   * PENDING, so it can be paid again. If a requirement is not met, an exception is thrown and
   * nothing is recorded.
   *
   * @param orderId the order whose payment failed; must not be null
   * @param amount the amount that was attempted
   * @param reason why the payment failed
   * @return the recorded payment
   * @throws IllegalArgumentException if the order does not exist or reason is blank
   * @throws IllegalStateException if the order is not pending
   */
  public Payment recordFailedPayment(UUID orderId, Money amount, String reason) {
    Objects.requireNonNull(amount, "amount must not be null");
    Order order = orderRepository.getById(orderId);
    requirePending(order);
    Payment payment = new Payment(orderId, amount);
    payment.markFailed(reason);
    return record(payment);
  }

  /**
   * Checks that a payment succeeded for an order.
   *
   * <p>REQUIRES: orderId and paymentId are non-null.
   *
   * <p>MODIFIES: nothing.
   *
   * <p>EFFECTS: Returns true only if the payment exists, belongs to the order and is
   * SUCCESSFUL.
   *
   * @param orderId the order that was paid
   * @param paymentId the payment to check
   * @return true if the payment is a successful payment of the order
   */
  public boolean verifyPayment(UUID orderId, UUID paymentId) {
    Objects.requireNonNull(orderId, "orderId must not be null");
    Objects.requireNonNull(paymentId, "paymentId must not be null");
    Payment payment = paymentsById.get(paymentId);
    return payment != null
        && payment.getOrderId().equals(orderId)
        && payment.getStatus() == PaymentStatus.SUCCESSFUL;
  }

  private Payment record(Payment payment) {
    paymentsById.put(payment.getId(), payment);
    checkRep();
    return payment;
  }

  private static void requirePending(Order order) {
    if (order.getStatus() != OrderStatus.PENDING) {
      throw new IllegalStateException("Order is not awaiting payment: " + order.getStatus());
    }
  }

  private void checkRep() {
    for (Map.Entry<UUID, Payment> entry : paymentsById.entrySet()) {
      assert entry.getValue() != null : "null payment";
      assert entry.getValue().getId().equals(entry.getKey()) : "key does not match payment id";
    }
  }
}