package org.example.model;

/** Immutable positive count of items. */
public final class Quantity {

  // AF: value items.
  // RI: value > 0.
  private final int value;

  /**
   * Creates a quantity.
   *
   * <p>REQUIRES: value is positive.
   *
   * <p>MODIFIES: nothing.
   *
   * <p>EFFECTS: Returns a quantity holding value.
   *
   * @param value the count
   * @throws IllegalArgumentException if value is zero or negative
   */
  public Quantity(int value) {
    if (value <= 0) {
      throw new IllegalArgumentException("Quantity must be positive: " + value);
    }
    this.value = value;
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
    if (!(other instanceof Quantity that)) {
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