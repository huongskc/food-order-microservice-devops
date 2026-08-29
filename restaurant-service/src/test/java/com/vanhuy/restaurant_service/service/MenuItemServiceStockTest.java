package com.vanhuy.restaurant_service.service;

import com.vanhuy.restaurant_service.dto.StockDeductionResponse;
import com.vanhuy.restaurant_service.exception.InsufficientStockException;
import com.vanhuy.restaurant_service.exception.ResourceNotFoundException;
import com.vanhuy.restaurant_service.model.MenuItem;
import com.vanhuy.restaurant_service.repository.MenuItemRepository;
import com.vanhuy.restaurant_service.repository.RestaurantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MenuItemServiceStockTest {
    private MenuItemRepository menuItemRepository;
    private MenuItemService menuItemService;

    @BeforeEach
    void setUp() {
        menuItemRepository = mock(MenuItemRepository.class);
        menuItemService = new MenuItemService(
                menuItemRepository,
                mock(FileStorageService.class),
                mock(RestaurantRepository.class));
    }

    @Test
    void deductStockReturnsPriceWhenAtomicUpdateSucceeds() {
        MenuItem menuItem = MenuItem.builder()
                .itemId(1)
                .price(new BigDecimal("45000.00"))
                .stock(10)
                .build();
        when(menuItemRepository.findById(1)).thenReturn(Optional.of(menuItem));
        when(menuItemRepository.deductStock(1, 3)).thenReturn(1);

        StockDeductionResponse response = menuItemService.deductStock(1, 3);

        assertEquals(1, response.menuItemId());
        assertEquals(new BigDecimal("45000.00"), response.unitPrice());
    }

    @Test
    void deductStockRejectsInsufficientQuantity() {
        MenuItem menuItem = MenuItem.builder()
                .itemId(1)
                .price(new BigDecimal("45000.00"))
                .stock(1)
                .build();
        when(menuItemRepository.findById(1)).thenReturn(Optional.of(menuItem));
        when(menuItemRepository.deductStock(1, 2)).thenReturn(0);

        assertThrows(InsufficientStockException.class,
                () -> menuItemService.deductStock(1, 2));
    }

    @Test
    void deductStockRejectsUnknownMenuItem() {
        when(menuItemRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> menuItemService.deductStock(99, 1));
    }

    @Test
    void restoreStockAddsQuantityBack() {
        when(menuItemRepository.restoreStock(1, 3)).thenReturn(1);

        menuItemService.restoreStock(1, 3);

        verify(menuItemRepository).restoreStock(1, 3);
    }
}
