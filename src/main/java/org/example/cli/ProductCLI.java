package org.example.cli;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import org.example.model.Account;
import org.example.model.Category;
import org.example.model.Money;
import org.example.model.Product;
import org.example.model.ProductFilter;
import org.example.model.ProductSortOption;
import org.example.model.Rating;
import org.example.model.Review;
import org.example.model.StockQuantity;
import org.example.service.InventoryService;
import org.example.service.ProductService;
import org.example.service.ReviewService;

/** Console screens for the catalog, reviews and the product administration of an admin. */
public final class ProductCLI {

  private static final int CATALOG_BACK = 0;
  private static final int CATALOG_BROWSE = 1;
  private static final int CATALOG_SEARCH = 2;
  private static final int CATALOG_CATEGORIES = 3;
  private static final int CATALOG_FILTER = 4;
  private static final int CATALOG_SORT = 5;
  private static final int CATALOG_REVIEW = 6;

  private static final int ADMIN_BACK = 0;
  private static final int ADMIN_CREATE = 1;
  private static final int ADMIN_UPDATE = 2;
  private static final int ADMIN_WITHDRAW = 3;
  private static final int ADMIN_INVENTORY = 4;
  private static final int ADMIN_ADJUST_STOCK = 5;

  private static final String ADMIN_REQUIRED_MESSAGE = "Administrator privileges are required.";
  private static final String NO_PRODUCTS_MESSAGE = "No products found.";

  private final ProductService productService;
  private final InventoryService inventoryService;
  private final ReviewService reviewService;
  private final ConsoleReader reader;

  /**
   * Creates the screens.
   *
   * @param productService lists, searches and manages products; must not be null
   * @param inventoryService reads and changes stock; must not be null
   * @param reviewService records and lists reviews; must not be null
   * @param reader reads the user's input; must not be null
   */
  public ProductCLI(
      ProductService productService,
      InventoryService inventoryService,
      ReviewService reviewService,
      ConsoleReader reader) {
    this.productService = Objects.requireNonNull(productService, "productService is null");
    this.inventoryService = Objects.requireNonNull(inventoryService, "inventoryService is null");
    this.reviewService = Objects.requireNonNull(reviewService, "reviewService is null");
    this.reader = Objects.requireNonNull(reader, "reader must not be null");
  }

  /**
   * Prints a numbered list of products.
   *
   * <p>REQUIRES: products is non-null.
   *
   * <p>MODIFIES: the console.
   *
   * @param products the products to show
   */
  public void displayCatalog(List<Product> products) {
    if (products.isEmpty()) {
      System.out.println(NO_PRODUCTS_MESSAGE);
      return;
    }
    System.out.println();
    for (int i = 0; i < products.size(); i++) {
      Product product = products.get(i);
      System.out.println(
          (i + 1)
              + ". "
              + product.getName()
              + " | "
              + product.getPrice()
              + " | in stock: "
              + product.getStock().getValue());
    }
  }

  /**
   * Prints the details of a product, including its reviews.
   *
   * <p>REQUIRES: prod is a stored product.
   *
   * <p>MODIFIES: the console.
   *
   * @param prod the product to show
   */
  public void showProductDetails(Product prod) {
    System.out.println();
    System.out.println("--- " + prod.getName() + " ---");
    if (!prod.getDescription().isBlank()) {
      System.out.println(prod.getDescription());
    }
    System.out.println("Price: " + prod.getPrice());
    System.out.println("In stock: " + prod.getStock().getValue());
    showReviews(prod);
  }

  /**
   * Lists the active products and lets the user open one.
   *
   * <p>MODIFIES: the console.
   */
  public void browseCatalog() {
    selectProduct().ifPresent(this::showProductDetails);
  }

  /**
   * Lists the active products and lets the user choose one.
   *
   * <p>MODIFIES: the console.
   *
   * @return the chosen product, or empty if the user cancels or nothing is available
   */
  public Optional<Product> selectProduct() {
    List<Product> products = productService.getActiveProducts();
    displayCatalog(products);
    return reader.select("Product number", products);
  }

  /**
   * Lists the active categories, lets the user choose one and shows its active products.
   *
   * <p>MODIFIES: the console.
   */
  public void browseCategories() {
    List<Category> categories = productService.getActiveCategories();
    displayCategories(categories);
    reader.select("Category number", categories).ifPresent(this::showCategoryProducts);
  }

  /**
   * Asks for optional conditions and lists the active products that meet all of them.
   *
   * <p>MODIFIES: the console and the input stream.
   *
   * <p>EFFECTS: A condition left empty is skipped. Invalid conditions, such as a minimum price
   * above the maximum price, are reported and nothing is listed.
   */
  public void filterCatalog() {
    try {
      listAndOpen(productService.filterProducts(readFilter()));
    } catch (IllegalArgumentException e) {
      System.out.println("Invalid filter: " + e.getMessage());
    }
  }

  /**
   * Lets the user choose how to order the active products and shows them in that order.
   *
   * <p>MODIFIES: the console.
   */
  public void sortCatalog() {
    List<ProductSortOption> options = List.of(ProductSortOption.values());
    for (int i = 0; i < options.size(); i++) {
      System.out.println((i + 1) + ". " + describe(options.get(i)));
    }
    reader.select("Sort by", options).ifPresent(this::listSorted);
  }

  /**
   * Shows the catalog menu of a logged-in account until the user goes back.
   *
   * <p>REQUIRES: account is non-null.
   *
   * <p>MODIFIES: the console and, through the services, reviews.
   *
   * @param account the logged-in account
   */
  public void openCatalogMenu(Account account) {
    Objects.requireNonNull(account, "account must not be null");
    boolean inMenu = true;
    while (inMenu) {
      printCatalogMenu();
      inMenu = handleCatalogChoice(reader.readInt("Choice"), account);
    }
  }

  /**
   * Shows the product administration menu until the user goes back.
   *
   * <p>REQUIRES: admin is non-null.
   *
   * <p>MODIFIES: the console and, through the services, products and stock.
   *
   * <p>EFFECTS: Prints a message and returns at once if the account is not an administrator.
   *
   * @param admin the logged-in administrator
   */
  public void openAdminMenu(Account admin) {
    Objects.requireNonNull(admin, "admin must not be null");
    if (!admin.isAdmin()) {
      System.out.println(ADMIN_REQUIRED_MESSAGE);
      return;
    }
    boolean inMenu = true;
    while (inMenu) {
      printAdminMenu();
      inMenu = handleAdminChoice(reader.readInt("Choice"), admin);
    }
  }

  private boolean handleCatalogChoice(int choice, Account account) {
    if (choice == CATALOG_BACK) {
      return false;
    }
    switch (choice) {
      case CATALOG_BROWSE -> browseCatalog();
      case CATALOG_SEARCH -> searchCatalog();
      case CATALOG_CATEGORIES -> browseCategories();
      case CATALOG_FILTER -> filterCatalog();
      case CATALOG_SORT -> sortCatalog();
      case CATALOG_REVIEW ->
          selectProduct().ifPresent(product -> submitReview(account, product));
      default -> System.out.println(ConsoleReader.INVALID_CHOICE_MESSAGE);
    }
    return true;
  }

  private boolean handleAdminChoice(int choice, Account admin) {
    if (choice == ADMIN_BACK) {
      return false;
    }
    switch (choice) {
      case ADMIN_CREATE -> chooseCategoryForNewProduct(admin);
      case ADMIN_UPDATE ->
          selectProduct().ifPresent(product -> updateProduct(admin, product));
      case ADMIN_WITHDRAW ->
          selectProduct().ifPresent(product -> withdrawProduct(admin, product));
      case ADMIN_INVENTORY -> showInventory();
      case ADMIN_ADJUST_STOCK ->
          selectProduct().ifPresent(product -> changeStock(admin, product));
      default -> System.out.println(ConsoleReader.INVALID_CHOICE_MESSAGE);
    }
    return true;
  }

  private void searchCatalog() {
    String keyword = reader.readLine("Search keyword");
    listAndOpen(productService.searchProducts(keyword));
  }

  private void listAndOpen(List<Product> products) {
    displayCatalog(products);
    reader.select("Product number", products).ifPresent(this::showProductDetails);
  }

  private void listSorted(ProductSortOption option) {
    listAndOpen(productService.sortProducts(productService.getActiveProducts(), option));
  }

  private void showCategoryProducts(Category category) {
    try {
      listAndOpen(productService.getProductsByCategory(category.getId()));
    } catch (IllegalArgumentException e) {
      System.out.println("Could not list the category: " + e.getMessage());
    }
  }

  private void displayCategories(List<Category> categories) {
    if (categories.isEmpty()) {
      System.out.println("No categories found.");
      return;
    }
    System.out.println();
    for (int i = 0; i < categories.size(); i++) {
      System.out.println((i + 1) + ". " + categories.get(i).getName());
    }
  }

  private ProductFilter readFilter() {
    System.out.println("Leave a condition empty to skip it.");
    ProductFilter filter = ProductFilter.none();
    List<Category> categories = productService.getActiveCategories();
    displayCategories(categories);
    Optional<Category> category = reader.select("Category", categories);
    if (category.isPresent()) {
      filter = filter.withCategory(category.get().getId());
    }
    Optional<BigDecimal> minPrice = reader.readOptionalAmount("Minimum price");
    if (minPrice.isPresent()) {
      filter = filter.withMinPrice(toMoney(minPrice.get()));
    }
    Optional<BigDecimal> maxPrice = reader.readOptionalAmount("Maximum price");
    if (maxPrice.isPresent()) {
      filter = filter.withMaxPrice(toMoney(maxPrice.get()));
    }
    OptionalInt minRating =
        reader.readOptionalInt(
            "Minimum rating (" + Rating.MIN_SCORE + "-" + Rating.MAX_SCORE + ")");
    if (minRating.isPresent()) {
      filter = filter.withMinRating(new Rating(minRating.getAsInt()));
    }
    return filter;
  }

  private void submitReview(Account account, Product product) {
    int rating = reader.readInt("Rating (" + Rating.MIN_SCORE + "-" + Rating.MAX_SCORE + ")");
    String comment = reader.readLine("Comment");
    try {
      reviewService.submitReview(account.getId(), product.getId(), rating, comment);
      System.out.println("Thank you for your review.");
    } catch (IllegalArgumentException e) {
      System.out.println("Review failed: " + e.getMessage());
    }
  }

  private void chooseCategoryForNewProduct(Account admin) {
    List<Category> categories = productService.getActiveCategories();
    displayCategories(categories);
    reader
        .select("Category number", categories)
        .ifPresent(category -> readAndCreateProduct(admin, category));
  }

  private void readAndCreateProduct(Account admin, Category category) {
    String name = reader.readLine("Product name");
    BigDecimal price = reader.readAmount("Price (" + Money.DEFAULT_CURRENCY + ")");
    int stock = reader.readInt("Initial stock");
    try {
      Product product =
          productService.createProduct(
              admin.getId(), name, toMoney(price), new StockQuantity(stock), category.getId());
      System.out.println("Created product: " + product.getName());
    } catch (IllegalArgumentException | SecurityException e) {
      System.out.println("Could not create the product: " + e.getMessage());
    }
  }

  private void updateProduct(Account admin, Product product) {
    System.out.println("Leave the name or description empty to keep the current value.");
    String name = keepIfBlank(reader.readLine("New name"), product.getName());
    String description =
        keepIfBlank(reader.readLine("New description"), product.getDescription());
    try {
      Money price =
          reader
              .readOptionalAmount("New price (" + Money.DEFAULT_CURRENCY + ")")
              .map(ProductCLI::toMoney)
              .orElse(product.getPrice());
      productService.updateProduct(admin.getId(), product.getId(), name, description, price);
      System.out.println("Product updated.");
    } catch (IllegalArgumentException | SecurityException e) {
      System.out.println("Could not update the product: " + e.getMessage());
    }
  }

  private void withdrawProduct(Account admin, Product product) {
    if (!reader.confirm("Withdraw " + product.getName() + " from sale?")) {
      return;
    }
    try {
      productService.deactivateProduct(admin.getId(), product.getId());
      System.out.println(product.getName() + " is no longer for sale.");
    } catch (IllegalArgumentException | SecurityException e) {
      System.out.println("Could not withdraw the product: " + e.getMessage());
    }
  }

  private void changeStock(Account admin, Product product) {
    int delta = reader.readInt("Stock change (negative to remove)");
    try {
      inventoryService.adjustStock(admin.getId(), product.getId(), delta);
      int stock = inventoryService.getStock(product.getId()).getValue();
      System.out.println("New stock of " + product.getName() + ": " + stock);
    } catch (IllegalArgumentException | SecurityException e) {
      System.out.println("Stock change failed: " + e.getMessage());
    }
  }

  private void showInventory() {
    List<Product> products = productService.getActiveProducts();
    if (products.isEmpty()) {
      System.out.println(NO_PRODUCTS_MESSAGE);
      return;
    }
    System.out.println();
    System.out.println("--- Inventory ---");
    for (Product product : products) {
      int stock = inventoryService.getStock(product.getId()).getValue();
      System.out.println(product.getName() + ": " + stock);
    }
  }

  private void showReviews(Product product) {
    List<Review> reviews = reviewService.getProductReviews(product.getId());
    if (reviews.isEmpty()) {
      System.out.println("No reviews yet.");
      return;
    }
    double average =
        reviews.stream().mapToInt(review -> review.getRating().getScore()).average().orElse(0);
    System.out.println(
        String.format(
            Locale.ROOT,
            "Rating: %.1f / %d (%d reviews)",
            average,
            Rating.MAX_SCORE,
            reviews.size()));
    for (Review review : reviews) {
      System.out.println(
          "- " + review.getRating().getScore() + "/" + Rating.MAX_SCORE + ": " + review.getText());
    }
  }

  private void printCatalogMenu() {
    System.out.println();
    System.out.println("--- Catalog ---");
    System.out.println(CATALOG_BROWSE + ". Browse products");
    System.out.println(CATALOG_SEARCH + ". Search products");
    System.out.println(CATALOG_CATEGORIES + ". Browse categories");
    System.out.println(CATALOG_FILTER + ". Filter products");
    System.out.println(CATALOG_SORT + ". Sort products");
    System.out.println(CATALOG_REVIEW + ". Write a review");
    System.out.println(CATALOG_BACK + ". Back");
  }

  private void printAdminMenu() {
    System.out.println();
    System.out.println("--- Product administration ---");
    System.out.println(ADMIN_CREATE + ". Create a product");
    System.out.println(ADMIN_UPDATE + ". Update a product");
    System.out.println(ADMIN_WITHDRAW + ". Withdraw a product from sale");
    System.out.println(ADMIN_INVENTORY + ". View inventory");
    System.out.println(ADMIN_ADJUST_STOCK + ". Change stock");
    System.out.println(ADMIN_BACK + ". Back");
  }

  private static String describe(ProductSortOption option) {
    return switch (option) {
      case CHEAPEST -> "Cheapest first";
      case MOST_EXPENSIVE -> "Most expensive first";
      case NEWEST -> "Newest first";
      case MOST_POPULAR -> "Most popular first";
      case HIGHEST_RATING -> "Highest rating first";
    };
  }

  private static Money toMoney(BigDecimal amount) {
    return new Money(amount, Money.DEFAULT_CURRENCY);
  }

  private static String keepIfBlank(String input, String current) {
    return input.isBlank() ? current : input;
  }
}