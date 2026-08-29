package com.vanhuy.order_service.client;

import com.vanhuy.order_service.dto.StockDeductionResponse;
import com.vanhuy.order_service.dto.StockUpdateRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.math.BigDecimal;

@FeignClient(name = "restaurant-service")
public interface RestaurantClient {
    @GetMapping("/api/v1/menu-items/{menuItemId}")
    BigDecimal getPriceByMenuItemId(@PathVariable Integer menuItemId);

    @PostMapping("/internal/menu-items/{menuItemId}/stock/deduct")
    StockDeductionResponse deductStock(
            @PathVariable Integer menuItemId,
            @RequestBody StockUpdateRequest request,
            @RequestHeader("Authorization") String authorizationHeader);

    @PostMapping("/internal/menu-items/{menuItemId}/stock/restore")
    void restoreStock(
            @PathVariable Integer menuItemId,
            @RequestBody StockUpdateRequest request,
            @RequestHeader("Authorization") String authorizationHeader);
}
