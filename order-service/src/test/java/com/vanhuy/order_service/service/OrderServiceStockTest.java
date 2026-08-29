package com.vanhuy.order_service.service;

import com.vanhuy.order_service.client.NotificationClient;
import com.vanhuy.order_service.client.RestaurantClient;
import com.vanhuy.order_service.client.UserServiceClient;
import com.vanhuy.order_service.dto.OrderItemRequest;
import com.vanhuy.order_service.dto.OrderRequest;
import com.vanhuy.order_service.dto.OrderResponse;
import com.vanhuy.order_service.dto.StockDeductionResponse;
import com.vanhuy.order_service.dto.StockUpdateRequest;
import com.vanhuy.order_service.model.Order;
import com.vanhuy.order_service.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrderServiceStockTest {
    private static final String AUTHORIZATION = "Bearer token";

    private RestaurantClient restaurantClient;
    private OrderRepository orderRepository;
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        restaurantClient = mock(RestaurantClient.class);
        orderRepository = mock(OrderRepository.class);
        orderService = new OrderService(
                mock(UserServiceClient.class),
                orderRepository,
                restaurantClient,
                mock(NotificationClient.class));
    }

    @Test
    void duplicateItemsUseOneCombinedStockDeduction() {
        OrderRequest request = orderRequest(item(1, 1), item(1, 2));
        when(restaurantClient.deductStock(1, new StockUpdateRequest(3), AUTHORIZATION))
                .thenReturn(new StockDeductionResponse(1, new BigDecimal("100.00")));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setOrderId(10);
            return order;
        });

        OrderResponse response = orderService.createOrder(request, 7, AUTHORIZATION);

        assertEquals(new BigDecimal("324.0000"), response.getTotalAmount());
        verify(restaurantClient).deductStock(1, new StockUpdateRequest(3), AUTHORIZATION);
        verify(restaurantClient, never()).restoreStock(any(), any(), any());
    }

    @Test
    void failureOnLaterItemRestoresPreviouslyDeductedStock() {
        OrderRequest request = orderRequest(item(1, 2), item(2, 1));
        when(restaurantClient.deductStock(1, new StockUpdateRequest(2), AUTHORIZATION))
                .thenReturn(new StockDeductionResponse(1, new BigDecimal("100.00")));
        when(restaurantClient.deductStock(2, new StockUpdateRequest(1), AUTHORIZATION))
                .thenThrow(new RuntimeException("Insufficient stock"));

        assertThrows(RuntimeException.class,
                () -> orderService.createOrder(request, 7, AUTHORIZATION));

        verify(restaurantClient).restoreStock(1, new StockUpdateRequest(2), AUTHORIZATION);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void orderPersistenceFailureRestoresDeductedStock() {
        OrderRequest request = orderRequest(item(1, 2));
        when(restaurantClient.deductStock(1, new StockUpdateRequest(2), AUTHORIZATION))
                .thenReturn(new StockDeductionResponse(1, new BigDecimal("100.00")));
        when(orderRepository.save(any(Order.class)))
                .thenThrow(new RuntimeException("Database unavailable"));

        assertThrows(RuntimeException.class,
                () -> orderService.createOrder(request, 7, AUTHORIZATION));

        verify(restaurantClient).restoreStock(1, new StockUpdateRequest(2), AUTHORIZATION);
    }

    private OrderRequest orderRequest(OrderItemRequest... items) {
        return OrderRequest.builder()
                .recipientName("Customer")
                .contactEmail("customer@example.com")
                .shippingAddress("Hanoi")
                .contactPhone("0123456789")
                .items(List.of(items))
                .build();
    }

    private OrderItemRequest item(Integer menuItemId, Integer quantity) {
        OrderItemRequest item = new OrderItemRequest();
        item.setMenuItemId(menuItemId);
        item.setQuantity(quantity);
        return item;
    }
}
