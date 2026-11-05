package org.example.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.example.model.Order;

/** Storage of placed orders. */
public interface OrderRepository {

  /**
   * Stores an order, replacing the stored order with the same id.
   *
   * <p>REQUIRES: order is non-null.
   *
   * <p>MODIFIES: this repository.
   *
   * <p>EFFECTS: After the call, findById(order.getId()) returns the order.
   *
   * @param order the order to store
   */
  void save(Order order);

  /**
   * Finds an order by its identifier.
   *
   * <p>REQUIRES: id is non-null.
   *
   * <p>EFFECTS: Returns the stored order, or empty if there is none; does not modify this
   * repository. The returned order is the stored instance, not a copy.
   *
   * @param id the order identifier
   * @return the matching order, if any
   */
  Optional<Order> findById(UUID id);

  /**
   * Lists the orders of one account.
   *
   * <p>REQUIRES: ownerId is non-null.
   *
   * <p>EFFECTS: Returns the orders placed by the account in the order they were first stored.
   * The list is unmodifiable and does not reflect later changes to this repository.
   *
   * @param ownerId the account that placed the orders
   * @return the orders of the account, possibly empty
   */
  List<Order> findByOwnerId(UUID ownerId);

  /**
   * Lists all orders of all accounts.
   *
   * <p>EFFECTS: Returns the orders in the order they were first stored. The list is
   * unmodifiable and does not reflect later changes to this repository.
   *
   * @return every stored order, possibly empty
   */
  List<Order> findAll();

  /**
   * Returns the order with the given identifier, or fails.
   *
   * <p>REQUIRES: id is non-null and belongs to a stored order.
   *
   * <p>EFFECTS: Returns the stored order instance; does not modify this repository.
   *
   * @param id the order identifier
   * @return the matching order
   * @throws IllegalArgumentException if no such order exists
   */
  default Order getById(UUID id) {
    return findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Order not found: " + id));
  }
}