package org.example.model;

import java.util.Objects;
import java.util.UUID;

/** Attempt to pay for an order; it ends either successful or failed. */
public final class Payment {

  // AF: one payment attempt of amount for orderId, in the state given by status.
  // RI: referenceId is set only when SUCCESSFUL and failureReason only when FAILED; a payment
  //     leaves PENDING at most once.
  private final UUID id;
  private final UUID orderId;
  private final Money amount;
  private PaymentStatus status;
  private String referenceId;
  private String failureReason;

  /**
   * Creates a pending payment with a new identifier.
   *
   * @param orderId the order being paid; must not be null
   * @param amount the amount to charge; must not be null
   */
  public Payment(UUID orderId, Money amount) {
    this.id = UUID.randomUUID();
    this.orderId = Objects.requireNonNull(orderId, "orderId must not be null");
    this.amount = Objects.requireNonNull(amount, "amount must not be null");
    this.status = PaymentStatus.PENDING;
  }

  /**
   * Marks the payment as successful.
   *
   * <p>REQUIRES: the status is PENDING and refId is non-blank.
   *
   * <p>MODIFIES: this payment.
   *
   * <p>EFFECTS: The status becomes SUCCESSFUL and the reference is stored. Otherwise an
   * exception is thrown and the payment is unchanged.
   *
   * @param refId the reference returned by the payment provider
   * @throws IllegalArgumentException if refId is blank
   * @throws IllegalStateException if the payment is not pending
   */
  public void markSuccessful(String refId) {
    requirePending();
    this.referenceId = requireText(refId, "Reference id");
    this.status = PaymentStatus.SUCCESSFUL;
  }

  /**
   * Marks the payment as failed.
   *
   * <p>REQUIRES: the status is PENDING and reason is non-blank.
   *
   * <p>MODIFIES: this payment.
   *
   * <p>EFFECTS: The status becomes FAILED and the reason is stored. Otherwise an exception is
   * thrown and the payment is unchanged.
   *
   * @param reason why the payment failed
   * @throws IllegalArgumentException if reason is blank
   * @throws IllegalStateException if the payment is not pending
   */
  public void markFailed(String reason) {
    requirePending();
    this.failureReason = requireText(reason, "Failure reason");
    this.status = PaymentStatus.FAILED;
  }

  /** Returns the unique identifier. */
  public UUID getId() {
    return id;
  }

  /** Returns the identifier of the paid order. */
  public UUID getOrderId() {
    return orderId;
  }

  /** Returns the amount charged. */
  public Money getAmount() {
    return amount;
  }

  /** Returns the current status. */
  public PaymentStatus getStatus() {
    return status;
  }

  /** Returns the provider reference, or null unless the payment is SUCCESSFUL. */
  public String getReferenceId() {
    return referenceId;
  }

  /** Returns the failure reason, or null unless the payment is FAILED. */
  public String getFailureReason() {
    return failureReason;
  }

  private void requirePending() {
    if (status != PaymentStatus.PENDING) {
      throw new IllegalStateException("Payment is already " + status);
    }
  }

  private static String requireText(String text, String fieldName) {
    if (text == null || text.isBlank()) {
      throw new IllegalArgumentException(fieldName + " must not be blank");
    }
    return text.trim();
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof Payment that)) {
      return false;
    }
    return id.equals(that.id);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }
}