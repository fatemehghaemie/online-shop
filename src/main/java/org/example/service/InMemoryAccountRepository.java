package org.example.service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.example.model.Account;
import org.example.model.Email;
import org.example.model.PhoneNumber;

/** {@link AccountRepository} that keeps accounts in memory. */
public final class InMemoryAccountRepository implements AccountRepository {

  // AF: the stored accounts are the values of accountsById.
  // RI: no value is null and every key equals the id of its account.
  private final Map<UUID, Account> accountsById = new LinkedHashMap<>();

  @Override
  public void save(Account account) {
    Objects.requireNonNull(account, "account must not be null");
    accountsById.put(account.getId(), account);
    checkRep();
  }

  @Override
  public Optional<Account> findById(UUID id) {
    Objects.requireNonNull(id, "id must not be null");
    return Optional.ofNullable(accountsById.get(id));
  }

  @Override
  public Optional<Account> findByEmail(Email email) {
    Objects.requireNonNull(email, "email must not be null");
    return accountsById.values().stream()
        .filter(account -> account.getEmail().equals(email))
        .findFirst();
  }

  @Override
  public Optional<Account> findByPhone(PhoneNumber phone) {
    Objects.requireNonNull(phone, "phone must not be null");
    return accountsById.values().stream()
        .filter(account -> account.getPhone().equals(phone))
        .findFirst();
  }

  @Override
  public boolean existsByEmail(Email email) {
    return findByEmail(email).isPresent();
  }

  private void checkRep() {
    for (Map.Entry<UUID, Account> entry : accountsById.entrySet()) {
      assert entry.getValue() != null : "null account";
      assert entry.getValue().getId().equals(entry.getKey()) : "key does not match account id";
    }
  }
}