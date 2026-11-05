package org.example.model;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/** Immutable set of optional conditions that a product must meet to be listed. */
public final class ProductFilter {

  // AF: a product matches if it meets every condition that is present; an absent condition
  //     accepts every product.
  // RI: if both prices are present, they use the same currency and minPrice <= maxPrice. A
  //     null field means the condition is absent; null never leaves this class.
  private final UUID categoryId;
  private final Money minPrice;
  private final Money maxPrice;
  private final Rating minRating;

  private ProductFilter(UUID categoryId, Money minPrice, Money maxPrice, Rating minRating) {
    requireOrderedPrices(minPrice, maxPrice);
    this.categoryId = categoryId;
    this.minPrice = minPrice;
    this.maxPrice = maxPrice;
    this.minRating = minRating;
  }

  /**
   * Creates a filter without conditions.
   *
   * <p>EFFECTS: The returned filter accepts every product.
   *
   * @return the empty filter
   */
  public static ProductFilter none() {
    return new ProductFilter(null, null, null, null);
  }

  /**
   * Adds a category condition.
   *
   * <p>REQUIRES: categoryId is non-null.
   *
   * <p>EFFECTS: Returns a new filter like this one that also requires the category; this
   * filter is unchanged.
   *
   * @param categoryId the required category
   * @return the new filter
   */
  public ProductFilter withCategory(UUID categoryId) {
    Objects.requireNonNull(categoryId, "categoryId must not be null");
    return new ProductFilter(categoryId, minPrice, maxPrice, minRating);
  }

  /**
   * Adds a minimum price condition.
   *
   * <p>REQUIRES: minPrice is non-null and does not exceed an existing maximum price.
   *
   * <p>EFFECTS: Returns a new filter like this one that also requires price >= minPrice; this
   * filter is unchanged.
   *
   * @param minPrice the lowest accepted price
   * @return the new filter
   * @throws IllegalArgumentException if the price range would be empty
   */
  public ProductFilter withMinPrice(Money minPrice) {
    Objects.requireNonNull(minPrice, "minPrice must not be null");
    return new ProductFilter(categoryId, minPrice, maxPrice, minRating);
  }

  /**
   * Adds a maximum price condition.
   *
   * <p>REQUIRES: maxPrice is non-null and is not below an existing minimum price.
   *
   * <p>EFFECTS: Returns a new filter like this one that also requires price <= maxPrice; this
   * filter is unchanged.
   *
   * @param maxPrice the highest accepted price
   * @return the new filter
   * @throws IllegalArgumentException if the price range would be empty
   */
  public ProductFilter withMaxPrice(Money maxPrice) {
    Objects.requireNonNull(maxPrice, "maxPrice must not be null");
    return new ProductFilter(categoryId, minPrice, maxPrice, minRating);
  }

  /**
   * Adds a minimum average rating condition.
   *
   * <p>REQUIRES: minRating is non-null.
   *
   * <p>EFFECTS: Returns a new filter like this one that also requires an average rating of at
   * least minRating; products without reviews never match. This filter is unchanged.
   *
   * @param minRating the lowest accepted average rating
   * @return the new filter
   */
  public ProductFilter withMinRating(Rating minRating) {
    Objects.requireNonNull(minRating, "minRating must not be null");
    return new ProductFilter(categoryId, minPrice, maxPrice, minRating);
  }

  /** Returns the required category, if the condition is present. */
  public Optional<UUID> getCategoryId() {
    return Optional.ofNullable(categoryId);
  }

  /** Returns the lowest accepted price, if the condition is present. */
  public Optional<Money> getMinPrice() {
    return Optional.ofNullable(minPrice);
  }

  /** Returns the highest accepted price, if the condition is present. */
  public Optional<Money> getMaxPrice() {
    return Optional.ofNullable(maxPrice);
  }

  /** Returns the lowest accepted average rating, if the condition is present. */
  public Optional<Rating> getMinRating() {
    return Optional.ofNullable(minRating);
  }

  private static void requireOrderedPrices(Money minPrice, Money maxPrice) {
    if (minPrice == null || maxPrice == null) {
      return;
    }
    if (!minPrice.getCurrency().equals(maxPrice.getCurrency())) {
      throw new IllegalArgumentException("Price limits must use the same currency");
    }
    if (minPrice.getAmount().compareTo(maxPrice.getAmount()) > 0) {
      throw new IllegalArgumentException("Minimum price must not exceed maximum price");
    }
  }
}