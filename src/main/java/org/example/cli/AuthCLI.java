package org.example.cli;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.example.model.Account;
import org.example.model.Address;
import org.example.service.AuthService;
import org.example.service.UserService;

/** Console screens for registering, logging in, recovering a password and managing a profile. */
public final class AuthCLI {

  private static final int PROFILE_BACK = 0;
  private static final int PROFILE_CHANGE = 1;
  private static final int PROFILE_ADD_ADDRESS = 2;
  private static final int PROFILE_EDIT_ADDRESS = 3;
  private static final int PROFILE_DELETE_ADDRESS = 4;
  private static final String NOT_LOGGED_IN_MESSAGE = "You are not logged in.";

  private final AuthService authService;
  private final UserService userService;
  private final ConsoleReader reader;

  // AF: currentAccount is the logged-in account, or null when nobody is logged in.
  // RI: if non-null, it is an account stored in the account repository.
  private Account currentAccount;

  /**
   * Creates the screens.
   *
   * @param authService registers and logs in accounts; must not be null
   * @param userService reads and changes profiles; must not be null
   * @param reader reads the user's input; must not be null
   */
  public AuthCLI(AuthService authService, UserService userService, ConsoleReader reader) {
    this.authService = Objects.requireNonNull(authService, "authService must not be null");
    this.userService = Objects.requireNonNull(userService, "userService must not be null");
    this.reader = Objects.requireNonNull(reader, "reader must not be null");
  }

  /**
   * Asks for registration data and registers a customer account.
   *
   * <p>MODIFIES: the console and, on success, the account repository.
   *
   * <p>EFFECTS: Prints the result. Invalid data is reported and nothing is stored.
   */
  public void readRegistrationData() {
    String email = reader.readLine("Email");
    String phone = reader.readLine("Phone number");
    String password = reader.readLine("Password (at least 8 characters)");
    String name = reader.readLine("Full name");
    try {
      Account account = authService.register(email, phone, password, name);
      System.out.println("Registered. You can now log in with " + account.getEmail() + ".");
    } catch (IllegalArgumentException e) {
      System.out.println("Registration failed: " + e.getMessage());
    }
  }

  /**
   * Asks for credentials and logs in.
   *
   * <p>MODIFIES: the console and, on success, the current account.
   *
   * <p>EFFECTS: On success the account becomes the current account. Otherwise the failure is
   * printed and the current account is unchanged.
   */
  public void readLoginData() {
    String email = reader.readLine("Email");
    String password = reader.readLine("Password");
    try {
      currentAccount = authService.login(email, password);
      System.out.println("Welcome, " + currentAccount.getFullName() + ".");
    } catch (IllegalArgumentException e) {
      System.out.println("Login failed: " + e.getMessage());
    }
  }

  /**
   * Asks for a registered email or phone number and a new password, then changes the password.
   *
   * <p>MODIFIES: the console and, on success, the stored account.
   *
   * <p>EFFECTS: Prints the result. If the email or phone number is not registered or the
   * password is too short, the reason is printed and nothing is changed. No verification code
   * is sent in this phase.
   */
  public void readPasswordResetData() {
    String emailOrPhone = reader.readLine("Registered email or phone number");
    String newPassword = reader.readLine("New password (at least 8 characters)");
    try {
      authService.resetPassword(emailOrPhone, newPassword);
      System.out.println("Password changed. You can now log in with the new password.");
    } catch (IllegalArgumentException e) {
      System.out.println("Password reset failed: " + e.getMessage());
    }
  }

  /**
   * Returns the logged-in account.
   *
   * <p>EFFECTS: Returns empty if nobody is logged in; does not modify anything.
   *
   * @return the current account, if any
   */
  public Optional<Account> getCurrentAccount() {
    return Optional.ofNullable(currentAccount);
  }

  /**
   * Logs the current account out.
   *
   * <p>MODIFIES: the console and the current account.
   *
   * <p>EFFECTS: After the call nobody is logged in.
   */
  public void logout() {
    if (currentAccount == null) {
      System.out.println(NOT_LOGGED_IN_MESSAGE);
      return;
    }
    authService.logout(currentAccount.getId());
    currentAccount = null;
    System.out.println("Logged out.");
  }

  /**
   * Shows the profile and lets the user change it and manage addresses until they go back.
   *
   * <p>MODIFIES: the console, the current account and the account repository.
   */
  public void openProfileMenu() {
    if (currentAccount == null) {
      System.out.println(NOT_LOGGED_IN_MESSAGE);
      return;
    }
    boolean inMenu = true;
    while (inMenu) {
      showProfile();
      printProfileMenu();
      inMenu = handleProfileChoice(reader.readInt("Choice"));
    }
  }

  private boolean handleProfileChoice(int choice) {
    if (choice == PROFILE_BACK) {
      return false;
    }
    switch (choice) {
      case PROFILE_CHANGE -> readProfileChanges();
      case PROFILE_ADD_ADDRESS -> readNewAddress();
      case PROFILE_EDIT_ADDRESS -> chooseAddressToEdit();
      case PROFILE_DELETE_ADDRESS -> chooseAddressToDelete();
      default -> System.out.println(ConsoleReader.INVALID_CHOICE_MESSAGE);
    }
    return true;
  }

  private void readProfileChanges() {
    String name = reader.readLine("New full name");
    String phone = reader.readLine("New phone number");
    try {
      currentAccount = userService.updateProfile(currentAccount.getId(), name, phone);
      System.out.println("Profile updated.");
    } catch (IllegalArgumentException e) {
      System.out.println("Update failed: " + e.getMessage());
    }
  }

  private void readNewAddress() {
    Address address = reader.readAddress();
    try {
      userService.addAddress(currentAccount.getId(), address);
      System.out.println("Address saved.");
    } catch (IllegalArgumentException e) {
      System.out.println("Could not save the address: " + e.getMessage());
    }
  }

  private void chooseAddressToEdit() {
    List<Address> addresses = currentAccount.getAddresses();
    reader.select("Address number to edit", addresses).ifPresent(this::replaceAddress);
  }

  private void replaceAddress(Address address) {
    int index = currentAccount.getAddresses().indexOf(address);
    Address replacement = reader.readAddress();
    try {
      userService.updateAddress(currentAccount.getId(), index, replacement);
      System.out.println("Address updated.");
    } catch (IllegalArgumentException e) {
      System.out.println("Could not update the address: " + e.getMessage());
    }
  }

  private void chooseAddressToDelete() {
    List<Address> addresses = currentAccount.getAddresses();
    reader.select("Address number to delete", addresses).ifPresent(this::deleteAddress);
  }

  private void deleteAddress(Address address) {
    int index = currentAccount.getAddresses().indexOf(address);
    try {
      userService.removeAddress(currentAccount.getId(), index);
      System.out.println("Address deleted.");
    } catch (IllegalArgumentException e) {
      System.out.println("Could not delete the address: " + e.getMessage());
    }
  }

  private void showProfile() {
    Account profile = userService.getProfile(currentAccount.getId());
    System.out.println();
    System.out.println("--- Profile ---");
    System.out.println("Name:  " + profile.getFullName());
    System.out.println("Email: " + profile.getEmail());
    System.out.println("Phone: " + profile.getPhone());
    System.out.println("Role:  " + profile.getRole());
    List<Address> addresses = profile.getAddresses();
    if (addresses.isEmpty()) {
      System.out.println("Addresses: none");
      return;
    }
    System.out.println("Addresses:");
    for (int i = 0; i < addresses.size(); i++) {
      System.out.println((i + 1) + ". " + addresses.get(i));
    }
  }

  private void printProfileMenu() {
    System.out.println(PROFILE_CHANGE + ". Change name and phone");
    System.out.println(PROFILE_ADD_ADDRESS + ". Add an address");
    System.out.println(PROFILE_EDIT_ADDRESS + ". Edit an address");
    System.out.println(PROFILE_DELETE_ADDRESS + ". Delete an address");
    System.out.println(PROFILE_BACK + ". Back");
  }
}