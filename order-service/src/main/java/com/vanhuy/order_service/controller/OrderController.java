package com.vanhuy.order_service.controller;

import com.vanhuy.order_service.dto.OrderRequest;
import com.vanhuy.order_service.dto.OrderResponse;
import com.vanhuy.order_service.service.OrderService;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/orders")
public class OrderController {
    private final OrderService orderService;

    @PostMapping()
    public ResponseEntity<OrderResponse> createOrder(@RequestBody OrderRequest orderRequest,
                                                     @AuthenticationPrincipal Jwt jwt) {
        OrderResponse orderResponse = orderService.createOrder(orderRequest, jwt.getClaim("userId"));
        return new ResponseEntity<>(orderResponse, HttpStatus.CREATED);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable Integer orderId,
                                                  @AuthenticationPrincipal Jwt jwt) {
        OrderResponse orderResponse = orderService.getOrderById(orderId, jwt.getClaim("userId"));
        return new ResponseEntity<>(orderResponse, HttpStatus.OK);
    }

    @GetMapping()
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Page<OrderResponse>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<OrderResponse> orders = orderService.getAllOrders(pageable);
        return ResponseEntity.ok(orders);
    }
}
