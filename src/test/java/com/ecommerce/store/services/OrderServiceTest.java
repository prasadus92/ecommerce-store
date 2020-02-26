package com.ecommerce.store.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import com.ecommerce.store.entities.dao.Order;
import com.ecommerce.store.entities.dao.OrderItem;
import com.ecommerce.store.entities.dao.Product;
import com.ecommerce.store.entities.dto.OrderDto;
import com.ecommerce.store.entities.dto.OrderItemDto;
import com.ecommerce.store.repositories.OrderItemRepository;
import com.ecommerce.store.repositories.OrderRepository;
import com.ecommerce.store.repositories.ProductRepository;
import com.ecommerce.store.services.impl.OrderServiceImpl;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.*;

@ExtendWith (SpringExtension.class)
public class OrderServiceTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderService orderService;

    @Captor
    private ArgumentCaptor<Order> orderArgumentCaptor;

    @Captor
    private ArgumentCaptor<OrderItem> orderItemArgumentCaptor;

    @Test
    public void testOrderCreationSucceeds() {
        // Given
        UUID productId = UUID.randomUUID();
        Product product = Product.builder()
                                 .id(productId)
                                 .name("Kryptonite Grenade Launcher")
                                 .description("The Grenade Launcher in Batman's style")
                                 .price(120000.98)
                                 .build();
        OrderItemDto orderItemDto = OrderItemDto.builder()
                                                .productId(productId)
                                                .quantity(2)
                                                .build();
        OrderDto orderDto = OrderDto.builder()
                                    .items(Collections.singletonList(orderItemDto))
                                    .buyerEmail("robin@waynecorp.com")
                                    .build();
        Order toBeCreated = Order.builder()
                                 .buyerEmail("robin@waynecorp.com")
                                 .build();
        OrderItem orderItem = OrderItem.builder()
                                       .order(toBeCreated)
                                       .product(product)
                                       .unitPrice(120000.98)
                                       .quantity(2)
                                       .build();
        Order created = Order.builder()
                             .id(UUID.randomUUID())
                             .buyerEmail("robin@waynecorp.com")
                             .items(Collections.singleton(orderItem))
                             .build();
        when(orderRepository.save(any())).thenReturn(created);
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(orderItemRepository.save(any())).thenReturn(orderItem);

        // When
        orderService.placeOrder(orderDto);

        // Then
        verify(productRepository, times(1)).findById(any());
        verify(orderItemRepository, times(1)).save(orderItemArgumentCaptor.capture());
        assertThat(orderItemArgumentCaptor.getValue().getQuantity(), is(2));
        assertThat(orderItemArgumentCaptor.getValue().getUnitPrice(), is(120000.98));
        assertThat(orderItemArgumentCaptor.getValue().getProduct(), is(product));
        verify(orderRepository, times(2)).save(orderArgumentCaptor.capture());
        List<Order> capturedOrders = orderArgumentCaptor.getAllValues();
        assertThat(capturedOrders.get(0).getBuyerEmail(), is("robin@waynecorp.com"));
        assertThat(capturedOrders.get(0).getItems(), nullValue());
        assertThat(capturedOrders.get(1).getItems(), hasSize(1));
    }

    @TestConfiguration
    static class OrderServiceTestContextConfiguration {

        @MockBean
        private OrderRepository orderRepository;

        @MockBean
        private OrderItemRepository orderItemRepository;

        @MockBean
        private ProductRepository productRepository;

        @Bean
        public OrderService orderService() {
            return new OrderServiceImpl(orderRepository, orderItemRepository, productRepository);
        }
    }
}
