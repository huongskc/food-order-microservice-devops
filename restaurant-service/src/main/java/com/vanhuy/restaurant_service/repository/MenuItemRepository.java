package com.vanhuy.restaurant_service.repository;

import com.vanhuy.restaurant_service.model.MenuItem;
import com.vanhuy.restaurant_service.model.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface MenuItemRepository extends JpaRepository<MenuItem , Integer> {
    List<MenuItem> findByRestaurant(Restaurant restaurant);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update MenuItem menuItem
            set menuItem.stock = menuItem.stock - :quantity
            where menuItem.itemId = :menuItemId
              and menuItem.stock >= :quantity
            """)
    int deductStock(@Param("menuItemId") Integer menuItemId, @Param("quantity") Integer quantity);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update MenuItem menuItem
            set menuItem.stock = menuItem.stock + :quantity
            where menuItem.itemId = :menuItemId
            """)
    int restoreStock(@Param("menuItemId") Integer menuItemId, @Param("quantity") Integer quantity);
}
