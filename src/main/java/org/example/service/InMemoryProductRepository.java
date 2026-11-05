package org.example.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.example.model.Category;
import org.example.model.Product;

/** {@link ProductRepository} that keeps the catalog in memory. */
public final class InMemoryProductRepository implements ProductRepository {

  // AF: the catalog is the products in productsById and the categories in categoriesById.
  // RI: no value is null and every key equals the id of its value.
  private final Map<UUID, Product> productsById = new LinkedHashMap<>();
  private final Map<UUID, Category> categoriesById = new LinkedHashMap<>();

  @Override
  public void save(Product product) {
    Objects.requireNonNull(product, "product must not be null");
    productsById.put(product.getId(), product);
    checkRep();
  }

  @Override
  public Optional<Product> findById(UUID id) {
    Objects.requireNonNull(id, "id must not be null");
    return Optional.ofNullable(productsById.get(id));
  }

  @Override
  public List<Product> findAll() {
    return List.copyOf(productsById.values());
  }

  @Override
  public void saveCategory(Category category) {
    Objects.requireNonNull(category, "category must not be null");
    categoriesById.put(category.getId(), category);
    checkRep();
  }

  @Override
  public Optional<Category> findCategoryById(UUID id) {
    Objects.requireNonNull(id, "id must not be null");
    return Optional.ofNullable(categoriesById.get(id));
  }

  @Override
  public List<Category> findAllCategories() {
    return List.copyOf(categoriesById.values());
  }

  private void checkRep() {
    for (Map.Entry<UUID, Product> entry : productsById.entrySet()) {
      assert entry.getValue() != null : "null product";
      assert entry.getValue().getId().equals(entry.getKey()) : "key does not match product id";
    }
    for (Map.Entry<UUID, Category> entry : categoriesById.entrySet()) {
      assert entry.getValue() != null : "null category";
      assert entry.getValue().getId().equals(entry.getKey()) : "key does not match category id";
    }
  }
}