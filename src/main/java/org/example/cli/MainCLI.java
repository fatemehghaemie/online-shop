package org.example.cli;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import org.example.model.Account;

/** Top-level console menu that sends the user to the other screens. */
public final class MainCLI {

  private static final int EXIT_CHOICE = 0;
  private static final int GUEST_REGISTER = 1;
  private static final int GUEST_LOGIN = 2;
  private static final int GUEST_FORGOT_PASSWORD = 3;
  private static final int GUEST_BROWSE = 4;
  private static final int MEMBER_CATALOG = 1;
  private static final int MEMBER_ADD_TO_CART = 2;
  private static final int MEMBER_CART_AND_ORDERS = 3;
  private static final int MEMBER_PROFILE = 4;
  private static final int MEMBER_LOGOUT = 5;
  private static final int MEMBER_ADMIN_PRODUCTS = 6;
  private static final int MEMBER_ADMIN_ORDERS = 7;

  private final AuthCLI authCli;
  private final ProductCLI productCli;
  private final CartOrderCLI cartOrderCli;
  private final ConsoleReader reader;

  /**
   * Creates the menu.
   *
   * @param authCli registration, login and profile screens; must not be null
   * @param productCli catalog screens; must not be null
   * @param cartOrderCli cart and order screens; must not be null
   * @param reader reads the user's input; must not be null
   */
  public MainCLI(
      AuthCLI authCli, ProductCLI productCli, CartOrderCLI cartOrderCli, ConsoleReader reader) {
    this.authCli = Objects.requireNonNull(authCli, "authCli must not be null");
    this.productCli = Objects.requireNonNull(productCli, "productCli must not be null");
    this.cartOrderCli = Objects.requireNonNull(cartOrderCli, "cartOrderCli must not be null");
    this.reader = Objects.requireNonNull(reader, "reader must not be null");
  }

  /**
   * Runs the application until the user exits or the input ends.
   *
   * <p>MODIFIES: the console, the input stream and, through the screens, the shop data.
   */
  public void start() {
    System.out.println("Welcome to the online shop.");
    try {
      boolean running = true;
      while (running) {
        displayMainMenu();
        running = handleChoice(reader.readInt("Choice"));
      }
    } catch (NoSuchElementException e) {
      System.out.println();
    }
    System.out.println("Goodbye.");
  }

  /**
   * Prints the menu that fits the current state: guest, customer or administrator.
   *
   * <p>MODIFIES: the console.
   */
  public void displayMainMenu() {
    System.out.println();
    System.out.println("=== Main menu ===");
    Optional<Account> account = authCli.getCurrentAccount();
    if (account.isPresent()) {
      printMemberMenu(account.get());
    } else {
      printGuestMenu();
    }
    System.out.println(EXIT_CHOICE + ". Exit");
  }

  private boolean handleChoice(int choice) {
    if (choice == EXIT_CHOICE) {
      return false;
    }
    Optional<Account> account = authCli.getCurrentAccount();
    if (account.isPresent()) {
      handleMemberChoice(choice, account.get());
    } else {
      handleGuestChoice(choice);
    }
    return true;
  }

  private void handleGuestChoice(int choice) {
    switch (choice) {
      case GUEST_REGISTER -> authCli.readRegistrationData();
      case GUEST_LOGIN -> authCli.readLoginData();
      case GUEST_FORGOT_PASSWORD -> authCli.readPasswordResetData();
      case GUEST_BROWSE -> productCli.browseCatalog();
      default -> System.out.println(ConsoleReader.INVALID_CHOICE_MESSAGE);
    }
  }

  private void handleMemberChoice(int choice, Account account) {
    switch (choice) {
      case MEMBER_CATALOG -> productCli.openCatalogMenu(account);
      case MEMBER_ADD_TO_CART -> addProductToCart(account);
      case MEMBER_CART_AND_ORDERS -> cartOrderCli.openCartMenu(account);
      case MEMBER_PROFILE -> authCli.openProfileMenu();
      case MEMBER_LOGOUT -> authCli.logout();
      case MEMBER_ADMIN_PRODUCTS -> productCli.openAdminMenu(account);
      case MEMBER_ADMIN_ORDERS -> cartOrderCli.openAdminOrderMenu(account);
      default -> System.out.println(ConsoleReader.INVALID_CHOICE_MESSAGE);
    }
  }

  private void addProductToCart(Account account) {
    productCli
        .selectProduct()
        .ifPresent(product -> cartOrderCli.addProductToCart(account, product));
  }

  private void printGuestMenu() {
    System.out.println(GUEST_REGISTER + ". Register");
    System.out.println(GUEST_LOGIN + ". Log in");
    System.out.println(GUEST_FORGOT_PASSWORD + ". Forgot my password");
    System.out.println(GUEST_BROWSE + ". Browse products");
  }

  private void printMemberMenu(Account account) {
    System.out.println("Logged in as " + account.getFullName() + " (" + account.getRole() + ")");
    System.out.println(MEMBER_CATALOG + ". Catalog");
    System.out.println(MEMBER_ADD_TO_CART + ". Add a product to the cart");
    System.out.println(MEMBER_CART_AND_ORDERS + ". Cart and orders");
    System.out.println(MEMBER_PROFILE + ". Profile");
    System.out.println(MEMBER_LOGOUT + ". Log out");
    if (account.isAdmin()) {
      System.out.println(MEMBER_ADMIN_PRODUCTS + ". Manage products (administrator)");
      System.out.println(MEMBER_ADMIN_ORDERS + ". View all orders (administrator)");
    }
  }
}