package org.example.model;

/** Lifecycle stage of an order. */
public enum OrderStatus {
  PENDING,
  PAID,
  PROCESSING,
  SHIPPED,
  DELIVERED,
  CANCELLED
}