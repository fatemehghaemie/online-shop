package org.example.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.example.model.Order;

/** {@link OrderRepository} that keeps orders in memory. */
public final class InMemoryOrderRepository implements OrderRepository {

  // AF: the stored orders are the values of ordersById.
  // RI: no value is null and every key equals the id of its order.
  private final Map<UUID, Order> ordersById = new LinkedHashMap<>();

  @Override
  public void save(Order order) {
    Objects.requireNonNull(order, "order must not be null");
    ordersById.put(order.getId(), order);
    checkRep();
  }

  @Override
  public Optional<Order> findById(UUID id) {
    Objects.requireNonNull(id, "id must not be null");
    return Optional.ofNullable(ordersById.get(id));
  }

  @Override
  public List<Order> findByOwnerId(UUID ownerId) {
    Objects.requireNonNull(ownerId, "ownerId must not be null");
    return ordersById.values().stream()
        .filter(order -> order.getOwnerId().equals(ownerId))
        .toList();
  }

  @Override
  public List<Order> findAll() {
    return List.copyOf(ordersById.values());
  }

  private void checkRep() {
    for (Map.Entry<UUID, Order> entry : ordersById.entrySet()) {
      assert entry.getValue() != null : "null order";
      assert entry.getValue().getId().equals(entry.getKey()) : "key does not match order id";
    }
  }
}