package org.example.model;

import java.util.Objects;
import java.util.UUID;

/** Immutable line of a cart: one product, its unit price at add time, and a quantity. */
public final class CartLine {

  // AF: quantity items of the product productId, each costing unitPrice.
  // RI: no field is null and productName is non-blank.
  private final UUID productId;
  private final String productName;
  private final Money unitPrice;
  private final Quantity quantity;

  /**
   * Creates a cart line.
   *
   * <p>REQUIRES: every argument is non-null and productName is non-blank.
   *
   * <p>MODIFIES: nothing.
   *
   * <p>EFFECTS: Returns a line holding the arguments.
   *
   * @param productId the product identifier
   * @param productName the product name
   * @param unitPrice the price of one item
   * @param quantity the number of items
   * @throws IllegalArgumentException if productName is blank
   */
  public CartLine(UUID productId, String productName, Money unitPrice, Quantity quantity) {
    this.productId = Objects.requireNonNull(productId, "productId must not be null");
    if (productName == null || productName.isBlank()) {
      throw new IllegalArgumentException("Product name must not be blank");
    }
    this.productName = productName;
    this.unitPrice = Objects.requireNonNull(unitPrice, "unitPrice must not be null");
    this.quantity = Objects.requireNonNull(quantity, "quantity must not be null");
  }

  /**
   * Computes the cost of the whole line.
   *
   * <p>MODIFIES: nothing.
   *
   * <p>EFFECTS: Returns the unit price multiplied by the quantity.
   *
   * @return the subtotal of the line
   */
  public Money calculateSubtotal() {
    return unitPrice.multiply(quantity);
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
    if (!(other instanceof CartLine that)) {
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