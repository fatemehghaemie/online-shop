package org.example.model;

import java.util.Objects;

/** Immutable non-negative count of items in stock. */
public final class StockQuantity {

  // AF: value items on the shelf.
  // RI: value >= 0.
  private final int value;

  /**
   * Creates a stock quantity.
   *
   * <p>REQUIRES: value is not negative.
   *
   * <p>MODIFIES: nothing.
   *
   * <p>EFFECTS: Returns a stock quantity holding value.
   *
   * @param value the count
   * @throws IllegalArgumentException if value is negative
   */
  public StockQuantity(int value) {
    if (value < 0) {
      throw new IllegalArgumentException("Stock must not be negative: " + value);
    }
    this.value = value;
  }

  /**
   * Checks whether the stock covers a requested quantity.
   *
   * <p>REQUIRES: requested is non-null.
   *
   * <p>MODIFIES: nothing.
   *
   * <p>EFFECTS: Returns true if this stock is at least the requested quantity.
   *
   * @param requested the quantity wanted
   * @return true if enough items are in stock
   */
  public boolean isAvailable(Quantity requested) {
    Objects.requireNonNull(requested, "requested must not be null");
    return value >= requested.getValue();
  }

  /** Returns the count. */
  public int getValue() {
    return value;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof StockQuantity that)) {
      return false;
    }
    return value == that.value;
  }

  @Override
  public int hashCode() {
    return Integer.hashCode(value);
  }

  @Override
  public String toString() {
    return Integer.toString(value);
  }
}