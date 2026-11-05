package org.example.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ProductFilterTest {

  private static Money usd(String amount) {
    return new Money(new BigDecimal(amount), Money.DEFAULT_CURRENCY);
  }

  @Test
  void noneHasNoConditions() {
    ProductFilter filter = ProductFilter.none();

    assertTrue(filter.getCategoryId().isEmpty());
    assertTrue(filter.getMinPrice().isEmpty());
    assertTrue(filter.getMaxPrice().isEmpty());
    assertTrue(filter.getMinRating().isEmpty());
  }

  @Test
  void minPriceAboveMaxPriceIsRejectedInEitherOrder() {
    ProductFilter withMax = ProductFilter.none().withMaxPrice(usd("10.00"));
    ProductFilter withMin = ProductFilter.none().withMinPrice(usd("20.00"));

    assertThrows(IllegalArgumentException.class, () -> withMax.withMinPrice(usd("20.00")));
    assertThrows(IllegalArgumentException.class, () -> withMin.withMaxPrice(usd("10.00")));
  }

  @Test
  void withMethodsReturnANewFilterAndKeepTheOriginalUnchanged() {
    UUID categoryId = UUID.randomUUID();
    ProductFilter base = ProductFilter.none();

    ProductFilter filtered = base.withCategory(categoryId);

    assertTrue(base.getCategoryId().isEmpty());
    assertEquals(Optional.of(categoryId), filtered.getCategoryId());
  }
}