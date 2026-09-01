package com.vanhuy.restaurant_service.service;

import com.vanhuy.restaurant_service.dto.MenuItemDTO;
import com.vanhuy.restaurant_service.dto.StockDeductionResponse;
import com.vanhuy.restaurant_service.exception.InsufficientStockException;
import com.vanhuy.restaurant_service.exception.ResourceNotFoundException;
import com.vanhuy.restaurant_service.exception.RestaurantNotFoundException;
import com.vanhuy.restaurant_service.model.MenuItem;
import com.vanhuy.restaurant_service.model.Restaurant;
import com.vanhuy.restaurant_service.repository.MenuItemRepository;
import com.vanhuy.restaurant_service.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MenuItemService {
    private final MenuItemRepository menuItemRepository;
    private final FileStorageService imageService;
    private final RestaurantRepository restaurantRepository;

    @Value("${app.base-url}")
    private String baseUrl;

    @Cacheable(value = "menuItems", key = "#restaurant.restaurantId")
    public List<MenuItemDTO> getMenuItemsByRestaurantId(Restaurant restaurant) {
        List<MenuItem> menuItems = menuItemRepository.findByRestaurant(restaurant);
        return menuItems.stream()
                .map(this::toDTO)
                .toList();
    }

    public MenuItemDTO createMenuItem (MenuItemDTO menuItemDTO, Integer restaurantId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RestaurantNotFoundException("Restaurant not found"));
        MenuItem menuItem = new MenuItem(
                menuItemDTO.menuItemId(),
                menuItemDTO.name(),
                menuItemDTO.price(),
                menuItemDTO.stock(),
                menuItemDTO.imageUrl(),
                restaurant
        );
        menuItemRepository.save(menuItem);
        return toDTO(menuItem);
    }

    public MenuItemDTO uploadImage(Integer menuItemId, MultipartFile file) throws IOException {
        MenuItem menuItem = menuItemRepository.findById(menuItemId)
                .orElseThrow(() -> new RestaurantNotFoundException("Menu item not found"));

        String oldImageFileName = menuItem.getImageUrl();

        String fileName = imageService.uploadImage(file ,oldImageFileName);

        menuItem.setImageUrl(fileName);
        menuItemRepository.save(menuItem);
        return toDTO(menuItem);
    }

    public BigDecimal getPriceByMenuItemId(Integer menuItemId) {
        MenuItem menuItem = menuItemRepository.findById(menuItemId)
                .orElseThrow(() -> new RestaurantNotFoundException("Menu item not found"));
        return menuItem.getPrice();
    }

    @Transactional
    public StockDeductionResponse deductStock(Integer menuItemId, Integer quantity) {
        MenuItem menuItem = menuItemRepository.findById(menuItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found"));

        int updatedRows = menuItemRepository.deductStock(menuItemId, quantity);
        if (updatedRows == 0) {
            throw new InsufficientStockException(
                    "Insufficient stock for menu item " + menuItemId);
        }

        return new StockDeductionResponse(menuItemId, menuItem.getPrice());
    }

    @Transactional
    public void restoreStock(Integer menuItemId, Integer quantity) {
        int updatedRows = menuItemRepository.restoreStock(menuItemId, quantity);
        if (updatedRows == 0) {
            throw new ResourceNotFoundException("Menu item not found");
        }
    }

    private MenuItemDTO toDTO(MenuItem menuItem) {
        String imageUrl = Optional.ofNullable(menuItem.getImageUrl())
                .map(fileName -> baseUrl + "/api/v1/menu-items/images/" + fileName)
                .orElse(null);
        return new MenuItemDTO(
                menuItem.getItemId(),
                menuItem.getName(),
                menuItem.getPrice(),
                menuItem.getStock(),
                imageUrl
        );
    }

}
