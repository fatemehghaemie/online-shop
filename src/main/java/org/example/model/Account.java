package org.example.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/** Registered user of the shop, either a customer or an administrator. */
public final class Account {

  private static final int NOT_FOUND = -1;

  // AF: one person identified by id, with a profile and an ordered address book.
  // RI: no field is null; fullName is non-blank; addresses has no null or duplicate entry.
  private final UUID id;
  private final Role role;
  private final Email email;
  private PasswordHash passwordHash;
  private PhoneNumber phone;
  private String fullName;
  private final List<Address> addresses = new ArrayList<>();

  /**
   * Creates an account with a new identifier and an empty address book.
   *
   * @param role the access level; must not be null
   * @param email the login email; must not be null
   * @param phone the contact phone; must not be null
   * @param passwordHash the hashed password; must not be null
   * @param fullName the display name; must not be null or blank
   * @throws IllegalArgumentException if the name is blank
   */
  public Account(
      Role role, Email email, PhoneNumber phone, PasswordHash passwordHash, String fullName) {
    this.id = UUID.randomUUID();
    this.role = Objects.requireNonNull(role, "role must not be null");
    this.email = Objects.requireNonNull(email, "email must not be null");
    this.phone = Objects.requireNonNull(phone, "phone must not be null");
    this.passwordHash = Objects.requireNonNull(passwordHash, "passwordHash must not be null");
    this.fullName = requireText(fullName);
  }

  /**
   * Adds an address to the address book.
   *
   * <p>REQUIRES: addr is non-null.
   *
   * <p>MODIFIES: this account.
   *
   * <p>EFFECTS: After the call the address book contains the address exactly once; adding an
   * address that is already present leaves the account unchanged.
   *
   * @param addr the address to add
   */
  public void addAddress(Address addr) {
    Objects.requireNonNull(addr, "addr must not be null");
    if (!addresses.contains(addr)) {
      addresses.add(addr);
    }
  }

  /**
   * Replaces the address at a position of the address book.
   *
   * <p>REQUIRES: index is a valid position, counted from 0; addr is non-null and is not
   * already stored at another position.
   *
   * <p>MODIFIES: this account.
   *
   * <p>EFFECTS: The address at index becomes the given address. If a requirement is not met,
   * an exception is thrown and the address book is unchanged.
   *
   * @param index the position to replace, counted from 0
   * @param addr the new address
   * @throws IllegalArgumentException if the position is invalid or the address already exists
   */
  public void updateAddress(int index, Address addr) {
    requireValidIndex(index);
    Objects.requireNonNull(addr, "addr must not be null");
    int existingIndex = addresses.indexOf(addr);
    if (existingIndex != NOT_FOUND && existingIndex != index) {
      throw new IllegalArgumentException("Address already exists: " + addr);
    }
    addresses.set(index, addr);
  }

  /**
   * Deletes the address at a position of the address book.
   *
   * <p>REQUIRES: index is a valid position, counted from 0.
   *
   * <p>MODIFIES: this account.
   *
   * <p>EFFECTS: The address is removed and later addresses move one position forward. If the
   * position is invalid, an exception is thrown and the address book is unchanged.
   *
   * @param index the position to delete, counted from 0
   * @throws IllegalArgumentException if the position is invalid
   */
  public void removeAddress(int index) {
    requireValidIndex(index);
    addresses.remove(index);
  }

  /**
   * Checks a raw password against the stored hash.
   *
   * <p>EFFECTS: Returns true only if the password matches; does not modify the account.
   *
   * @param rawPassword the plain-text password; null never matches
   * @return true if the password is correct
   */
  public boolean verifyPassword(String rawPassword) {
    return passwordHash.matches(rawPassword);
  }

  /**
   * Replaces the stored password hash.
   *
   * <p>REQUIRES: newPasswordHash is non-null.
   *
   * <p>MODIFIES: this account.
   *
   * <p>EFFECTS: After the call only the password behind the new hash is accepted by
   * verifyPassword.
   *
   * @param newPasswordHash the hash of the new password
   */
  public void changePassword(PasswordHash newPasswordHash) {
    this.passwordHash =
        Objects.requireNonNull(newPasswordHash, "newPasswordHash must not be null");
  }

  /** Returns true if this account has the administrator role. */
  public boolean isAdmin() {
    return role == Role.ADMIN;
  }

  /**
   * Changes the display name and phone number together.
   *
   * <p>REQUIRES: fullName is non-blank and phone is non-null.
   *
   * <p>MODIFIES: this account.
   *
   * <p>EFFECTS: On success both values are replaced. If an argument is invalid, an exception is
   * thrown and the account is unchanged.
   *
   * @param fullName the new display name
   * @param phone the new contact phone
   * @throws IllegalArgumentException if the name is blank
   */
  public void updateProfile(String fullName, PhoneNumber phone) {
    String validName = requireText(fullName);
    PhoneNumber validPhone = Objects.requireNonNull(phone, "phone must not be null");
    this.fullName = validName;
    this.phone = validPhone;
  }

  /** Returns the unique identifier. */
  public UUID getId() {
    return id;
  }

  /** Returns the access level. */
  public Role getRole() {
    return role;
  }

  /** Returns the login email. */
  public Email getEmail() {
    return email;
  }

  /** Returns the contact phone. */
  public PhoneNumber getPhone() {
    return phone;
  }

  /** Returns the display name. */
  public String getFullName() {
    return fullName;
  }

  /**
   * Returns the addresses in the order they were added.
   *
   * <p>EFFECTS: The returned list is unmodifiable and does not reflect later changes to this
   * account.
   */
  public List<Address> getAddresses() {
    return List.copyOf(addresses);
  }

  private void requireValidIndex(int index) {
    if (index < 0 || index >= addresses.size()) {
      throw new IllegalArgumentException("No address at position " + index);
    }
  }

  private static String requireText(String text) {
    if (text == null || text.isBlank()) {
      throw new IllegalArgumentException("Full name must not be blank");
    }
    return text.trim();
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof Account that)) {
      return false;
    }
    return id.equals(that.id);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }
}