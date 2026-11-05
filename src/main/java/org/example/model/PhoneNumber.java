package org.example.model;

import java.util.regex.Pattern;

/** Immutable, validated phone number with an optional leading plus sign. */
public final class PhoneNumber {

  private static final Pattern FORMAT = Pattern.compile("^\\+?[0-9]{10,15}$");

  // AF: the subscriber line identified by value.
  // RI: value is trimmed and matches FORMAT.
  private final String value;

  /**
   * Creates a phone number.
   *
   * <p>REQUIRES: value is non-null and, after trimming, is 10 to 15 digits with an optional
   * leading plus sign.
   *
   * <p>MODIFIES: nothing.
   *
   * <p>EFFECTS: Returns a number holding the trimmed text.
   *
   * @param value the raw number
   * @throws IllegalArgumentException if value is null or malformed
   */
  public PhoneNumber(String value) {
    if (value == null) {
      throw new IllegalArgumentException("Phone number must not be null");
    }
    String trimmed = value.trim();
    if (!FORMAT.matcher(trimmed).matches()) {
      throw new IllegalArgumentException("Invalid phone number: " + value);
    }
    this.value = trimmed;
  }

  /** Returns the normalized number. */
  public String getValue() {
    return value;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof PhoneNumber that)) {
      return false;
    }
    return value.equals(that.value);
  }

  @Override
  public int hashCode() {
    return value.hashCode();
  }

  @Override
  public String toString() {
    return value;
  }
}