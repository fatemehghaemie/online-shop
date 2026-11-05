package org.example.service;

import java.util.Objects;
import java.util.UUID;
import org.example.model.Account;
import org.example.model.Address;
import org.example.model.PhoneNumber;

/** Use cases for viewing and changing the profile and addresses of an account. */
public final class UserService {

  private final AccountRepository accountRepository;

  /**
   * Creates the service.
   *
   * @param accountRepository where accounts are stored; must not be null
   */
  public UserService(AccountRepository accountRepository) {
    this.accountRepository =
        Objects.requireNonNull(accountRepository, "accountRepository must not be null");
  }

  /**
   * Returns the account with the given id.
   *
   * <p>REQUIRES: accountId belongs to an existing account.
   *
   * <p>MODIFIES: nothing.
   *
   * <p>EFFECTS: Returns the stored account instance.
   *
   * @param accountId the account to read; must not be null
   * @return the account
   * @throws IllegalArgumentException if no such account exists
   */
  public Account getProfile(UUID accountId) {
    return accountRepository.getById(accountId);
  }

  /**
   * Changes the name and phone number of an account.
   *
   * <p>REQUIRES: the account exists, name is non-blank and phone is a valid phone number.
   *
   * <p>MODIFIES: the account and the account repository.
   *
   * <p>EFFECTS: Stores and returns the updated account. If an argument is invalid, an
   * exception is thrown and the account is unchanged.
   *
   * @param accountId the account to change; must not be null
   * @param name the new display name
   * @param phone the new phone number
   * @return the updated account
   * @throws IllegalArgumentException if the account does not exist or an argument is invalid
   */
  public Account updateProfile(UUID accountId, String name, String phone) {
    Account account = accountRepository.getById(accountId);
    account.updateProfile(name, new PhoneNumber(phone));
    accountRepository.save(account);
    return account;
  }

  /**
   * Adds an address to the address book of an account.
   *
   * <p>REQUIRES: the account exists and addr is non-null.
   *
   * <p>MODIFIES: the account and the account repository.
   *
   * <p>EFFECTS: The account's address book contains addr exactly once.
   *
   * @param accountId the account to change; must not be null
   * @param addr the address to add
   * @throws IllegalArgumentException if the account does not exist
   */
  public void addAddress(UUID accountId, Address addr) {
    Account account = accountRepository.getById(accountId);
    account.addAddress(addr);
    accountRepository.save(account);
  }

  /**
   * Replaces one address of an account.
   *
   * <p>REQUIRES: the account exists, index is a valid position counted from 0, and addr is
   * non-null and not stored at another position.
   *
   * <p>MODIFIES: the account and the account repository.
   *
   * <p>EFFECTS: The address at index becomes addr. If a requirement is not met, an exception
   * is thrown and the address book is unchanged.
   *
   * @param accountId the account to change; must not be null
   * @param index the position to replace, counted from 0
   * @param addr the new address
   * @throws IllegalArgumentException if the account does not exist or an argument is invalid
   */
  public void updateAddress(UUID accountId, int index, Address addr) {
    Account account = accountRepository.getById(accountId);
    account.updateAddress(index, addr);
    accountRepository.save(account);
  }

  /**
   * Deletes one address of an account.
   *
   * <p>REQUIRES: the account exists and index is a valid position counted from 0.
   *
   * <p>MODIFIES: the account and the account repository.
   *
   * <p>EFFECTS: The address at index is removed. If a requirement is not met, an exception is
   * thrown and the address book is unchanged.
   *
   * @param accountId the account to change; must not be null
   * @param index the position to delete, counted from 0
   * @throws IllegalArgumentException if the account does not exist or the position is invalid
   */
  public void removeAddress(UUID accountId, int index) {
    Account account = accountRepository.getById(accountId);
    account.removeAddress(index);
    accountRepository.save(account);
  }
}