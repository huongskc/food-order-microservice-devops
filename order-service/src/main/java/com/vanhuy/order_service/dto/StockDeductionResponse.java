package com.vanhuy.order_service.dto;

import java.math.BigDecimal;

public record StockDeductionResponse(Integer menuItemId, BigDecimal unitPrice) {
}
