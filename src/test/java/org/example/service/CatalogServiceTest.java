package org.example.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.example.model.Account;
import org.example.model.Category;
import org.example.model.Order;
import org.example.model.Product;
import org.example.model.ProductFilter;
import org.example.model.ProductSortOption;
import org.example.model.Rating;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CatalogServiceTest {

  private static final int PLENTY = 100;

  private ShopFixture shop;
  private Category books;
  private Category electronics;

  @BeforeEach
  void setUp() {
    shop = new ShopFixture();
    books = shop.createCategory("Books");
    electronics = shop.createCategory("Electronics");
  }

  private static List<String> names(List<Product> products) {
    return products.stream().map(Product::getName).toList();
  }

  private List<String> sortedNames(ProductSortOption option) {
    List<Product> sorted = shop.productService.sortProducts(shop.productService.getActiveProducts(), option);
    return names(sorted);
  }

  @Test
  void activeCategoriesExcludeInactiveOnes() {
    electronics.deactivate();

    assertEquals(List.of(books), shop.productService.getActiveCategories());
  }

  @Test
  void productsByCategoryReturnOnlyActiveProductsOfThatCategory() {
    shop.createProduct("Book", "10.00", 5, books);
    shop.createProduct("Mouse", "20.00", 5, electronics);
    Product oldBook = shop.createProduct("Old Book", "5.00", 5, books);
    shop.productService.deactivateProduct(shop.admin.getId(), oldBook.getId());

    List<Product> inBooks = shop.productService.getProductsByCategory(books.getId());

    assertEquals(List.of("Book"), names(inBooks));
  }

  @Test
  void productsOfAnInactiveCategoryAreRejected() {
    electronics.deactivate();

    assertThrows(
        IllegalArgumentException.class,
        () -> shop.productService.getProductsByCategory(electronics.getId()));
  }

  @Test
  void deactivatedProductDisappearsFromTheCatalog() {
    Product book = shop.createProduct("Book", "10.00", 5, books);

    shop.productService.deactivateProduct(shop.admin.getId(), book.getId());

    assertTrue(shop.productService.getActiveProducts().isEmpty());
  }

  @Test
  void filterByPriceRangeKeepsOnlyProductsInsideTheRange() {
    shop.createProduct("Cheap", "10.00", 5, books);
    shop.createProduct("Middle", "20.00", 5, books);
    shop.createProduct("Dear", "30.00", 5, books);
    ProductFilter filter =
        ProductFilter.none()
            .withMinPrice(ShopFixture.usd("15.00"))
            .withMaxPrice(ShopFixture.usd("25.00"));

    assertEquals(List.of("Middle"), names(shop.productService.filterProducts(filter)));
  }

  @Test
  void filterByMinimumRatingUsesTheAverageAndExcludesUnreviewedProducts() {
    Account customer = shop.registerCustomer("buyer@example.com", "09121234567");
    Product good = shop.createProduct("Good", "10.00", PLENTY, books);
    Product poor = shop.createProduct("Poor", "10.00", PLENTY, books);
    shop.createProduct("Unreviewed", "10.00", PLENTY, books);
    shop.placePaidOrder(customer, good, 1);
    shop.placePaidOrder(customer, poor, 1);
    shop.reviewService.submitReview(customer.getId(), good.getId(), 5, "Great");
    shop.reviewService.submitReview(customer.getId(), poor.getId(), 2, "Weak");
    ProductFilter filter = ProductFilter.none().withMinRating(new Rating(4));

    assertEquals(List.of("Good"), names(shop.productService.filterProducts(filter)));
  }

  @Test
  void sortByPriceOrdersFromCheapestAndFromMostExpensive() {
    shop.createProduct("Middle", "20.00", 5, books);
    shop.createProduct("Cheap", "10.00", 5, books);
    shop.createProduct("Dear", "30.00", 5, books);

    assertEquals(List.of("Cheap", "Middle", "Dear"), sortedNames(ProductSortOption.CHEAPEST));
    assertEquals(List.of("Dear", "Middle", "Cheap"), sortedNames(ProductSortOption.MOST_EXPENSIVE));
  }

  @Test
  void sortByNewestPutsTheLatestProductFirst() throws InterruptedException {
    shop.createProduct("Older", "10.00", 5, books);
    Thread.sleep(2);
    shop.createProduct("Newer", "10.00", 5, books);

    assertEquals(List.of("Newer", "Older"), sortedNames(ProductSortOption.NEWEST));
  }

  @Test
  void sortByMostPopularCountsUnitsInOrdersThatAreNotCancelled() {
    Account customer = shop.registerCustomer("buyer@example.com", "09121234567");
    Product niche = shop.createProduct("Niche", "10.00", PLENTY, books);
    Product unsold = shop.createProduct("Unsold", "10.00", PLENTY, books);
    Product hit = shop.createProduct("Hit", "10.00", PLENTY, books);
    shop.placePaidOrder(customer, hit, 3);
    shop.placePaidOrder(customer, niche, 1);
    Order cancelled = shop.placeOrder(customer, unsold, 5);
    cancelled.cancel();

    assertEquals(List.of("Hit", "Niche", "Unsold"), sortedNames(ProductSortOption.MOST_POPULAR));
  }

  @Test
  void sortByHighestRatingPutsUnreviewedProductsLast() {
    Account customer = shop.registerCustomer("buyer@example.com", "09121234567");
    Product average = shop.createProduct("Average", "10.00", PLENTY, books);
    Product best = shop.createProduct("Best", "10.00", PLENTY, books);
    shop.createProduct("Unreviewed", "10.00", PLENTY, books);
    shop.placePaidOrder(customer, average, 1);
    shop.placePaidOrder(customer, best, 1);
    shop.reviewService.submitReview(customer.getId(), average.getId(), 3, "Fine");
    shop.reviewService.submitReview(customer.getId(), best.getId(), 5, "Great");

    assertEquals(
        List.of("Best", "Average", "Unreviewed"), sortedNames(ProductSortOption.HIGHEST_RATING));
  }

  @Test
  void adminCanUpdateTheInformationOfAProduct() {
    Product book = shop.createProduct("Book", "10.00", 5, books);

    shop.productService.updateProduct(
        shop.admin.getId(), book.getId(), "New Title", "A description", ShopFixture.usd("12.00"));

    Product stored = shop.productRepository.getById(book.getId());
    assertEquals("New Title", stored.getName());
    assertEquals("A description", stored.getDescription());
    assertEquals(ShopFixture.usd("12.00"), stored.getPrice());
  }

  @Test
  void customerCannotUpdateOrWithdrawAProduct() {
    Account customer = shop.registerCustomer("buyer@example.com", "09121234567");
    Product book = shop.createProduct("Book", "10.00", 5, books);

    assertThrows(
        SecurityException.class,
        () ->
            shop.productService.updateProduct(
                customer.getId(), book.getId(), "Hacked", "", ShopFixture.usd("1.00")));
    assertThrows(
        SecurityException.class,
        () -> shop.productService.deactivateProduct(customer.getId(), book.getId()));
  }
}