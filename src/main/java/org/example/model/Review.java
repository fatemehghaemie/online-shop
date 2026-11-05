package org.example.model;

import java.util.Objects;
import java.util.UUID;

/** Immutable opinion of an account about a product. */
public final class Review {

  // AF: the account authorId rated productId with rating and wrote text.
  // RI: no field is null; text is trimmed and may be empty.
  private final UUID id;
  private final UUID authorId;
  private final UUID productId;
  private final Rating rating;
  private final String text;

  /**
   * Creates a review with a new identifier.
   *
   * @param authorId the reviewing account; must not be null
   * @param productId the reviewed product; must not be null
   * @param rating the score; must not be null
   * @param text the comment; must not be null, may be empty
   */
  public Review(UUID authorId, UUID productId, Rating rating, String text) {
    this.id = UUID.randomUUID();
    this.authorId = Objects.requireNonNull(authorId, "authorId must not be null");
    this.productId = Objects.requireNonNull(productId, "productId must not be null");
    this.rating = Objects.requireNonNull(rating, "rating must not be null");
    this.text = Objects.requireNonNull(text, "text must not be null").trim();
  }

  /** Returns the score. */
  public Rating getRating() {
    return rating;
  }

  /** Returns the unique identifier. */
  public UUID getId() {
    return id;
  }

  /** Returns the identifier of the reviewing account. */
  public UUID getAuthorId() {
    return authorId;
  }

  /** Returns the identifier of the reviewed product. */
  public UUID getProductId() {
    return productId;
  }

  /** Returns the comment. */
  public String getText() {
    return text;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof Review that)) {
      return false;
    }
    return id.equals(that.id);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }
}