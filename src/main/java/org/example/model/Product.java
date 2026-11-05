package org.example.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** Item offered for sale, with its price and stock. */
public final class Product {

  // AF: a sellable item identified by id, created at createdAt, belonging to one category,
  //     with a price and a stock.
  // RI: no field is null and name is non-blank; stock is never negative (StockQuantity).
  private final UUID id;
  private String name;
  private String description;
  private Money price;
  private StockQuantity stock;
  private final UUID categoryId;
  private final Instant createdAt;
  private boolean active;

  /**
   * Creates an active product with a new identifier, created now.
   *
   * @param name the product name; must not be null or blank
   * @param description the product description; must not be null
   * @param price the price of one item; must not be null
   * @param stock the initial stock; must not be null
   * @param categoryId the owning category; must not be null
   * @throws IllegalArgumentException if the name is blank
   */
  public Product(
      String name, String description, Money price, StockQuantity stock, UUID categoryId) {
    this.id = UUID.randomUUID();
    this.name = requireName(name);
    this.description = Objects.requireNonNull(description, "description must not be null");
    this.price = Objects.requireNonNull(price, "price must not be null");
    this.stock = Objects.requireNonNull(stock, "stock must not be null");
    this.categoryId = Objects.requireNonNull(categoryId, "categoryId must not be null");
    this.createdAt = Instant.now();
    this.active = true;
  }

  /**
   * Replaces the name, description and price.
   *
   * <p>REQUIRES: name is non-blank; desc and price are non-null.
   *
   * <p>MODIFIES: this product.
   *
   * <p>EFFECTS: On success all three values are replaced. If an argument is invalid, an
   * exception is thrown and the product is unchanged.
   *
   * @param name the new name
   * @param desc the new description
   * @param price the new price
   * @throws IllegalArgumentException if the name is blank
   */
  public void updateInfo(String name, String desc, Money price) {
    String validName = requireName(name);
    String validDescription = Objects.requireNonNull(desc, "desc must not be null");
    Money validPrice = Objects.requireNonNull(price, "price must not be null");
    this.name = validName;
    this.description = validDescription;
    this.price = validPrice;
  }

  /**
   * Changes the stock by a signed amount.
   *
   * <p>REQUIRES: current stock plus delta is not negative.
   *
   * <p>MODIFIES: this product.
   *
   * <p>EFFECTS: The stock becomes its previous value plus delta. If the result would be
   * negative, an exception is thrown and the stock is unchanged.
   *
   * @param delta the change; positive to add stock, negative to remove it
   * @throws IllegalArgumentException if the stock would become negative
   */
  public void adjustStock(int delta) {
    int newValue = Math.addExact(stock.getValue(), delta);
    if (newValue < 0) {
      throw new IllegalArgumentException(
          "Insufficient stock: have " + stock.getValue() + ", change " + delta);
    }
    stock = new StockQuantity(newValue);
  }

  /**
   * Checks whether the requested quantity can be bought now.
   *
   * <p>EFFECTS: Returns true if the product is active and the stock covers the quantity; does
   * not modify the product.
   *
   * @param qty the requested quantity; must not be null
   * @return true if the purchase is possible
   */
  public boolean canBePurchased(Quantity qty) {
    return active && stock.isAvailable(qty);
  }

  /**
   * Makes the product visible in the catalog.
   *
   * <p>MODIFIES: this product.
   */
  public void activate() {
    active = true;
  }

  /**
   * Hides the product from the catalog.
   *
   * <p>MODIFIES: this product.
   */
  public void deactivate() {
    active = false;
  }

  /** Returns the unique identifier. */
  public UUID getId() {
    return id;
  }

  /** Returns the product name. */
  public String getName() {
    return name;
  }

  /** Returns the product description. */
  public String getDescription() {
    return description;
  }

  /** Returns the price of one item. */
  public Money getPrice() {
    return price;
  }

  /** Returns the current stock. */
  public StockQuantity getStock() {
    return stock;
  }

  /** Returns the identifier of the owning category. */
  public UUID getCategoryId() {
    return categoryId;
  }

  /** Returns the moment the product was created. */
  public Instant getCreatedAt() {
    return createdAt;
  }

  /** Returns true if the product is visible in the catalog. */
  public boolean isActive() {
    return active;
  }

  private static String requireName(String name) {
    if (name == null || name.isBlank()) {
      throw new IllegalArgumentException("Product name must not be blank");
    }
    return name.trim();
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof Product that)) {
      return false;
    }
    return id.equals(that.id);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }
}