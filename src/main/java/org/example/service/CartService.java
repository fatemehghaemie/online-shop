package org.example.service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.example.model.Cart;
import org.example.model.Product;
import org.example.model.Quantity;

/** Use cases for viewing and editing the shopping cart of an account. */
public final class CartService {

  private static final String UNAVAILABLE_MESSAGE =
      "Product is not available in the requested quantity: ";

  // AF: each entry maps an account to the cart it is filling; an account without an entry
  //     has an empty cart.
  // RI: no value is null and every key equals the owner id of its cart.
  private final Map<UUID, Cart> cartsByOwnerId = new LinkedHashMap<>();
  private final AccountRepository accountRepository;
  private final ProductRepository productRepository;

  /**
   * Creates the service.
   *
   * @param accountRepository where accounts are stored; must not be null
   * @param productRepository where the catalog is stored; must not be null
   */
  public CartService(AccountRepository accountRepository, ProductRepository productRepository) {
    this.accountRepository =
        Objects.requireNonNull(accountRepository, "accountRepository must not be null");
    this.productRepository =
        Objects.requireNonNull(productRepository, "productRepository must not be null");
  }

  /**
   * Returns the cart of an account.
   *
   * <p>REQUIRES: accountId belongs to an existing account.
   *
   * <p>MODIFIES: nothing observable. An account without a cart gets an empty one, which is
   * indistinguishable from having no cart.
   *
   * <p>EFFECTS: Returns the stored cart instance, not a copy. Callers read it and edit it only
   * through this service.
   *
   * @param accountId the account that owns the cart; must not be null
   * @return the cart of the account
   * @throws IllegalArgumentException if no such account exists
   */
  public Cart getCart(UUID accountId) {
    accountRepository.getById(accountId); // fails if the account does not exist
    Cart cart = cartsByOwnerId.computeIfAbsent(accountId, Cart::new);
    checkRep();
    return cart;
  }

  /**
   * Adds items of a product to the cart of an account.
   *
   * <p>REQUIRES: the account and the product exist, qty is positive, the product is active and
   * its stock covers the quantity already in the cart plus qty.
   *
   * <p>MODIFIES: the cart of the account.
   *
   * <p>EFFECTS: The quantity of the product in the cart increases by qty. If a requirement is
   * not met, an exception is thrown and the cart is unchanged.
   *
   * @param accountId the account that owns the cart; must not be null
   * @param prodId the product to add; must not be null
   * @param qty the number of items to add
   * @throws IllegalArgumentException if a requirement is not met
   */
  public void addProduct(UUID accountId, UUID prodId, int qty) {
    Quantity requested = new Quantity(qty);
    Product product = productRepository.getById(prodId);
    Cart cart = getCart(accountId);
    Quantity wanted = totalWanted(cart, product, requested);
    if (!product.canBePurchased(wanted)) {
      throw new IllegalArgumentException(UNAVAILABLE_MESSAGE + product.getName());
    }
    cart.addOrUpdateProduct(product, requested);
  }

  /**
   * Sets the quantity of a product that is already in the cart of an account.
   *
   * <p>REQUIRES: the account and the product exist, the product is in the cart, qty is
   * between 1 and the stock of the product, and the product is active.
   *
   * <p>MODIFIES: the cart of the account.
   *
   * <p>EFFECTS: The quantity of the product in the cart becomes exactly qty. If a requirement
   * is not met, an exception is thrown and the cart is unchanged.
   *
   * @param accountId the account that owns the cart; must not be null
   * @param prodId the product to change; must not be null
   * @param qty the new number of items
   * @throws IllegalArgumentException if a requirement is not met
   */
  public void updateQuantity(UUID accountId, UUID prodId, int qty) {
    Quantity requested = new Quantity(qty);
    Product product = productRepository.getById(prodId);
    Cart cart = getCart(accountId);
    if (!product.canBePurchased(requested)) {
      throw new IllegalArgumentException(UNAVAILABLE_MESSAGE + product.getName());
    }
    cart.updateQuantity(prodId, requested);
  }

  /**
   * Removes a product completely from the cart of an account.
   *
   * <p>REQUIRES: the account exists.
   *
   * <p>MODIFIES: the cart of the account.
   *
   * <p>EFFECTS: After the call the product is not in the cart. Removing a product that is not
   * in the cart leaves the cart unchanged.
   *
   * @param accountId the account that owns the cart; must not be null
   * @param prodId the product to remove; must not be null
   * @throws IllegalArgumentException if no such account exists
   */
  public void removeProduct(UUID accountId, UUID prodId) {
    Objects.requireNonNull(prodId, "prodId must not be null");
    getCart(accountId).removeProduct(prodId);
  }

  /**
   * Empties the cart of an account.
   *
   * <p>REQUIRES: the account exists.
   *
   * <p>MODIFIES: the cart of the account.
   *
   * <p>EFFECTS: After the call the cart has no lines and its total is zero.
   *
   * @param accountId the account that owns the cart; must not be null
   * @throws IllegalArgumentException if no such account exists
   */
  public void clearCart(UUID accountId) {
    getCart(accountId).clear();
  }

  private static Quantity totalWanted(Cart cart, Product product, Quantity requested) {
    int alreadyInCart =
        cart.getLines().stream()
            .filter(line -> line.getProductId().equals(product.getId()))
            .mapToInt(line -> line.getQuantity().getValue())
            .sum();
    return new Quantity(Math.addExact(alreadyInCart, requested.getValue()));
  }

  private void checkRep() {
    for (Map.Entry<UUID, Cart> entry : cartsByOwnerId.entrySet()) {
      assert entry.getValue() != null : "null cart";
      assert entry.getValue().getOwnerId().equals(entry.getKey()) : "key does not match owner id";
    }
  }
}