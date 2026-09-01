package com.vanhuy.restaurant_service.controller;

import com.vanhuy.restaurant_service.dto.StockDeductionResponse;
import com.vanhuy.restaurant_service.dto.StockUpdateRequest;
import com.vanhuy.restaurant_service.service.MenuItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/menu-items")
public class InternalStockController {
    private final MenuItemService menuItemService;

    @PostMapping("/{menuItemId}/stock/deduct")
    public ResponseEntity<StockDeductionResponse> deductStock(
            @PathVariable Integer menuItemId,
            @Valid @RequestBody StockUpdateRequest request) {
        return ResponseEntity.ok(menuItemService.deductStock(menuItemId, request.quantity()));
    }

    @PostMapping("/{menuItemId}/stock/restore")
    public ResponseEntity<Void> restoreStock(
            @PathVariable Integer menuItemId,
            @Valid @RequestBody StockUpdateRequest request) {
        menuItemService.restoreStock(menuItemId, request.quantity());
        return ResponseEntity.noContent().build();
    }
}
