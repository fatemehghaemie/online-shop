package org.example.service;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.example.model.Category;
import org.example.model.Money;
import org.example.model.Order;
import org.example.model.OrderLine;
import org.example.model.OrderStatus;
import org.example.model.Product;
import org.example.model.ProductFilter;
import org.example.model.ProductSortOption;
import org.example.model.StockQuantity;

/** Use cases for browsing, searching, sorting and managing products. */
public final class ProductService {

  private static final String NO_DESCRIPTION = "";
  private static final double NO_RATING = 0.0;

  private final ProductRepository productRepository;
  private final AccountRepository accountRepository;
  private final ReviewService reviewService;
  private final OrderRepository orderRepository;

  /**
   * Creates the service.
   *
   * @param productRepository where the catalog is stored; must not be null
   * @param accountRepository where accounts are stored, used to check administrators; must not
   *     be null
   * @param reviewService provides the reviews behind ratings; must not be null
   * @param orderRepository where orders are stored, used to count sales; must not be null
   */
  public ProductService(
      ProductRepository productRepository,
      AccountRepository accountRepository,
      ReviewService reviewService,
      OrderRepository orderRepository) {
    this.productRepository =
        Objects.requireNonNull(productRepository, "productRepository must not be null");
    this.accountRepository =
        Objects.requireNonNull(accountRepository, "accountRepository must not be null");
    this.reviewService = Objects.requireNonNull(reviewService, "reviewService must not be null");
    this.orderRepository =
        Objects.requireNonNull(orderRepository, "orderRepository must not be null");
  }

  /**
   * Lists the products that customers can see.
   *
   * <p>MODIFIES: nothing.
   *
   * <p>EFFECTS: Returns the active products in the order they were created. The list is
   * unmodifiable.
   *
   * @return all active products
   */
  public List<Product> getActiveProducts() {
    return productRepository.findAll().stream().filter(Product::isActive).toList();
  }

  /**
   * Finds active products by a keyword.
   *
   * <p>REQUIRES: keyword is non-null.
   *
   * <p>MODIFIES: nothing.
   *
   * <p>EFFECTS: Returns the active products whose name or description contains the keyword,
   * ignoring case and surrounding spaces. A blank keyword matches every active product.
   *
   * @param keyword the text to look for
   * @return the matching products, possibly empty
   */
  public List<Product> searchProducts(String keyword) {
    Objects.requireNonNull(keyword, "keyword must not be null");
    String normalizedKeyword = keyword.trim().toLowerCase(Locale.ROOT);
    return getActiveProducts().stream()
        .filter(product -> matches(product, normalizedKeyword))
        .toList();
  }

  /**
   * Lists the categories that customers can see.
   *
   * <p>MODIFIES: nothing.
   *
   * <p>EFFECTS: Returns the active categories in the order they were created. The list is
   * unmodifiable.
   *
   * @return all active categories
   */
  public List<Category> getActiveCategories() {
    return productRepository.findAllCategories().stream().filter(Category::isActive).toList();
  }

  /**
   * Lists the active products of one category.
   *
   * <p>REQUIRES: categoryId belongs to an existing, active category.
   *
   * <p>MODIFIES: nothing.
   *
   * <p>EFFECTS: Returns the active products of the category in the order they were created.
   * The list is unmodifiable.
   *
   * @param categoryId the category to list; must not be null
   * @return the active products of the category, possibly empty
   * @throws IllegalArgumentException if the category does not exist or is not active
   */
  public List<Product> getProductsByCategory(UUID categoryId) {
    Objects.requireNonNull(categoryId, "categoryId must not be null");
    Category category =
        productRepository
            .findCategoryById(categoryId)
            .orElseThrow(() -> new IllegalArgumentException("Category not found: " + categoryId));
    if (!category.isActive()) {
      throw new IllegalArgumentException("Category is not active: " + category.getName());
    }
    return getActiveProducts().stream()
        .filter(product -> product.getCategoryId().equals(categoryId))
        .toList();
  }

  /**
   * Lists the active products that meet every condition of a filter.
   *
   * <p>REQUIRES: filter is non-null.
   *
   * <p>MODIFIES: nothing.
   *
   * <p>EFFECTS: Returns the matching active products in the order they were created. The
   * rating condition compares the average of a product's review scores; a product without
   * reviews never meets it.
   *
   * @param filter the conditions to apply
   * @return the matching products, possibly empty
   */
  public List<Product> filterProducts(ProductFilter filter) {
    Objects.requireNonNull(filter, "filter must not be null");
    return getActiveProducts().stream()
        .filter(product -> matchesFilter(filter, product))
        .toList();
  }

  /**
   * Orders products.
   *
   * <p>REQUIRES: products and option are non-null and every product is stored.
   *
   * <p>MODIFIES: nothing; the given list is not changed.
   *
   * <p>EFFECTS: Returns a new unmodifiable list with the same products. CHEAPEST and
   * MOST_EXPENSIVE order by price, NEWEST by creation time (latest first), MOST_POPULAR by
   * units sold in orders that are not cancelled (most first) and HIGHEST_RATING by average
   * review score (highest first, products without reviews last). Products that tie keep their
   * order from the given list.
   *
   * @param products the products to order
   * @param option how to order them
   * @return the ordered products
   */
  public List<Product> sortProducts(List<Product> products, ProductSortOption option) {
    Objects.requireNonNull(products, "products must not be null");
    Objects.requireNonNull(option, "option must not be null");
    return products.stream().sorted(comparatorFor(option)).toList();
  }

  /**
   * Creates an active product with an empty description.
   *
   * <p>REQUIRES: adminId belongs to an administrator; name is non-blank; price is in the
   * default currency; stock is non-null; catId belongs to an existing category.
   *
   * <p>MODIFIES: the product repository.
   *
   * <p>EFFECTS: Stores and returns the new product. If a requirement is not met, an exception
   * is thrown and nothing is stored.
   *
   * @param adminId the administrator who creates the product; must not be null
   * @param name the product name
   * @param price the price of one item
   * @param stock the initial stock
   * @param catId the category of the product
   * @return the created product
   * @throws SecurityException if adminId is not an administrator
   * @throws IllegalArgumentException if another requirement is not met
   */
  public Product createProduct(
      UUID adminId, String name, Money price, StockQuantity stock, UUID catId) {
    accountRepository.getAdminById(adminId);
    Objects.requireNonNull(price, "price must not be null");
    Objects.requireNonNull(stock, "stock must not be null");
    Objects.requireNonNull(catId, "catId must not be null");
    requireDefaultCurrency(price);
    if (productRepository.findCategoryById(catId).isEmpty()) {
      throw new IllegalArgumentException("Category not found: " + catId);
    }
    Product product = new Product(name, NO_DESCRIPTION, price, stock, catId);
    productRepository.save(product);
    return product;
  }

  /**
   * Changes the name, description and price of a product.
   *
   * <p>REQUIRES: adminId belongs to an administrator, prodId belongs to an existing product,
   * name is non-blank, desc is non-null and price is non-null and in the default currency.
   *
   * <p>MODIFIES: the product and the product repository.
   *
   * <p>EFFECTS: Stores and returns the updated product. If a requirement is not met, an
   * exception is thrown and the product is unchanged.
   *
   * @param adminId the administrator who changes the product; must not be null
   * @param prodId the product to change; must not be null
   * @param name the new name
   * @param desc the new description
   * @param price the new price
   * @return the updated product
   * @throws SecurityException if adminId is not an administrator
   * @throws IllegalArgumentException if another requirement is not met
   */
  public Product updateProduct(
      UUID adminId, UUID prodId, String name, String desc, Money price) {
    accountRepository.getAdminById(adminId);
    Product product = productRepository.getById(prodId);
    Objects.requireNonNull(price, "price must not be null");
    requireDefaultCurrency(price);
    product.updateInfo(name, desc, price);
    productRepository.save(product);
    return product;
  }

  /**
   * Withdraws a product from sale.
   *
   * <p>REQUIRES: adminId belongs to an administrator and prodId belongs to an existing
   * product.
   *
   * <p>MODIFIES: the product and the product repository.
   *
   * <p>EFFECTS: After the call the product is not active, so it is hidden from the catalog and
   * cannot be bought. Withdrawing an inactive product changes nothing.
   *
   * @param adminId the administrator who withdraws the product; must not be null
   * @param prodId the product to withdraw; must not be null
   * @throws SecurityException if adminId is not an administrator
   * @throws IllegalArgumentException if the product does not exist
   */
  public void deactivateProduct(UUID adminId, UUID prodId) {
    accountRepository.getAdminById(adminId);
    Product product = productRepository.getById(prodId);
    product.deactivate();
    productRepository.save(product);
  }

  private Comparator<Product> comparatorFor(ProductSortOption option) {
    return switch (option) {
      case CHEAPEST -> byPrice();
      case MOST_EXPENSIVE -> byPrice().reversed();
      case NEWEST -> Comparator.comparing(Product::getCreatedAt).reversed();
      case MOST_POPULAR -> byUnitsSold(unitsSoldByProduct());
      case HIGHEST_RATING -> byAverageRating();
    };
  }

  private static Comparator<Product> byPrice() {
    return Comparator.comparing((Product product) -> product.getPrice().getAmount());
  }

  private static Comparator<Product> byUnitsSold(Map<UUID, Integer> unitsSold) {
    return Comparator.comparingInt(
            (Product product) -> unitsSold.getOrDefault(product.getId(), 0))
        .reversed();
  }

  private Comparator<Product> byAverageRating() {
    return Comparator.comparingDouble((Product product) -> averageRating(product)).reversed();
  }

  private Map<UUID, Integer> unitsSoldByProduct() {
    Map<UUID, Integer> unitsSold = new HashMap<>();
    for (Order order : orderRepository.findAll()) {
      if (order.getStatus() == OrderStatus.CANCELLED) {
        continue;
      }
      for (OrderLine line : order.getLines()) {
        unitsSold.merge(line.getProductId(), line.getQuantity().getValue(), Integer::sum);
      }
    }
    return unitsSold;
  }

  private double averageRating(Product product) {
    return reviewService.getProductReviews(product.getId()).stream()
        .mapToInt(review -> review.getRating().getScore())
        .average()
        .orElse(NO_RATING);
  }

  private boolean matchesFilter(ProductFilter filter, Product product) {
    boolean inCategory =
        filter.getCategoryId().map(id -> id.equals(product.getCategoryId())).orElse(true);
    boolean aboveMinPrice =
        filter.getMinPrice().map(min -> isAtLeast(product.getPrice(), min)).orElse(true);
    boolean belowMaxPrice =
        filter.getMaxPrice().map(max -> isAtLeast(max, product.getPrice())).orElse(true);
    boolean wellRated =
        filter.getMinRating().map(min -> averageRating(product) >= min.getScore()).orElse(true);
    return inCategory && aboveMinPrice && belowMaxPrice && wellRated;
  }

  private static boolean isAtLeast(Money amount, Money limit) {
    return amount.getAmount().compareTo(limit.getAmount()) >= 0;
  }

  private static void requireDefaultCurrency(Money price) {
    if (!Money.DEFAULT_CURRENCY.equals(price.getCurrency())) {
      throw new IllegalArgumentException(
          "Price must be in " + Money.DEFAULT_CURRENCY + ": " + price);
    }
  }

  private static boolean matches(Product product, String normalizedKeyword) {
    return product.getName().toLowerCase(Locale.ROOT).contains(normalizedKeyword)
        || product.getDescription().toLowerCase(Locale.ROOT).contains(normalizedKeyword);
  }
}