package org.example.model;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/** Placed order: frozen lines and address, plus a status that moves through its lifecycle. */
public final class Order {

  // AF: the purchase of lines by ownerId, shipped to shippingAddress, at a state given by status.
  // RI: lines is non-empty and immutable; totalAmount equals the sum of the line subtotals;
  //     status changes only through cancel, markAsPaid and updateStatus.
  private final UUID id;
  private final UUID ownerId;
  private final List<OrderLine> lines;
  private final ShippingAddressSnapshot shippingAddress;
  private final Money totalAmount;
  private OrderStatus status;

  /**
   * Creates a pending order; the total is computed from the lines.
   *
   * @param ownerId the account that places the order; must not be null
   * @param lines the ordered lines; must not be null or empty; copied on entry
   * @param shippingAddress the delivery address; must not be null
   * @throws IllegalArgumentException if there are no lines
   */
  public Order(UUID ownerId, List<OrderLine> lines, ShippingAddressSnapshot shippingAddress) {
    Objects.requireNonNull(lines, "lines must not be null");
    if (lines.isEmpty()) {
      throw new IllegalArgumentException("An order needs at least one line");
    }
    this.id = UUID.randomUUID();
    this.ownerId = Objects.requireNonNull(ownerId, "ownerId must not be null");
    this.lines = List.copyOf(lines);
    this.shippingAddress =
        Objects.requireNonNull(shippingAddress, "shippingAddress must not be null");
    this.totalAmount = sumSubtotals(this.lines);
    this.status = OrderStatus.PENDING;
  }

  /**
   * Cancels the order.
   *
   * <p>REQUIRES: the status is PENDING, PAID or PROCESSING.
   *
   * <p>MODIFIES: this order.
   *
   * <p>EFFECTS: The status becomes CANCELLED. In any other status an exception is thrown and
   * the order is unchanged.
   *
   * @throws IllegalStateException if the order was already shipped or cancelled
   */
  public void cancel() {
    if (!isCancellable()) {
      throw new IllegalStateException("Order cannot be cancelled in status " + status);
    }
    status = OrderStatus.CANCELLED;
  }

  /**
   * Records that the order was paid.
   *
   * <p>REQUIRES: the status is PENDING.
   *
   * <p>MODIFIES: this order.
   *
   * <p>EFFECTS: The status becomes PAID. In any other status an exception is thrown and the
   * order is unchanged.
   *
   * @throws IllegalStateException if the order is not pending
   */
  public void markAsPaid() {
    if (status != OrderStatus.PENDING) {
      throw new IllegalStateException("Only a pending order can be paid, status: " + status);
    }
    status = OrderStatus.PAID;
  }

  /**
   * Moves the order to the next fulfilment stage.
   *
   * <p>REQUIRES: status is the stage right after the current one in the sequence PAID,
   * PROCESSING, SHIPPED, DELIVERED. Payment and cancellation use their own methods.
   *
   * <p>MODIFIES: this order.
   *
   * <p>EFFECTS: The status becomes the given status. For any other transition an exception is
   * thrown and the order is unchanged.
   *
   * @param status the stage to move to; must not be null
   * @throws IllegalStateException if the transition is not allowed
   */
  public void updateStatus(OrderStatus status) {
    Objects.requireNonNull(status, "status must not be null");
    if (!isNextStage(this.status, status)) {
      throw new IllegalStateException(
          "Cannot move order from " + this.status + " to " + status);
    }
    this.status = status;
  }

  /** Returns the unique identifier. */
  public UUID getId() {
    return id;
  }

  /** Returns the identifier of the account that placed the order. */
  public UUID getOwnerId() {
    return ownerId;
  }

  /** Returns the total price, computed when the order was created. */
  public Money getTotalAmount() {
    return totalAmount;
  }

  /** Returns the current status. */
  public OrderStatus getStatus() {
    return status;
  }

  /** Returns the frozen delivery address. */
  public ShippingAddressSnapshot getShippingAddress() {
    return shippingAddress;
  }

  /** Returns the order lines as an unmodifiable list. */
  public List<OrderLine> getLines() {
    return lines;
  }

  private boolean isCancellable() {
    return status == OrderStatus.PENDING
        || status == OrderStatus.PAID
        || status == OrderStatus.PROCESSING;
  }

  private static boolean isNextStage(OrderStatus current, OrderStatus next) {
    return switch (current) {
      case PAID -> next == OrderStatus.PROCESSING;
      case PROCESSING -> next == OrderStatus.SHIPPED;
      case SHIPPED -> next == OrderStatus.DELIVERED;
      default -> false;
    };
  }

  private static Money sumSubtotals(List<OrderLine> orderLines) {
    return orderLines.stream()
        .map(OrderLine::getSubtotal)
        .reduce(Money.zero(Money.DEFAULT_CURRENCY), Money::add);
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof Order that)) {
      return false;
    }
    return id.equals(that.id);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }
}