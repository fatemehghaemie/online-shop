package org.example.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.example.model.Category;
import org.example.model.Product;

/** Storage of the catalog, meaning products and their categories. */
public interface ProductRepository {

  /**
   * Stores a product, replacing the stored product with the same id.
   *
   * <p>REQUIRES: product is non-null.
   *
   * <p>MODIFIES: this repository.
   *
   * <p>EFFECTS: After the call, findById(product.getId()) returns the product.
   *
   * @param product the product to store
   */
  void save(Product product);

  /**
   * Finds a product by its identifier.
   *
   * <p>REQUIRES: id is non-null.
   *
   * <p>EFFECTS: Returns the stored product, or empty if there is none; does not modify this
   * repository. The returned product is the stored instance, not a copy.
   *
   * @param id the product identifier
   * @return the matching product, if any
   */
  Optional<Product> findById(UUID id);

  /**
   * Lists all products.
   *
   * <p>EFFECTS: Returns the products in the order they were first stored. The list is
   * unmodifiable and does not reflect later changes to this repository.
   *
   * @return all stored products
   */
  List<Product> findAll();

  /**
   * Stores a category, replacing the stored category with the same id.
   *
   * <p>REQUIRES: category is non-null.
   *
   * <p>MODIFIES: this repository.
   *
   * <p>EFFECTS: After the call, findCategoryById(category.getId()) returns the category.
   *
   * @param category the category to store
   */
  void saveCategory(Category category);

  /**
   * Finds a category by its identifier.
   *
   * <p>REQUIRES: id is non-null.
   *
   * <p>EFFECTS: Returns the stored category, or empty if there is none; does not modify this
   * repository.
   *
   * @param id the category identifier
   * @return the matching category, if any
   */
  Optional<Category> findCategoryById(UUID id);

  /**
   * Lists all categories.
   *
   * <p>EFFECTS: Returns the categories in the order they were first stored. The list is
   * unmodifiable and does not reflect later changes to this repository.
   *
   * @return all stored categories
   */
  List<Category> findAllCategories();

  /**
   * Returns the product with the given identifier, or fails.
   *
   * <p>REQUIRES: id is non-null and belongs to a stored product.
   *
   * <p>EFFECTS: Returns the stored product instance; does not modify this repository.
   *
   * @param id the product identifier
   * @return the matching product
   * @throws IllegalArgumentException if no such product exists
   */
  default Product getById(UUID id) {
    return findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Product not found: " + id));
  }
}