package org.example.model;

import java.util.Objects;

/** Immutable copy of an address taken when an order is placed. */
public final class ShippingAddressSnapshot {

  // AF: the delivery place that an Address named when the order was placed.
  // RI: the fields were validated by Address, which is the only creator.
  private final String province;
  private final String city;
  private final String street;
  private final String postalCode;

  /**
   * Creates a snapshot; only {@link Address#toSnapshot()} calls this.
   *
   * <p>REQUIRES: every argument is validated, non-blank text.
   *
   * <p>MODIFIES: nothing.
   */
  ShippingAddressSnapshot(String province, String city, String street, String postalCode) {
    this.province = province;
    this.city = city;
    this.street = street;
    this.postalCode = postalCode;
  }

  /** Returns the address as a single line. */
  public String getFullAddress() {
    return Address.format(province, city, street, postalCode);
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof ShippingAddressSnapshot that)) {
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