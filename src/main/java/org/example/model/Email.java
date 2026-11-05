package org.example.model;

import java.util.Locale;
import java.util.regex.Pattern;

/** Immutable, validated email address, stored in lower case. */
public final class Email {

  private static final Pattern FORMAT = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

  // AF: the mailbox named by value.
  // RI: value is trimmed, lower case and matches FORMAT.
  private final String value;

  /**
   * Creates an email address.
   *
   * <p>REQUIRES: value is non-null and, after trimming, has the form name@domain.tld.
   *
   * <p>MODIFIES: nothing.
   *
   * <p>EFFECTS: Returns an address holding the trimmed, lower-case text.
   *
   * @param value the raw address
   * @throws IllegalArgumentException if value is null or malformed
   */
  public Email(String value) {
    if (value == null) {
      throw new IllegalArgumentException("Email must not be null");
    }
    String normalized = value.trim().toLowerCase(Locale.ROOT);
    if (!FORMAT.matcher(normalized).matches()) {
      throw new IllegalArgumentException("Invalid email: " + value);
    }
    this.value = normalized;
  }

  /** Returns the normalized address. */
  public String getValue() {
    return value;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof Email that)) {
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