package com.chriswilty.spring.webflux.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import org.springframework.boot.context.properties.ConstructorBinding;

import java.math.BigDecimal;

@Value
@Builder(toBuilder = true)
@AllArgsConstructor
@ConstructorBinding
public class Product {
    long id;
    String name;
    String description;
    BigDecimal price;
}
