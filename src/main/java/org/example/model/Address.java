package org.example.model;

import java.util.Objects;

/** Immutable postal address. */
public final class Address {

  // AF: the place reached by province, city, street and postalCode.
  // RI: every field is a non-blank, trimmed string.
  private final String province;
  private final String city;
  private final String street;
  private final String postalCode;

  /**
   * Creates an address.
   *
   * <p>REQUIRES: every argument is non-null and non-blank.
   *
   * <p>MODIFIES: nothing.
   *
   * <p>EFFECTS: Returns an address holding the trimmed arguments.
   *
   * @param province the province
   * @param city the city
   * @param street the street
   * @param postalCode the postal code
   * @throws IllegalArgumentException if any argument is null or blank
   */
  public Address(String province, String city, String street, String postalCode) {
    this.province = requireText(province, "Province");
    this.city = requireText(city, "City");
    this.street = requireText(street, "Street");
    this.postalCode = requireText(postalCode, "Postal code");
  }

  /** Returns the address as a single line. */
  public String getFullAddress() {
    return format(province, city, street, postalCode);
  }

  /**
   * Copies this address for storing inside an order.
   *
   * <p>MODIFIES: nothing.
   *
   * <p>EFFECTS: Returns a snapshot with the same parts; later changes elsewhere cannot affect
   * it because both types are immutable.
   *
   * @return the snapshot
   */
  public ShippingAddressSnapshot toSnapshot() {
    return new ShippingAddressSnapshot(province, city, street, postalCode);
  }

  static String format(String province, String city, String street, String postalCode) {
    return street + ", " + city + ", " + province + ", " + postalCode;
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
    if (!(other instanceof Address that)) {
      return false;
    }
    return province.equals(that.province)
        && city.equals(that.city)
        && street.equals(that.street)
        && postalCode.equals(that.postalCode);
  }

  @Override
  public int hashCode() {
    return Objects.hash(province, city, street, postalCode);
  }

  @Override
  public String toString() {
    return getFullAddress();
  }
}