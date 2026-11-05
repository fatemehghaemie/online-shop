package org.example.service;

import java.util.Objects;
import java.util.UUID;
import org.example.model.Product;
import org.example.model.StockQuantity;

/** Use cases for reading and changing the stock of products. */
public final class InventoryService {

  private final ProductRepository productRepository;
  private final AccountRepository accountRepository;

  /**
   * Creates the service.
   *
   * @param productRepository where the catalog is stored; must not be null
   * @param accountRepository where accounts are stored, used to check administrators; must not
   *     be null
   */
  public InventoryService(
      ProductRepository productRepository, AccountRepository accountRepository) {
    this.productRepository =
        Objects.requireNonNull(productRepository, "productRepository must not be null");
    this.accountRepository =
        Objects.requireNonNull(accountRepository, "accountRepository must not be null");
  }

  /**
   * Returns the stock of a product.
   *
   * <p>REQUIRES: productId belongs to an existing product.
   *
   * <p>MODIFIES: nothing.
   *
   * @param productId the product to read; must not be null
   * @return the current stock
   * @throws IllegalArgumentException if no such product exists
   */
  public StockQuantity getStock(UUID productId) {
    return productRepository.getById(productId).getStock();
  }

  /**
   * Changes the stock of a product by a signed amount.
   *
   * <p>REQUIRES: adminId belongs to an administrator, prodId belongs to an existing product and
   * the resulting stock is not negative.
   *
   * <p>MODIFIES: the product and the product repository.
   *
   * <p>EFFECTS: The stock becomes its previous value plus delta. If a requirement is not met,
   * an exception is thrown and the stock is unchanged.
   *
   * @param adminId the administrator who changes the stock; must not be null
   * @param prodId the product to change; must not be null
   * @param delta the change; positive to add stock, negative to remove it
   * @throws SecurityException if adminId is not an administrator
   * @throws IllegalArgumentException if the product does not exist or the stock would become
   *     negative
   */
  public void adjustStock(UUID adminId, UUID prodId, int delta) {
    accountRepository.getAdminById(adminId);
    Product product = productRepository.getById(prodId);
    product.adjustStock(delta);
    productRepository.save(product);
  }
}