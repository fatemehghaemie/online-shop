package org.example.cli;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.example.model.Account;
import org.example.model.Address;
import org.example.model.Cart;
import org.example.model.CartLine;
import org.example.model.Money;
import org.example.model.Order;
import org.example.model.OrderLine;
import org.example.model.OrderStatus;
import org.example.model.Payment;
import org.example.model.PaymentStatus;
import org.example.model.Product;
import org.example.service.CartService;
import org.example.service.OrderService;
import org.example.service.PaymentService;
import org.example.service.UserService;

/** Console screens for the cart, checkout, payment, order history and order administration. */
public final class CartOrderCLI {

  private static final int CART_BACK = 0;
  private static final int CART_VIEW = 1;
  private static final int CART_CHANGE_QUANTITY = 2;
  private static final int CART_REMOVE = 3;
  private static final int CART_EMPTY = 4;
  private static final int CART_CHECKOUT = 5;
  private static final int CART_PAY = 6;
  private static final int CART_HISTORY = 7;

  private static final int ADMIN_ORDERS_BACK = 0;
  private static final int ADMIN_ALL_ORDERS = 1;
  private static final int ADMIN_ORDERS_BY_STATUS = 2;

  private static final int CANCEL_NUMBER = 0;
  private static final String EMPTY_CART_MESSAGE = "Your cart is empty.";
  private static final String ADMIN_REQUIRED_MESSAGE = "Administrator privileges are required.";
  private static final String GATEWAY_DECLINED_REASON = "Declined by the payment gateway";

  private final CartService cartService;
  private final OrderService orderService;
  private final PaymentService paymentService;
  private final UserService userService;
  private final ConsoleReader reader;

  /**
   * Creates the screens.
   *
   * @param cartService reads and edits carts; must not be null
   * @param orderService places orders and lists them; must not be null
   * @param paymentService pays orders; must not be null
   * @param userService saves a new delivery address; must not be null
   * @param reader reads the user's input; must not be null
   */
  public CartOrderCLI(
      CartService cartService,
      OrderService orderService,
      PaymentService paymentService,
      UserService userService,
      ConsoleReader reader) {
    this.cartService = Objects.requireNonNull(cartService, "cartService must not be null");
    this.orderService = Objects.requireNonNull(orderService, "orderService must not be null");
    this.paymentService =
        Objects.requireNonNull(paymentService, "paymentService must not be null");
    this.userService = Objects.requireNonNull(userService, "userService must not be null");
    this.reader = Objects.requireNonNull(reader, "reader must not be null");
  }

  /**
   * Prints the lines and the total of a cart.
   *
   * <p>REQUIRES: cart is non-null.
   *
   * <p>MODIFIES: the console.
   *
   * @param cart the cart to show
   */
  public void showCart(Cart cart) {
    List<CartLine> lines = cart.getLines();
    if (lines.isEmpty()) {
      System.out.println(EMPTY_CART_MESSAGE);
      return;
    }
    System.out.println();
    System.out.println("--- Your cart ---");
    for (int i = 0; i < lines.size(); i++) {
      CartLine line = lines.get(i);
      System.out.println(
          (i + 1)
              + ". "
              + line.getProductName()
              + " x "
              + line.getQuantity()
              + " @ "
              + line.getUnitPrice()
              + " = "
              + line.calculateSubtotal());
    }
    System.out.println("Total: " + cart.calculateTotal());
  }

  /**
   * Asks for a new delivery address.
   *
   * <p>MODIFIES: the console and the input stream.
   *
   * <p>EFFECTS: Asks again until the address is valid. The address is not saved here.
   *
   * @return the address the user typed
   */
  public Address readCheckoutAddress() {
    System.out.println("New delivery address:");
    return reader.readAddress();
  }

  /**
   * Prints the amount the user has to pay.
   *
   * <p>REQUIRES: amount is non-null.
   *
   * <p>MODIFIES: the console.
   *
   * @param amount the amount due, as calculated by the system
   */
  public void showPaymentPrompt(Money amount) {
    System.out.println("Amount due: " + amount + " (set by the system; it cannot be changed).");
  }

  /**
   * Prints the details of an order: products, quantities, amount, address and status.
   *
   * <p>REQUIRES: order is non-null.
   *
   * <p>MODIFIES: the console.
   *
   * @param order the order to show
   */
  public void showOrderDetails(Order order) {
    System.out.println();
    System.out.println("--- Order details ---");
    System.out.println("Status: " + order.getStatus());
    for (OrderLine line : order.getLines()) {
      System.out.println(
          "- "
              + line.getProductName()
              + " x "
              + line.getQuantity()
              + " @ "
              + line.getUnitPrice()
              + " = "
              + line.getSubtotal());
    }
    System.out.println("Total: " + order.getTotalAmount());
    System.out.println("Shipping address: " + order.getShippingAddress());
  }

  /**
   * Asks for a quantity and adds a product to the cart of an account.
   *
   * <p>REQUIRES: account and product are non-null.
   *
   * <p>MODIFIES: the console and, on success, the cart.
   *
   * <p>EFFECTS: On failure the reason is printed and the cart is unchanged.
   *
   * @param account the logged-in account
   * @param product the product to add
   */
  public void addProductToCart(Account account, Product product) {
    int quantity = reader.readInt("Quantity");
    try {
      cartService.addProduct(account.getId(), product.getId(), quantity);
      System.out.println("Added " + quantity + " x " + product.getName() + " to your cart.");
    } catch (IllegalArgumentException e) {
      System.out.println("Could not add to the cart: " + e.getMessage());
    }
  }

  /**
   * Shows the cart and order menu of a logged-in account until the user goes back.
   *
   * <p>REQUIRES: account is non-null.
   *
   * <p>MODIFIES: the console and, through the services, the cart, orders and payments.
   *
   * @param account the logged-in account
   */
  public void openCartMenu(Account account) {
    Objects.requireNonNull(account, "account must not be null");
    boolean inMenu = true;
    while (inMenu) {
      printCartMenu();
      inMenu = handleCartChoice(reader.readInt("Choice"), account);
    }
  }

  /**
   * Shows the order administration menu until the user goes back.
   *
   * <p>REQUIRES: admin is non-null.
   *
   * <p>MODIFIES: the console and the input stream; orders are only read.
   *
   * <p>EFFECTS: Prints a message and returns at once if the account is not an administrator.
   *
   * @param admin the logged-in administrator
   */
  public void openAdminOrderMenu(Account admin) {
    Objects.requireNonNull(admin, "admin must not be null");
    if (!admin.isAdmin()) {
      System.out.println(ADMIN_REQUIRED_MESSAGE);
      return;
    }
    boolean inMenu = true;
    while (inMenu) {
      printAdminOrderMenu();
      inMenu = handleAdminOrderChoice(reader.readInt("Choice"), admin);
    }
  }

  private boolean handleCartChoice(int choice, Account account) {
    if (choice == CART_BACK) {
      return false;
    }
    switch (choice) {
      case CART_VIEW -> showCart(cartService.getCart(account.getId()));
      case CART_CHANGE_QUANTITY -> changeQuantity(account);
      case CART_REMOVE -> removeProductFromCart(account);
      case CART_EMPTY -> emptyCart(account);
      case CART_CHECKOUT -> checkout(account);
      case CART_PAY -> payOrder(account);
      case CART_HISTORY -> showOrderHistory(account);
      default -> System.out.println(ConsoleReader.INVALID_CHOICE_MESSAGE);
    }
    return true;
  }

  private boolean handleAdminOrderChoice(int choice, Account admin) {
    if (choice == ADMIN_ORDERS_BACK) {
      return false;
    }
    switch (choice) {
      case ADMIN_ALL_ORDERS -> showAllOrders(admin);
      case ADMIN_ORDERS_BY_STATUS -> chooseStatusAndShowOrders(admin);
      default -> System.out.println(ConsoleReader.INVALID_CHOICE_MESSAGE);
    }
    return true;
  }

  private void changeQuantity(Account account) {
    Cart cart = cartService.getCart(account.getId());
    showCart(cart);
    reader.select("Line number", cart.getLines()).ifPresent(line -> setQuantity(account, line));
  }

  private void setQuantity(Account account, CartLine line) {
    int quantity = reader.readInt("New quantity");
    try {
      cartService.updateQuantity(account.getId(), line.getProductId(), quantity);
      System.out.println(line.getProductName() + " now has quantity " + quantity + ".");
    } catch (IllegalArgumentException e) {
      System.out.println("Could not change the quantity: " + e.getMessage());
    }
  }

  private void removeProductFromCart(Account account) {
    Cart cart = cartService.getCart(account.getId());
    showCart(cart);
    reader.select("Line number", cart.getLines()).ifPresent(line -> removeLine(account, line));
  }

  private void removeLine(Account account, CartLine line) {
    cartService.removeProduct(account.getId(), line.getProductId());
    System.out.println("Removed " + line.getProductName() + " from your cart.");
  }

  private void emptyCart(Account account) {
    if (cartService.getCart(account.getId()).getLines().isEmpty()) {
      System.out.println(EMPTY_CART_MESSAGE);
      return;
    }
    if (reader.confirm("Remove all products from your cart?")) {
      cartService.clearCart(account.getId());
      System.out.println(EMPTY_CART_MESSAGE);
    }
  }

  private void checkout(Account account) {
    Cart cart = cartService.getCart(account.getId());
    if (cart.getLines().isEmpty()) {
      System.out.println(EMPTY_CART_MESSAGE);
      return;
    }
    showCart(cart);
    selectCheckoutAddress(account).ifPresent(address -> placeOrder(account, cart, address));
  }

  private Optional<Address> selectCheckoutAddress(Account account) {
    List<Address> addresses = account.getAddresses();
    System.out.println("Choose a delivery address:");
    for (int i = 0; i < addresses.size(); i++) {
      System.out.println((i + 1) + ". " + addresses.get(i));
    }
    int newAddressNumber = addresses.size() + 1;
    System.out.println(newAddressNumber + ". Add a new address");
    int choice = reader.readInt("Address number (0 to cancel)");
    if (choice == CANCEL_NUMBER) {
      return Optional.empty();
    }
    if (choice == newAddressNumber) {
      return saveNewAddress(account);
    }
    if (choice < 1 || choice > addresses.size()) {
      System.out.println(ConsoleReader.INVALID_CHOICE_MESSAGE);
      return Optional.empty();
    }
    return Optional.of(addresses.get(choice - 1));
  }

  private Optional<Address> saveNewAddress(Account account) {
    Address address = readCheckoutAddress();
    try {
      userService.addAddress(account.getId(), address);
      return Optional.of(address);
    } catch (IllegalArgumentException e) {
      System.out.println("Could not save the address: " + e.getMessage());
      return Optional.empty();
    }
  }

  private void placeOrder(Account account, Cart cart, Address address) {
    System.out.println("Final amount to pay: " + cart.calculateTotal());
    System.out.println("Delivery address: " + address);
    if (!reader.confirm("Place this order?")) {
      System.out.println("Order cancelled.");
      return;
    }
    try {
      Order order = orderService.checkout(account.getId(), address);
      System.out.println(
          "Order placed. Total: " + order.getTotalAmount() + ", status: " + order.getStatus());
      System.out.println("Choose option " + CART_PAY + " in this menu to pay for it.");
    } catch (IllegalArgumentException | IllegalStateException e) {
      System.out.println("Checkout failed: " + e.getMessage());
    }
  }

  private void payOrder(Account account) {
    List<Order> pendingOrders =
        orderService.getOrderHistory(account.getId()).stream()
            .filter(order -> order.getStatus() == OrderStatus.PENDING)
            .toList();
    if (pendingOrders.isEmpty()) {
      System.out.println("You have no orders waiting for payment.");
      return;
    }
    printOrders(pendingOrders);
    reader.select("Order number", pendingOrders).ifPresent(this::payFor);
  }

  private void payFor(Order order) {
    showPaymentPrompt(order.getTotalAmount());
    boolean approved = reader.confirm("Simulated payment gateway: approve the payment?");
    try {
      Payment payment =
          approved
              ? paymentService.processPayment(order.getId(), order.getTotalAmount())
              : paymentService.recordFailedPayment(
                  order.getId(), order.getTotalAmount(), GATEWAY_DECLINED_REASON);
      showPaymentResult(order, payment);
    } catch (IllegalArgumentException | IllegalStateException e) {
      System.out.println("Payment failed: " + e.getMessage());
    }
  }

  private void showPaymentResult(Order order, Payment payment) {
    if (payment.getStatus() != PaymentStatus.SUCCESSFUL) {
      System.out.println(
          "Payment status: "
              + payment.getStatus()
              + " ("
              + payment.getFailureReason()
              + "). The order is still unpaid; you can try again.");
      return;
    }
    boolean verified = paymentService.verifyPayment(order.getId(), payment.getId());
    System.out.println("Payment status: " + payment.getStatus());
    System.out.println("Reference: " + payment.getReferenceId() + ", verified: " + verified);
    System.out.println("Order status: " + order.getStatus());
  }

  private void showOrderHistory(Account account) {
    System.out.println();
    System.out.println("--- Your orders ---");
    listAndOpenOrders(orderService.getOrderHistory(account.getId()));
  }

  private void showAllOrders(Account admin) {
    try {
      listAndOpenOrders(orderService.getAllOrders(admin.getId()));
    } catch (IllegalArgumentException | SecurityException e) {
      System.out.println("Could not list the orders: " + e.getMessage());
    }
  }

  private void chooseStatusAndShowOrders(Account admin) {
    List<OrderStatus> statuses = List.of(OrderStatus.values());
    for (int i = 0; i < statuses.size(); i++) {
      System.out.println((i + 1) + ". " + statuses.get(i));
    }
    reader.select("Status number", statuses).ifPresent(status -> showOrdersByStatus(admin, status));
  }

  private void showOrdersByStatus(Account admin, OrderStatus status) {
    try {
      listAndOpenOrders(orderService.getOrdersByStatus(admin.getId(), status));
    } catch (IllegalArgumentException | SecurityException e) {
      System.out.println("Could not list the orders: " + e.getMessage());
    }
  }

  private void listAndOpenOrders(List<Order> orders) {
    if (orders.isEmpty()) {
      System.out.println("No orders found.");
      return;
    }
    printOrders(orders);
    reader.select("Order number for details", orders).ifPresent(this::showOrderDetails);
  }

  private void printOrders(List<Order> orders) {
    for (int i = 0; i < orders.size(); i++) {
      System.out.println((i + 1) + ". " + describe(orders.get(i)));
    }
  }

  private static String describe(Order order) {
    return order.getTotalAmount()
        + " | "
        + order.getStatus()
        + " | "
        + order.getLines().size()
        + " item(s) | "
        + order.getShippingAddress();
  }

  private void printCartMenu() {
    System.out.println();
    System.out.println("--- Cart and orders ---");
    System.out.println(CART_VIEW + ". View cart");
    System.out.println(CART_CHANGE_QUANTITY + ". Change the quantity of a product");
    System.out.println(CART_REMOVE + ". Remove a product from the cart");
    System.out.println(CART_EMPTY + ". Empty the cart");
    System.out.println(CART_CHECKOUT + ". Checkout");
    System.out.println(CART_PAY + ". Pay an order");
    System.out.println(CART_HISTORY + ". Order history");
    System.out.println(CART_BACK + ". Back");
  }

  private void printAdminOrderMenu() {
    System.out.println();
    System.out.println("--- Order administration ---");
    System.out.println(ADMIN_ALL_ORDERS + ". All orders");
    System.out.println(ADMIN_ORDERS_BY_STATUS + ". Orders with a status");
    System.out.println(ADMIN_ORDERS_BACK + ". Back");
  }
}