package org.example.model;

/** Immutable review score between {@value #MIN_SCORE} and {@value #MAX_SCORE}. */
public final class Rating {

  public static final int MIN_SCORE = 1;
  public static final int MAX_SCORE = 5;

  // AF: the number of stars a reviewer gave.
  // RI: MIN_SCORE <= score <= MAX_SCORE.
  private final int score;

  /**
   * Creates a rating.
   *
   * <p>REQUIRES: score is between MIN_SCORE and MAX_SCORE inclusive.
   *
   * <p>MODIFIES: nothing.
   *
   * <p>EFFECTS: Returns a rating holding score.
   *
   * @param score the number of stars
   * @throws IllegalArgumentException if score is out of range
   */
  public Rating(int score) {
    if (score < MIN_SCORE || score > MAX_SCORE) {
      throw new IllegalArgumentException(
          "Rating must be between " + MIN_SCORE + " and " + MAX_SCORE + ": " + score);
    }
    this.score = score;
  }

  /** Returns the score. */
  public int getScore() {
    return score;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof Rating that)) {
      return false;
    }
    return score == that.score;
  }

  @Override
  public int hashCode() {
    return Integer.hashCode(score);
  }

  @Override
  public String toString() {
    return Integer.toString(score);
  }
}