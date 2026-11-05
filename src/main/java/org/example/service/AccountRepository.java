package org.example.service;

import java.util.Optional;
import java.util.UUID;
import org.example.model.Account;
import org.example.model.Email;
import org.example.model.PhoneNumber;

/** Storage of accounts that hides how and where they are kept. */
public interface AccountRepository {

  /**
   * Stores an account, replacing the stored account with the same id.
   *
   * <p>REQUIRES: account is non-null.
   *
   * <p>MODIFIES: this repository.
   *
   * <p>EFFECTS: After the call, findById(account.getId()) returns the account. Email
   * uniqueness is not checked here; callers use existsByEmail first.
   *
   * @param account the account to store
   */
  void save(Account account);

  /**
   * Finds an account by its identifier.
   *
   * <p>REQUIRES: id is non-null.
   *
   * <p>EFFECTS: Returns the stored account, or empty if there is none; does not modify this
   * repository. The returned account is the stored instance, not a copy.
   *
   * @param id the account identifier
   * @return the matching account, if any
   */
  Optional<Account> findById(UUID id);

  /**
   * Finds an account by its email.
   *
   * <p>REQUIRES: email is non-null.
   *
   * <p>EFFECTS: Returns the stored account with this email, or empty if there is none; does
   * not modify this repository.
   *
   * @param email the login email
   * @return the matching account, if any
   */
  Optional<Account> findByEmail(Email email);

  /**
   * Finds an account by its phone number.
   *
   * <p>REQUIRES: phone is non-null.
   *
   * <p>EFFECTS: Returns the first stored account with this phone number, or empty if there is
   * none; does not modify this repository.
   *
   * @param phone the contact phone number
   * @return the matching account, if any
   */
  Optional<Account> findByPhone(PhoneNumber phone);

  /**
   * Checks whether an account with the email exists.
   *
   * <p>REQUIRES: email is non-null.
   *
   * <p>EFFECTS: Returns true if findByEmail(email) is present; does not modify this
   * repository.
   *
   * @param email the login email
   * @return true if an account uses the email
   */
  boolean existsByEmail(Email email);

  /**
   * Returns the account with the given identifier, or fails.
   *
   * <p>REQUIRES: id is non-null and belongs to a stored account.
   *
   * <p>EFFECTS: Returns the stored account instance; does not modify this repository.
   *
   * @param id the account identifier
   * @return the matching account
   * @throws IllegalArgumentException if no such account exists
   */
  default Account getById(UUID id) {
    return findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Account not found: " + id));
  }

  /**
   * Returns the administrator with the given identifier, or fails.
   *
   * <p>REQUIRES: id is non-null and belongs to a stored account with the administrator role.
   *
   * <p>EFFECTS: Returns the stored account instance; does not modify this repository.
   *
   * @param id the account identifier
   * @return the matching administrator
   * @throws IllegalArgumentException if no such account exists
   * @throws SecurityException if the account is not an administrator
   */
  default Account getAdminById(UUID id) {
    Account account = getById(id);
    if (!account.isAdmin()) {
      throw new SecurityException("Administrator privileges are required");
    }
    return account;
  }
}