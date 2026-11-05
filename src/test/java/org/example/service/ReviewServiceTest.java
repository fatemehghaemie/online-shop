package org.example.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.example.model.Account;
import org.example.model.Product;
import org.example.model.Review;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ReviewServiceTest {

  private ShopFixture shop;
  private Account customer;
  private Product product;

  @BeforeEach
  void setUp() {
    shop = new ShopFixture();
    customer = shop.registerCustomer("buyer@example.com", "09121234567");
    product = shop.createProduct("Clean Code", "35.00", 10, shop.createCategory("Books"));
  }

  @Test
  void customerWhoPaidForTheProductCanReviewIt() {
    shop.placePaidOrder(customer, product, 1);

    Review review = shop.reviewService.submitReview(customer.getId(), product.getId(), 4, "Good");

    assertEquals(4, review.getRating().getScore());
    assertEquals(List.of(review), shop.reviewService.getProductReviews(product.getId()));
  }

  @Test
  void customerWithOnlyAPendingOrderCannotReviewTheProduct() {
    shop.placeOrder(customer, product, 1);

    assertThrows(
        IllegalArgumentException.class,
        () -> shop.reviewService.submitReview(customer.getId(), product.getId(), 4, "Good"));
  }

  @Test
  void customerWhoNeverBoughtTheProductCannotReviewIt() {
    assertThrows(
        IllegalArgumentException.class,
        () -> shop.reviewService.submitReview(customer.getId(), product.getId(), 4, "Good"));
  }

  @Test
  void ratingOutsideTheAllowedRangeIsRejectedAndNothingIsRecorded() {
    shop.placePaidOrder(customer, product, 1);

    assertThrows(
        IllegalArgumentException.class,
        () -> shop.reviewService.submitReview(customer.getId(), product.getId(), 6, "Too high"));
    assertEquals(List.of(), shop.reviewService.getProductReviews(product.getId()));
  }
}