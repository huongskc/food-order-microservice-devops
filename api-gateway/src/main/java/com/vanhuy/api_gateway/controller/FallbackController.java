package com.vanhuy.api_gateway.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FallbackController {
    @RequestMapping("/fallback/restaurant")
    public ResponseEntity<String> restaurantFallback() {
        return ResponseEntity.ok("Restaurant Service is currently unavailable. Please try again later.");
    }

    @RequestMapping("/fallback/order")
    public ResponseEntity<String> orderFallback() {
        return ResponseEntity.ok("Order Service is currently unavailable. Please try again later.");
    }

    @RequestMapping("/fallback/user")
    public ResponseEntity<String> userFallback() {
        return ResponseEntity.ok("User Service is currently unavailable. Please try again later.");
    }
}
