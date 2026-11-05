package org.example;

import java.math.BigDecimal;
import org.example.cli.AuthCLI;
import org.example.cli.CartOrderCLI;
import org.example.cli.ConsoleReader;
import org.example.cli.MainCLI;
import org.example.cli.ProductCLI;
import org.example.model.Account;
import org.example.model.Category;
import org.example.model.Email;
import org.example.model.Money;
import org.example.model.PasswordHash;
import org.example.model.PhoneNumber;
import org.example.model.Role;
import org.example.model.StockQuantity;
import org.example.service.AccountRepository;
import org.example.service.AuthService;
import org.example.service.CartService;
import org.example.service.InMemoryAccountRepository;
import org.example.service.InMemoryOrderRepository;
import org.example.service.InMemoryProductRepository;
import org.example.service.InventoryService;
import org.example.service.OrderRepository;
import org.example.service.OrderService;
import org.example.service.PaymentService;
import org.example.service.ProductRepository;
import org.example.service.ProductService;
import org.example.service.ReviewService;
import org.example.service.UserService;

/** Entry point: wires the layers together and starts the console application. */
public final class Main {

  private static final String ADMIN_EMAIL = "admin@shop.com";
  private static final String ADMIN_PHONE = "09120000000";
  private static final String ADMIN_PASSWORD = "Admin1234";
  private static final String ADMIN_NAME = "Shop Admin";

  private MainCLI mainCli;

  /**
   * Starts the application.
   *
   * @param args ignored
   */
  public static void main(String[] args) {
    Main application = new Main();
    application.setupDependencyInjection();
    System.out.println("Sample administrator: " + ADMIN_EMAIL + " / " + ADMIN_PASSWORD);
    application.mainCli.start();
  }

  private void setupDependencyInjection() {
    AccountRepository accountRepository = new InMemoryAccountRepository();
    ProductRepository productRepository = new InMemoryProductRepository();
    OrderRepository orderRepository = new InMemoryOrderRepository();

    AuthService authService = new AuthService(accountRepository);
    UserService userService = new UserService(accountRepository);
    ReviewService reviewService =
        new ReviewService(accountRepository, productRepository, orderRepository);
    ProductService productService =
        new ProductService(productRepository, accountRepository, reviewService, orderRepository);
    InventoryService inventoryService =
        new InventoryService(productRepository, accountRepository);
    CartService cartService = new CartService(accountRepository, productRepository);
    OrderService orderService =
        new OrderService(orderRepository, cartService, productRepository, accountRepository);
    PaymentService paymentService = new PaymentService(orderRepository);

    seedSampleData(accountRepository, productRepository, productService);

    ConsoleReader reader = new ConsoleReader(System.in);
    AuthCLI authCli = new AuthCLI(authService, userService, reader);
    ProductCLI productCli =
        new ProductCLI(productService, inventoryService, reviewService, reader);
    CartOrderCLI cartOrderCli =
        new CartOrderCLI(cartService, orderService, paymentService, userService, reader);
    mainCli = new MainCLI(authCli, productCli, cartOrderCli, reader);
  }

  private static void seedSampleData(
      AccountRepository accountRepository,
      ProductRepository productRepository,
      ProductService productService) {
    Account admin =
        new Account(
            Role.ADMIN,
            new Email(ADMIN_EMAIL),
            new PhoneNumber(ADMIN_PHONE),
            PasswordHash.fromRaw(ADMIN_PASSWORD),
            ADMIN_NAME);
    accountRepository.save(admin);

    Category books = new Category("Books");
    Category electronics = new Category("Electronics");
    productRepository.saveCategory(books);
    productRepository.saveCategory(electronics);

    createSampleProduct(productService, admin, books, "Clean Code", "35.00", 10);
    createSampleProduct(productService, admin, books, "Effective Java", "42.50", 5);
    createSampleProduct(productService, admin, electronics, "Wireless Mouse", "18.99", 25);
    createSampleProduct(productService, admin, electronics, "USB-C Cable", "7.50", 100);
  }

  private static void createSampleProduct(
      ProductService productService,
      Account admin,
      Category category,
      String name,
      String price,
      int stock) {
    productService.createProduct(
        admin.getId(),
        name,
        new Money(new BigDecimal(price), Money.DEFAULT_CURRENCY),
        new StockQuantity(stock),
        category.getId());
  }
}