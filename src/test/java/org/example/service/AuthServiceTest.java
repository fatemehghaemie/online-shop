package org.example.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.example.model.Account;
import org.example.model.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AuthServiceTest {

  private static final String EMAIL = "user@example.com";
  private static final String PHONE = "09121234567";
  private static final String PASSWORD = "Password1";

  private AuthService authService;

  @BeforeEach
  void setUp() {
    authService = new AuthService(new InMemoryAccountRepository());
  }

  @Test
  void registerCreatesCustomerAccountThatCanLogIn() {
    Account registered = authService.register(EMAIL, PHONE, PASSWORD, "Ali Rezaei");

    assertEquals(Role.CUSTOMER, registered.getRole());
    assertFalse(registered.isAdmin());
    assertSame(registered, authService.login(EMAIL, PASSWORD));
  }

  @Test
  void registerRejectsEmailThatIsAlreadyUsed() {
    authService.register(EMAIL, PHONE, PASSWORD, "Ali Rezaei");

    assertThrows(
        IllegalArgumentException.class,
        () -> authService.register(EMAIL, PHONE, PASSWORD, "Someone Else"));
  }

  @Test
  void registerRejectsShortPassword() {
    assertThrows(
        IllegalArgumentException.class,
        () -> authService.register(EMAIL, PHONE, "short", "Ali Rezaei"));
  }

  @Test
  void loginFailureMessageIsTheSameForUnknownEmailAndWrongPassword() {
    authService.register(EMAIL, PHONE, PASSWORD, "Ali Rezaei");

    IllegalArgumentException unknownEmail =
        assertThrows(
            IllegalArgumentException.class,
            () -> authService.login("nobody@example.com", PASSWORD));
    IllegalArgumentException wrongPassword =
        assertThrows(
            IllegalArgumentException.class, () -> authService.login(EMAIL, "WrongPass1"));

    assertEquals(unknownEmail.getMessage(), wrongPassword.getMessage());
  }
}