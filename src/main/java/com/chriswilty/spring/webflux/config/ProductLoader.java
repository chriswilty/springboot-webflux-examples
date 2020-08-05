package com.chriswilty.spring.webflux.config;

import com.chriswilty.spring.webflux.repository.ProductRepository;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(ProductRepository.class)
public class ProductLoader {
}
