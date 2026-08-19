package com.vanhuy.order_service.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class OrderRequest {
    private Integer userId;
    private String recipientName;
    private String contactEmail;
    private String shippingAddress;
    private String contactPhone;

    @NotEmpty(message = "items must not be empty")
    private List<@Valid OrderItemRequest> items;
}
