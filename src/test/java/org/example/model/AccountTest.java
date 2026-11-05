package org.example.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AccountTest {

  private static final Address HOME = new Address("Tehran", "Tehran", "Valiasr St", "1234567890");
  private static final Address WORK = new Address("Fars", "Shiraz", "Zand St", "7134567890");

  private Account account;

  @BeforeEach
  void setUp() {
    account =
        new Account(
            Role.CUSTOMER,
            new Email("ali@example.com"),
            new PhoneNumber("09121234567"),
            PasswordHash.fromRaw("Password1"),
            "Ali Rezaei");
    account.addAddress(HOME);
  }

  @Test
  void updateAddressReplacesTheAddressAtTheIndex() {
    account.updateAddress(0, WORK);

    assertEquals(List.of(WORK), account.getAddresses());
  }

  @Test
  void updateAddressRejectsAnAddressThatAlreadyExistsAtAnotherIndex() {
    account.addAddress(WORK);

    assertThrows(IllegalArgumentException.class, () -> account.updateAddress(1, HOME));
    assertEquals(List.of(HOME, WORK), account.getAddresses());
  }

  @Test
  void removeAddressDeletesTheAddressAtTheIndex() {
    account.addAddress(WORK);

    account.removeAddress(0);

    assertEquals(List.of(WORK), account.getAddresses());
  }

  @Test
  void invalidIndexIsRejectedAndTheAddressBookStaysUnchanged() {
    assertThrows(IllegalArgumentException.class, () -> account.removeAddress(3));
    assertThrows(IllegalArgumentException.class, () -> account.updateAddress(-1, WORK));
    assertEquals(List.of(HOME), account.getAddresses());
  }

  @Test
  void changePasswordReplacesTheOldPassword() {
    account.changePassword(PasswordHash.fromRaw("NewPass123"));

    assertTrue(account.verifyPassword("NewPass123"));
    assertFalse(account.verifyPassword("Password1"));
  }
}