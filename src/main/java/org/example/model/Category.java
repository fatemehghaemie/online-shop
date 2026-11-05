package org.example.model;

import java.util.UUID;

/** Group of related products. */
public final class Category {

  // AF: a named product group identified by id, visible in the catalog only while active.
  // RI: id is non-null and name is non-blank.
  private final UUID id;
  private String name;
  private boolean active;

  /**
   * Creates an active category with a new identifier.
   *
   * @param name the category name; must not be null or blank
   * @throws IllegalArgumentException if the name is blank
   */
  public Category(String name) {
    if (name == null || name.isBlank()) {
      throw new IllegalArgumentException("Category name must not be blank");
    }
    this.id = UUID.randomUUID();
    this.name = name.trim();
    this.active = true;
  }

  /**
   * Makes the category visible.
   *
   * <p>MODIFIES: this category.
   *
   * <p>EFFECTS: After the call the category is active.
   */
  public void activate() {
    active = true;
  }

  /**
   * Hides the category.
   *
   * <p>MODIFIES: this category.
   *
   * <p>EFFECTS: After the call the category is not active.
   */
  public void deactivate() {
    active = false;
  }

  /** Returns the unique identifier. */
  public UUID getId() {
    return id;
  }

  /** Returns the category name. */
  public String getName() {
    return name;
  }

  /** Returns true if the category is visible. */
  public boolean isActive() {
    return active;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof Category that)) {
      return false;
    }
    return id.equals(that.id);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }
}