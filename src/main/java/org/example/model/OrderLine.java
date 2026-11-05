package org.example.model;

import java.util.Objects;
import java.util.UUID;

/** Immutable line of a placed order, with the price frozen at order time. */
public final class OrderLine {

  // AF: quantity items of the product productId bought at unitPrice, costing subtotal in total.
  // RI: no field is null, productName is non-blank and subtotal equals unitPrice * quantity.
  private final UUID productId;
  private final String productName;
  private final Money unitPrice;
  private final Quantity quantity;
  private final Money subtotal;

  /**
   * Creates an order line; the subtotal is computed, never supplied.
   *
   * <p>REQUIRES: every argument is non-null and productName is non-blank.
   *
   * <p>MODIFIES: nothing.
   *
   * <p>EFFECTS: Returns a line holding the arguments and their subtotal.
   *
   * @param productId the product identifier
   * @param productName the product name
   * @param unitPrice the price of one item
   * @param quantity the number of items
   * @throws IllegalArgumentException if productName is blank
   */
  public OrderLine(UUID productId, String productName, Money unitPrice, Quantity quantity) {
    this.productId = Objects.requireNonNull(productId, "productId must not be null");
    if (productName == null || productName.isBlank()) {
      throw new IllegalArgumentException("Product name must not be blank");
    }
    this.productName = productName;
    this.unitPrice = Objects.requireNonNull(unitPrice, "unitPrice must not be null");
    this.quantity = Objects.requireNonNull(quantity, "quantity must not be null");
    this.subtotal = unitPrice.multiply(quantity);
  }

  /**
   * Freezes a cart line into an order line.
   *
   * <p>REQUIRES: cartLine is non-null.
   *
   * <p>MODIFIES: nothing.
   *
   * <p>EFFECTS: Returns a line with the same product, price and quantity.
   *
   * @param cartLine the line to copy
   * @return the order line
   */
  public static OrderLine fromCartLine(CartLine cartLine) {
    Objects.requireNonNull(cartLine, "cartLine must not be null");
    return new OrderLine(
        cartLine.getProductId(),
        cartLine.getProductName(),
        cartLine.getUnitPrice(),
        cartLine.getQuantity());
  }

  /** Returns the unit price multiplied by the quantity. */
  public Money getSubtotal() {
    return subtotal;
  }

  /** Returns the product identifier. */
  public UUID getProductId() {
    return productId;
  }

  /** Returns the product name. */
  public String getProductName() {
    return productName;
  }

  /** Returns the price of one item. */
  public Money getUnitPrice() {
    return unitPrice;
  }

  /** Returns the number of items. */
  public Quantity getQuantity() {
    return quantity;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof OrderLine that)) {
      return false;
    }
    return productId.equals(that.productId)
        && productName.equals(that.productName)
        && unitPrice.equals(that.unitPrice)
        && quantity.equals(that.quantity);
  }

  @Override
  public int hashCode() {
    return Objects.hash(productId, productName, unitPrice, quantity);
  }
}