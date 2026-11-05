package org.example.cli;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Scanner;
import java.util.Set;
import org.example.model.Address;

/** Reads prompted text, numbers, answers, addresses and list selections from a text stream. */
public final class ConsoleReader {

  /** Message shown when a menu choice is not valid. */
  public static final String INVALID_CHOICE_MESSAGE = "Invalid choice.";

  private static final int CANCEL_NUMBER = 0;
  private static final Set<String> YES_ANSWERS = Set.of("y", "yes");
  private static final Set<String> NO_ANSWERS = Set.of("n", "no");
  private static final String SKIP_HINT = " (empty to skip)";
  private static final String INVALID_NUMBER_MESSAGE = "Please enter a whole number.";
  private static final String INVALID_AMOUNT_MESSAGE = "Please enter a number, e.g. 12.50.";

  private final Scanner scanner;

  /**
   * Creates a reader.
   *
   * @param input the stream to read from; must not be null
   */
  public ConsoleReader(InputStream input) {
    Objects.requireNonNull(input, "input must not be null");
    this.scanner = new Scanner(input);
  }

  /**
   * Prints a prompt and reads one line.
   *
   * <p>MODIFIES: the console and the input stream.
   *
   * @param label the prompt text
   * @return the line the user typed, without surrounding spaces
   * @throws java.util.NoSuchElementException if the input has ended
   */
  public String readLine(String label) {
    System.out.print(label + ": ");
    return scanner.nextLine().trim();
  }

  /**
   * Prompts until the user types a whole number.
   *
   * <p>MODIFIES: the console and the input stream.
   *
   * @param label the prompt text
   * @return the number the user typed
   * @throws java.util.NoSuchElementException if the input has ended
   */
  public int readInt(String label) {
    while (true) {
      OptionalInt number = parseInt(readLine(label));
      if (number.isPresent()) {
        return number.getAsInt();
      }
      System.out.println(INVALID_NUMBER_MESSAGE);
    }
  }

  /**
   * Prompts until the user types a whole number or leaves the answer empty.
   *
   * <p>MODIFIES: the console and the input stream.
   *
   * @param label the prompt text
   * @return the number the user typed, or empty if the answer was empty
   * @throws java.util.NoSuchElementException if the input has ended
   */
  public OptionalInt readOptionalInt(String label) {
    while (true) {
      String text = readLine(label + SKIP_HINT);
      if (text.isEmpty()) {
        return OptionalInt.empty();
      }
      OptionalInt number = parseInt(text);
      if (number.isPresent()) {
        return number;
      }
      System.out.println(INVALID_NUMBER_MESSAGE);
    }
  }

  /**
   * Prompts until the user types a decimal number.
   *
   * <p>MODIFIES: the console and the input stream.
   *
   * @param label the prompt text
   * @return the number the user typed
   * @throws java.util.NoSuchElementException if the input has ended
   */
  public BigDecimal readAmount(String label) {
    while (true) {
      Optional<BigDecimal> amount = parseAmount(readLine(label));
      if (amount.isPresent()) {
        return amount.get();
      }
      System.out.println(INVALID_AMOUNT_MESSAGE);
    }
  }

  /**
   * Prompts until the user types a decimal number or leaves the answer empty.
   *
   * <p>MODIFIES: the console and the input stream.
   *
   * @param label the prompt text
   * @return the number the user typed, or empty if the answer was empty
   * @throws java.util.NoSuchElementException if the input has ended
   */
  public Optional<BigDecimal> readOptionalAmount(String label) {
    while (true) {
      String text = readLine(label + SKIP_HINT);
      if (text.isEmpty()) {
        return Optional.empty();
      }
      Optional<BigDecimal> amount = parseAmount(text);
      if (amount.isPresent()) {
        return amount;
      }
      System.out.println(INVALID_AMOUNT_MESSAGE);
    }
  }

  /**
   * Prompts until the user answers yes or no.
   *
   * <p>MODIFIES: the console and the input stream.
   *
   * @param label the question
   * @return true for yes (y or yes), false for no (n or no)
   * @throws java.util.NoSuchElementException if the input has ended
   */
  public boolean confirm(String label) {
    while (true) {
      String answer = readLine(label + " (yes/no)").toLowerCase(Locale.ROOT);
      if (YES_ANSWERS.contains(answer)) {
        return true;
      }
      if (NO_ANSWERS.contains(answer)) {
        return false;
      }
      System.out.println("Please answer yes or no.");
    }
  }

  /**
   * Prompts until the user types a valid address.
   *
   * <p>MODIFIES: the console and the input stream.
   *
   * @return the address the user typed
   * @throws java.util.NoSuchElementException if the input has ended
   */
  public Address readAddress() {
    while (true) {
      String province = readLine("Province");
      String city = readLine("City");
      String street = readLine("Street");
      String postalCode = readLine("Postal code");
      try {
        return new Address(province, city, street, postalCode);
      } catch (IllegalArgumentException e) {
        System.out.println("Invalid address: " + e.getMessage());
      }
    }
  }

  /**
   * Lets the user pick one item of a list by its position, counted from 1.
   *
   * <p>MODIFIES: the console and the input stream.
   *
   * <p>EFFECTS: Returns empty without prompting if the list is empty, and empty if the user
   * types 0 or a number outside the list.
   *
   * @param label the prompt text
   * @param items the items to choose from
   * @param <T> the type of the items
   * @return the chosen item, if any
   * @throws java.util.NoSuchElementException if the input has ended
   */
  public <T> Optional<T> select(String label, List<T> items) {
    if (items.isEmpty()) {
      return Optional.empty();
    }
    int number = readInt(label + " (0 to cancel)");
    if (number == CANCEL_NUMBER) {
      return Optional.empty();
    }
    if (number < 1 || number > items.size()) {
      System.out.println(INVALID_CHOICE_MESSAGE);
      return Optional.empty();
    }
    return Optional.of(items.get(number - 1));
  }

  private static OptionalInt parseInt(String text) {
    try {
      return OptionalInt.of(Integer.parseInt(text));
    } catch (NumberFormatException e) {
      return OptionalInt.empty();
    }
  }

  private static Optional<BigDecimal> parseAmount(String text) {
    try {
      return Optional.of(new BigDecimal(text));
    } catch (NumberFormatException e) {
      return Optional.empty();
    }
  }
}