package com.ecommerce.store.domain.repository;

import com.ecommerce.store.domain.entity.Order;
import com.ecommerce.store.domain.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    Page<Order> findByCreatedAtBetween(Instant start, Instant end, Pageable pageable);

    Page<Order> findByBuyerEmail(String buyerEmail, Pageable pageable);

    Page<Order> findByBuyerEmailIgnoreCase(String buyerEmail, Pageable pageable);

    Page<Order> findByStatus(OrderStatus status, Pageable pageable);

    @Query("SELECT o FROM Order o WHERE o.createdAt BETWEEN :start AND :end ORDER BY o.createdAt DESC")
    Page<Order> findOrdersInPeriod(@Param("start") Instant start, @Param("end") Instant end, Pageable pageable);
}
