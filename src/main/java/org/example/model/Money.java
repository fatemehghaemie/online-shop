package org.example.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;
import java.util.Objects;

/** Immutable non-negative amount of money in a single currency. */
public final class Money {

  /** Currency used by the whole shop, e.g. for the total of an empty cart. */
  public static final String DEFAULT_CURRENCY = "USD";

  private static final int SCALE = 2;

  // AF: amount units of the currency named by currency.
  // RI: amount >= 0 with scale SCALE; currency is a non-blank upper-case code.
  private final BigDecimal amount;
  private final String currency;

  /**
   * Creates a money value, rounded half-up to two decimal places.
   *
   * <p>REQUIRES: amount is non-null and not negative; currency is non-null and non-blank.
   *
   * <p>MODIFIES: nothing.
   *
   * <p>EFFECTS: Returns the rounded amount in the upper-case currency.
   *
   * @param amount the amount
   * @param currency the currency code
   * @throws IllegalArgumentException if amount or currency is invalid
   */
  public Money(BigDecimal amount, String currency) {
    if (amount == null || amount.signum() < 0) {
      throw new IllegalArgumentException("Amount must be non-null and non-negative");
    }
    if (currency == null || currency.isBlank()) {
      throw new IllegalArgumentException("Currency must not be blank");
    }
    this.amount = amount.setScale(SCALE, RoundingMode.HALF_UP);
    this.currency = currency.trim().toUpperCase(Locale.ROOT);
  }

  /**
   * Creates a zero amount.
   *
   * <p>REQUIRES: currency is non-null and non-blank.
   *
   * <p>MODIFIES: nothing.
   *
   * @param currency the currency code
   * @return zero in the given currency
   * @throws IllegalArgumentException if currency is null or blank
   */
  public static Money zero(String currency) {
    return new Money(BigDecimal.ZERO, currency);
  }

  /**
   * Adds another amount.
   *
   * <p>REQUIRES: other is non-null and uses the same currency.
   *
   * <p>MODIFIES: nothing.
   *
   * <p>EFFECTS: Returns a new value holding the sum; this value and other are unchanged.
   *
   * @param other the amount to add
   * @return the sum
   * @throws IllegalArgumentException if the currencies differ
   */
  public Money add(Money other) {
    Objects.requireNonNull(other, "other must not be null");
    if (!currency.equals(other.currency)) {
      throw new IllegalArgumentException(
          "Currency mismatch: " + currency + " vs " + other.currency);
    }
    return new Money(amount.add(other.amount), currency);
  }

  /**
   * Multiplies this amount by a quantity.
   *
   * <p>REQUIRES: quantity is non-null.
   *
   * <p>MODIFIES: nothing.
   *
   * <p>EFFECTS: Returns a new value holding the product; this value is unchanged.
   *
   * @param quantity the factor
   * @return the product
   */
  public Money multiply(Quantity quantity) {
    Objects.requireNonNull(quantity, "quantity must not be null");
    return new Money(amount.multiply(BigDecimal.valueOf(quantity.getValue())), currency);
  }

  /** Returns the amount. */
  public BigDecimal getAmount() {
    return amount;
  }

  /** Returns the upper-case currency code. */
  public String getCurrency() {
    return currency;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof Money that)) {
      return false;
    }
    return amount.equals(that.amount) && currency.equals(that.currency);
  }

  @Override
  public int hashCode() {
    return Objects.hash(amount, currency);
  }

  @Override
  public String toString() {
    return amount.toPlainString() + " " + currency;
  }
}