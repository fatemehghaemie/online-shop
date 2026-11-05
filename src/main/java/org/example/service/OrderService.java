package org.example.service;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.example.model.Address;
import org.example.model.Cart;
import org.example.model.CartLine;
import org.example.model.Order;
import org.example.model.OrderLine;
import org.example.model.OrderStatus;
import org.example.model.Product;

/** Use cases for placing orders and reading the order history. */
public final class OrderService {

  private static final String UNAVAILABLE_MESSAGE =
      "Product is not available in the requested quantity: ";

  private final OrderRepository orderRepository;
  private final CartService cartService;
  private final ProductRepository productRepository;
  private final AccountRepository accountRepository;

  /**
   * Creates the service.
   *
   * @param orderRepository where orders are stored; must not be null
   * @param cartService provides the cart that is checked out; must not be null
   * @param productRepository where the catalog is stored, used to reduce stock; must not be
   *     null
   * @param accountRepository where accounts are stored, used to check addresses and
   *     administrators; must not be null
   */
  public OrderService(
      OrderRepository orderRepository,
      CartService cartService,
      ProductRepository productRepository,
      AccountRepository accountRepository) {
    this.orderRepository =
        Objects.requireNonNull(orderRepository, "orderRepository must not be null");
    this.cartService = Objects.requireNonNull(cartService, "cartService must not be null");
    this.productRepository =
        Objects.requireNonNull(productRepository, "productRepository must not be null");
    this.accountRepository =
        Objects.requireNonNull(accountRepository, "accountRepository must not be null");
  }

  /**
   * Turns the cart of an account into a pending order.
   *
   * <p>REQUIRES: the account exists, addr is one of the addresses saved in that account, the
   * cart is not empty and every product in it is still active with enough stock.
   *
   * <p>MODIFIES: the order repository, the stock of the ordered products and the cart.
   *
   * <p>EFFECTS: Stores and returns a PENDING order with the cart lines at their stored prices
   * and a snapshot of addr. The stock of each product is reduced by its ordered quantity and
   * the cart becomes empty. If a requirement is not met, an exception is thrown and nothing is
   * changed.
   *
   * @param accountId the account that places the order; must not be null
   * @param addr the delivery address
   * @return the created order
   * @throws IllegalStateException if the cart is empty
   * @throws IllegalArgumentException if the account does not exist, the address is not saved in
   *     the account or a product is unavailable
   */
  public Order checkout(UUID accountId, Address addr) {
    Objects.requireNonNull(addr, "addr must not be null");
    requireSavedAddress(accountId, addr);
    Cart cart = cartService.getCart(accountId);
    List<CartLine> lines = cart.getLines();
    if (lines.isEmpty()) {
      throw new IllegalStateException("Cart is empty");
    }
    requireAllPurchasable(lines);
    Order order = new Order(accountId, toOrderLines(lines), addr.toSnapshot());
    reserveStock(lines);
    orderRepository.save(order);
    cart.clear();
    return order;
  }

  /**
   * Lists the orders of an account.
   *
   * <p>REQUIRES: accountId is non-null.
   *
   * <p>MODIFIES: nothing.
   *
   * <p>EFFECTS: Returns the orders in the order they were placed. The list is unmodifiable.
   *
   * @param accountId the account that placed the orders
   * @return the orders of the account, possibly empty
   */
  public List<Order> getOrderHistory(UUID accountId) {
    Objects.requireNonNull(accountId, "accountId must not be null");
    return orderRepository.findByOwnerId(accountId);
  }

  /**
   * Lists the orders of all customers.
   *
   * <p>REQUIRES: adminId belongs to an administrator.
   *
   * <p>MODIFIES: nothing.
   *
   * <p>EFFECTS: Returns every order in the order it was placed. The list is unmodifiable.
   *
   * @param adminId the administrator who reviews the orders; must not be null
   * @return all orders, possibly empty
   * @throws SecurityException if adminId is not an administrator
   * @throws IllegalArgumentException if no such account exists
   */
  public List<Order> getAllOrders(UUID adminId) {
    accountRepository.getAdminById(adminId);
    return orderRepository.findAll();
  }

  /**
   * Lists the orders of all customers that have one status.
   *
   * <p>REQUIRES: adminId belongs to an administrator and status is non-null.
   *
   * <p>MODIFIES: nothing.
   *
   * <p>EFFECTS: Returns the orders whose status equals status, in the order they were placed.
   * The list is unmodifiable.
   *
   * @param adminId the administrator who reviews the orders; must not be null
   * @param status the status to keep
   * @return the matching orders, possibly empty
   * @throws SecurityException if adminId is not an administrator
   * @throws IllegalArgumentException if no such account exists
   */
  public List<Order> getOrdersByStatus(UUID adminId, OrderStatus status) {
    Objects.requireNonNull(status, "status must not be null");
    return getAllOrders(adminId).stream().filter(order -> order.getStatus() == status).toList();
  }

  private void requireSavedAddress(UUID accountId, Address addr) {
    if (!accountRepository.getById(accountId).getAddresses().contains(addr)) {
      throw new IllegalArgumentException("Address does not belong to this account");
    }
  }

  private void requireAllPurchasable(List<CartLine> lines) {
    for (CartLine line : lines) {
      Product product = productRepository.getById(line.getProductId());
      if (!product.canBePurchased(line.getQuantity())) {
        throw new IllegalArgumentException(UNAVAILABLE_MESSAGE + product.getName());
      }
    }
  }

  private void reserveStock(List<CartLine> lines) {
    for (CartLine line : lines) {
      Product product = productRepository.getById(line.getProductId());
      product.adjustStock(-line.getQuantity().getValue());
      productRepository.save(product);
    }
  }

  private static List<OrderLine> toOrderLines(List<CartLine> lines) {
    return lines.stream().map(OrderLine::fromCartLine).toList();
  }
}