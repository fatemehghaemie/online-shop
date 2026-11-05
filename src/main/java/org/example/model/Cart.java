package org.example.model;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/** Mutable collection of products a customer intends to buy. */
public final class Cart {

  // AF: the cart of ownerId is the mapping { productId -> quantity } given by the lines; a
  //     product that is not a key has quantity 0. Each line keeps the unit price from the
  //     moment the product was first added.
  // RI: no line is null and every key equals the product id of its line.
  private final UUID id;
  private final UUID ownerId;
  private final Map<UUID, CartLine> lines = new LinkedHashMap<>();

  /**
   * Creates an empty cart with a new identifier.
   *
   * @param ownerId the account that owns the cart; must not be null
   */
  public Cart(UUID ownerId) {
    this.id = UUID.randomUUID();
    this.ownerId = Objects.requireNonNull(ownerId, "ownerId must not be null");
    checkRep();
  }

  /**
   * Adds a product to the cart.
   *
   * <p>REQUIRES: product and qty are non-null.
   *
   * <p>MODIFIES: this cart.
   *
   * <p>EFFECTS: The quantity of the product increases by qty. If the product is already in the
   * cart, its stored unit price is not changed. Lines of other products are unchanged.
   *
   * @param product the product to add
   * @param qty the number of items to add
   */
  public void addOrUpdateProduct(Product product, Quantity qty) {
    Objects.requireNonNull(product, "product must not be null");
    Objects.requireNonNull(qty, "qty must not be null");
    CartLine existing = lines.get(product.getId());
    CartLine updated =
        existing == null ? createLine(product, qty) : increaseQuantity(existing, qty);
    lines.put(product.getId(), updated);
    checkRep();
  }

  /**
   * Sets the quantity of a product that is already in the cart.
   *
   * <p>REQUIRES: productId and qty are non-null and the product is in the cart.
   *
   * <p>MODIFIES: this cart.
   *
   * <p>EFFECTS: The quantity of the product becomes exactly qty and its stored unit price is
   * not changed. If the product is not in the cart, an exception is thrown and the cart is
   * unchanged.
   *
   * @param productId the product to change
   * @param qty the new number of items
   * @throws IllegalArgumentException if the product is not in the cart
   */
  public void updateQuantity(UUID productId, Quantity qty) {
    Objects.requireNonNull(productId, "productId must not be null");
    Objects.requireNonNull(qty, "qty must not be null");
    CartLine existing = lines.get(productId);
    if (existing == null) {
      throw new IllegalArgumentException("Product is not in the cart: " + productId);
    }
    lines.put(productId, withQuantity(existing, qty));
    checkRep();
  }

  /**
   * Removes a product completely.
   *
   * <p>REQUIRES: productId is non-null.
   *
   * <p>MODIFIES: this cart.
   *
   * <p>EFFECTS: After the call the product is not in the cart. Removing a product that is not
   * in the cart leaves the cart unchanged.
   *
   * @param productId the product to remove
   */
  public void removeProduct(UUID productId) {
    Objects.requireNonNull(productId, "productId must not be null");
    lines.remove(productId);
    checkRep();
  }

  /**
   * Removes all products.
   *
   * <p>MODIFIES: this cart.
   *
   * <p>EFFECTS: After the call the cart is empty.
   */
  public void clear() {
    lines.clear();
    checkRep();
  }

  /**
   * Sums the subtotals of all lines.
   *
   * <p>EFFECTS: Returns zero in the default currency for an empty cart; does not modify the
   * cart.
   *
   * @return the total price of the cart
   */
  public Money calculateTotal() {
    Money total =
        lines.values().stream()
            .map(CartLine::calculateSubtotal)
            .reduce(Money.zero(Money.DEFAULT_CURRENCY), Money::add);
    checkRep();
    return total;
  }

  /** Returns the unique identifier. */
  public UUID getId() {
    return id;
  }

  /** Returns the identifier of the owning account. */
  public UUID getOwnerId() {
    return ownerId;
  }

  /**
   * Returns the cart lines in the order the products were first added.
   *
   * <p>EFFECTS: The returned list is unmodifiable and does not reflect later changes to the
   * cart.
   */
  public List<CartLine> getLines() {
    return List.copyOf(lines.values());
  }

  private static CartLine createLine(Product product, Quantity quantity) {
    return new CartLine(product.getId(), product.getName(), product.getPrice(), quantity);
  }

  private static CartLine increaseQuantity(CartLine line, Quantity extra) {
    int total = Math.addExact(line.getQuantity().getValue(), extra.getValue());
    return withQuantity(line, new Quantity(total));
  }

  private static CartLine withQuantity(CartLine line, Quantity quantity) {
    return new CartLine(
        line.getProductId(), line.getProductName(), line.getUnitPrice(), quantity);
  }

  private void checkRep() {
    for (Map.Entry<UUID, CartLine> entry : lines.entrySet()) {
      assert entry.getValue() != null : "null cart line";
      assert entry.getValue().getProductId().equals(entry.getKey())
          : "map key does not match product id";
    }
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof Cart that)) {
      return false;
    }
    return id.equals(that.id);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }
}