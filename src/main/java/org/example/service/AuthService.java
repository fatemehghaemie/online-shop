package org.example.service;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.example.model.Account;
import org.example.model.Email;
import org.example.model.PasswordHash;
import org.example.model.PhoneNumber;
import org.example.model.Role;

/** Use cases for registering, logging in, logging out and recovering a password. */
public final class AuthService {

  private static final int MIN_PASSWORD_LENGTH = 8;
  private static final String EMAIL_SEPARATOR = "@";
  private static final String LOGIN_FAILURE_MESSAGE = "Invalid email or password";
  private static final String NOT_REGISTERED_MESSAGE =
      "No account is registered with this email or phone number";

  private final AccountRepository accountRepository;

  /**
   * Creates the service.
   *
   * @param accountRepository where accounts are stored; must not be null
   */
  public AuthService(AccountRepository accountRepository) {
    this.accountRepository =
        Objects.requireNonNull(accountRepository, "accountRepository must not be null");
  }

  /**
   * Registers a new customer account.
   *
   * <p>REQUIRES: all arguments are valid: a well-formed email not used by another account, a
   * valid phone number, a password of at least eight characters and a non-blank name.
   *
   * <p>MODIFIES: the account repository.
   *
   * <p>EFFECTS: Stores and returns a new account with the CUSTOMER role. If any argument is
   * invalid, an exception is thrown and nothing is stored.
   *
   * @param email the login email
   * @param phone the contact phone number
   * @param pass the plain-text password
   * @param name the display name
   * @return the created account
   * @throws IllegalArgumentException if an argument is invalid or the email is already used
   */
  public Account register(String email, String phone, String pass, String name) {
    Email validEmail = new Email(email);
    PhoneNumber validPhone = new PhoneNumber(phone);
    requireValidPassword(pass);
    if (accountRepository.existsByEmail(validEmail)) {
      throw new IllegalArgumentException("Email is already registered: " + validEmail);
    }
    Account account =
        new Account(Role.CUSTOMER, validEmail, validPhone, PasswordHash.fromRaw(pass), name);
    accountRepository.save(account);
    return account;
  }

  /**
   * Finds the account that matches an email and password.
   *
   * <p>REQUIRES: email is well formed.
   *
   * <p>MODIFIES: nothing.
   *
   * <p>EFFECTS: Returns the account if the password is correct. The error message is the same
   * for an unknown email and a wrong password.
   *
   * @param email the login email
   * @param pass the plain-text password
   * @return the logged-in account
   * @throws IllegalArgumentException if the email is malformed or the credentials do not match
   */
  public Account login(String email, String pass) {
    Email validEmail = new Email(email);
    return accountRepository
        .findByEmail(validEmail)
        .filter(account -> account.verifyPassword(pass))
        .orElseThrow(() -> new IllegalArgumentException(LOGIN_FAILURE_MESSAGE));
  }

  /**
   * Ends the session of an account.
   *
   * <p>REQUIRES: accountId belongs to an existing account.
   *
   * <p>MODIFIES: nothing. This service keeps no session; the caller forgets the current
   * account.
   *
   * <p>EFFECTS: Returns normally if the account exists.
   *
   * @param accountId the account that logs out; must not be null
   * @throws IllegalArgumentException if no such account exists
   */
  public void logout(UUID accountId) {
    accountRepository.getById(accountId);
  }

  /**
   * Sets a new password for the account registered with an email or a phone number.
   *
   * <p>REQUIRES: emailOrPhone is a well-formed email or phone number of an existing account
   * and newPassword has at least eight characters.
   *
   * <p>MODIFIES: the account and the account repository.
   *
   * <p>EFFECTS: After the call only newPassword logs the account in. If a requirement is not
   * met, an exception is thrown and nothing is changed. No verification code is sent in this
   * phase, so this operation trusts the caller.
   *
   * @param emailOrPhone the registered email or phone number
   * @param newPassword the plain-text new password
   * @throws IllegalArgumentException if the identifier is malformed or unknown or the password
   *     is too short
   */
  public void resetPassword(String emailOrPhone, String newPassword) {
    requireValidPassword(newPassword);
    Account account = findByEmailOrPhone(emailOrPhone);
    account.changePassword(PasswordHash.fromRaw(newPassword));
    accountRepository.save(account);
  }

  private Account findByEmailOrPhone(String emailOrPhone) {
    if (emailOrPhone == null || emailOrPhone.isBlank()) {
      throw new IllegalArgumentException("Email or phone number must not be blank");
    }
    Optional<Account> account =
        emailOrPhone.contains(EMAIL_SEPARATOR)
            ? accountRepository.findByEmail(new Email(emailOrPhone))
            : accountRepository.findByPhone(new PhoneNumber(emailOrPhone));
    return account.orElseThrow(() -> new IllegalArgumentException(NOT_REGISTERED_MESSAGE));
  }

  private static void requireValidPassword(String password) {
    if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
      throw new IllegalArgumentException(
          "Password must have at least " + MIN_PASSWORD_LENGTH + " characters");
    }
  }
}