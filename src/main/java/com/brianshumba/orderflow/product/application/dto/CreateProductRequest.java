package com.brianshumba.orderflow.product.application.dto;

import java.math.BigDecimal;

public record CreateProductRequest(
    String sku,
    String name,
    BigDecimal price
) {

}
