package org.example.model;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Immutable hash of a password; the raw password is never stored.
 *
 * <p>SHA-256 keeps this phase dependency-free. A production system should use a salted,
 * slow algorithm such as bcrypt.
 */
public final class PasswordHash {

  private static final String ALGORITHM = "SHA-256";

  // AF: the digest of exactly one secret password.
  // RI: value is a non-blank string.
  private final String value;

  /**
   * Wraps an already computed hash.
   *
   * <p>REQUIRES: value is non-null and non-blank.
   *
   * <p>MODIFIES: nothing.
   *
   * <p>EFFECTS: Returns a hash holding the given text.
   *
   * @param value the hash string
   * @throws IllegalArgumentException if value is null or blank
   */
  public PasswordHash(String value) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("Password hash must not be blank");
    }
    this.value = value;
  }

  /**
   * Hashes a raw password.
   *
   * <p>REQUIRES: rawPassword is non-null and non-empty.
   *
   * <p>MODIFIES: nothing.
   *
   * <p>EFFECTS: Returns the hash of the password; the same password always gives an equal
   * hash.
   *
   * @param rawPassword the plain-text password
   * @return the hash of the password
   * @throws IllegalArgumentException if the password is null or empty
   */
  public static PasswordHash fromRaw(String rawPassword) {
    if (rawPassword == null || rawPassword.isEmpty()) {
      throw new IllegalArgumentException("Password must not be empty");
    }
    return new PasswordHash(hash(rawPassword));
  }

  /**
   * Checks a raw password against this hash.
   *
   * <p>MODIFIES: nothing.
   *
   * <p>EFFECTS: Returns true only if the password produces this hash; null never matches.
   *
   * @param rawPassword the plain-text password to check
   * @return true if the password is correct
   */
  public boolean matches(String rawPassword) {
    return rawPassword != null && value.equals(hash(rawPassword));
  }

  /** Returns the hash string. */
  public String getValue() {
    return value;
  }

  private static String hash(String rawPassword) {
    try {
      MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
      byte[] bytes = digest.digest(rawPassword.getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(bytes);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException(ALGORITHM + " is not available", e);
    }
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof PasswordHash that)) {
      return false;
    }
    return value.equals(that.value);
  }

  @Override
  public int hashCode() {
    return value.hashCode();
  }
}