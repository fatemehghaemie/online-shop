package org.example.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import org.example.model.OrderStatus;
import org.example.model.Rating;
import org.example.model.Review;

/** Use cases for rating products and reading their reviews. */
public final class ReviewService {

  private static final Set<OrderStatus> PURCHASED_STATUSES =
      Set.of(
          OrderStatus.PAID, OrderStatus.PROCESSING, OrderStatus.SHIPPED, OrderStatus.DELIVERED);
  private static final String NOT_PURCHASED_MESSAGE =
      "Only customers who bought this product can review it";

  // AF: the submitted reviews, oldest first.
  // RI: no element is null.
  private final List<Review> reviews = new ArrayList<>();
  private final AccountRepository accountRepository;
  private final ProductRepository productRepository;
  private final OrderRepository orderRepository;

  /**
   * Creates the service.
   *
   * @param accountRepository where accounts are stored; must not be null
   * @param productRepository where the catalog is stored; must not be null
   * @param orderRepository where orders are stored, used to check purchases; must not be null
   */
  public ReviewService(
      AccountRepository accountRepository,
      ProductRepository productRepository,
      OrderRepository orderRepository) {
    this.accountRepository =
        Objects.requireNonNull(accountRepository, "accountRepository must not be null");
    this.productRepository =
        Objects.requireNonNull(productRepository, "productRepository must not be null");
    this.orderRepository =
        Objects.requireNonNull(orderRepository, "orderRepository must not be null");
  }

  /**
   * Records a review of a product.
   *
   * <p>REQUIRES: the account and the product exist, the account has a paid order (status PAID,
   * PROCESSING, SHIPPED or DELIVERED) that contains the product, rating is between the minimum
   * and the maximum score of {@link Rating}, and comment is non-null (it may be empty).
   *
   * <p>MODIFIES: the submitted reviews.
   *
   * <p>EFFECTS: Records and returns the new review. If a requirement is not met, an exception
   * is thrown and nothing is recorded.
   *
   * @param accountId the account that writes the review; must not be null
   * @param prodId the product that is reviewed; must not be null
   * @param rating the score
   * @param comment the text of the review
   * @return the recorded review
   * @throws IllegalArgumentException if the account or product does not exist, the product was
   *     not bought by the account or the rating is out of range
   */
  public Review submitReview(UUID accountId, UUID prodId, int rating, String comment) {
    accountRepository.getById(accountId); // fails if the account does not exist
    productRepository.getById(prodId); // fails if the product does not exist
    if (!hasPurchased(accountId, prodId)) {
      throw new IllegalArgumentException(NOT_PURCHASED_MESSAGE);
    }
    Review review = new Review(accountId, prodId, new Rating(rating), comment);
    reviews.add(review);
    return review;
  }

  /**
   * Lists the reviews of a product.
   *
   * <p>REQUIRES: prodId belongs to an existing product.
   *
   * <p>MODIFIES: nothing.
   *
   * <p>EFFECTS: Returns the reviews of the product, oldest first. The list is unmodifiable and
   * does not reflect later reviews.
   *
   * @param prodId the product whose reviews are listed; must not be null
   * @return the reviews of the product, possibly empty
   * @throws IllegalArgumentException if no such product exists
   */
  public List<Review> getProductReviews(UUID prodId) {
    productRepository.getById(prodId); // fails if the product does not exist
    return reviews.stream().filter(review -> review.getProductId().equals(prodId)).toList();
  }

  private boolean hasPurchased(UUID accountId, UUID productId) {
    return orderRepository.findByOwnerId(accountId).stream()
        .filter(order -> PURCHASED_STATUSES.contains(order.getStatus()))
        .flatMap(order -> order.getLines().stream())
        .anyMatch(line -> line.getProductId().equals(productId));
  }
}