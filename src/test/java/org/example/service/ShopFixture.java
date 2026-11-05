package org.example.service;

import java.math.BigDecimal;
import org.example.model.Account;
import org.example.model.Address;
import org.example.model.Category;
import org.example.model.Email;
import org.example.model.Money;
import org.example.model.Order;
import org.example.model.PasswordHash;
import org.example.model.PhoneNumber;
import org.example.model.Product;
import org.example.model.Role;
import org.example.model.StockQuantity;

/** Builds an in-memory shop with an administrator and helpers for common test set-up. */
final class ShopFixture {

  static final String CUSTOMER_PASSWORD = "Password1";
  static final Address ADDRESS = new Address("Tehran", "Tehran", "Valiasr St", "1234567890");

  final AccountRepository accountRepository = new InMemoryAccountRepository();
  final ProductRepository productRepository = new InMemoryProductRepository();
  final OrderRepository orderRepository = new InMemoryOrderRepository();
  final AuthService authService = new AuthService(accountRepository);
  final UserService userService = new UserService(accountRepository);
  final InventoryService inventoryService =
      new InventoryService(productRepository, accountRepository);
  final CartService cartService = new CartService(accountRepository, productRepository);
  final OrderService orderService =
      new OrderService(orderRepository, cartService, productRepository, accountRepository);
  final PaymentService paymentService = new PaymentService(orderRepository);
  final ReviewService reviewService =
      new ReviewService(accountRepository, productRepository, orderRepository);
  final ProductService productService =
      new ProductService(productRepository, accountRepository, reviewService, orderRepository);
  final Account admin;

  ShopFixture() {
    admin =
        new Account(
            Role.ADMIN,
            new Email("admin@shop.com"),
            new PhoneNumber("09120000000"),
            PasswordHash.fromRaw("Admin1234"),
            "Shop Admin");
    accountRepository.save(admin);
  }

  static Money usd(String amount) {
    return new Money(new BigDecimal(amount), Money.DEFAULT_CURRENCY);
  }

  /** Registers a customer who already has {@link #ADDRESS} in the address book. */
  Account registerCustomer(String email, String phone) {
    Account account = authService.register(email, phone, CUSTOMER_PASSWORD, "Test Customer");
    userService.addAddress(account.getId(), ADDRESS);
    return account;
  }

  Category createCategory(String name) {
    Category category = new Category(name);
    productRepository.saveCategory(category);
    return category;
  }

  Product createProduct(String name, String price, int stock, Category category) {
    return productService.createProduct(
        admin.getId(), name, usd(price), new StockQuantity(stock), category.getId());
  }

  /** Puts the product in the cart of the customer and checks out; the order is PENDING. */
  Order placeOrder(Account customer, Product product, int quantity) {
    cartService.addProduct(customer.getId(), product.getId(), quantity);
    return orderService.checkout(customer.getId(), ADDRESS);
  }

  /** Like {@link #placeOrder}, and pays the order in full; the order is PAID. */
  Order placePaidOrder(Account customer, Product product, int quantity) {
    Order order = placeOrder(customer, product, quantity);
    paymentService.processPayment(order.getId(), order.getTotalAmount());
    return order;
  }
}