package org.example.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class MoneyTest {

  private static Money usd(String amount) {
    return new Money(new BigDecimal(amount), "USD");
  }

  @Test
  void addReturnsSumWithoutChangingOperands() {
    Money first = usd("10.50");
    Money second = usd("4.25");

    Money sum = first.add(second);

    assertEquals(usd("14.75"), sum);
    assertEquals(usd("10.50"), first);
    assertEquals(usd("4.25"), second);
  }

  @Test
  void addRejectsDifferentCurrencies() {
    Money euros = new Money(BigDecimal.ONE, "EUR");

    assertThrows(IllegalArgumentException.class, () -> usd("1.00").add(euros));
  }

  @Test
  void multiplyScalesByQuantity() {
    assertEquals(usd("29.97"), usd("9.99").multiply(new Quantity(3)));
  }

  @Test
  void amountIsRoundedHalfUpToTwoDecimals() {
    assertEquals(usd("1.01"), usd("1.005"));
  }

  @Test
  void negativeAmountIsRejected() {
    assertThrows(IllegalArgumentException.class, () -> usd("-0.01"));
  }
}