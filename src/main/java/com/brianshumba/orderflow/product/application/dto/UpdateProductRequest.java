package com.brianshumba.orderflow.product.application.dto;

import java.math.BigDecimal;

public record UpdateProductRequest(
    String name,
    BigDecimal price
) {

}
