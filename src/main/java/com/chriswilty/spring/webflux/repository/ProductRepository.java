package com.chriswilty.spring.webflux.repository;

import com.chriswilty.spring.webflux.model.Product;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "data")
public class ProductRepository {

    private List<Product> products;

    public Flux<Product> findAll() {
        return Flux.fromIterable(products);
    }

    public Mono<Product> findById(final long id) {
        return products.stream()
                .filter(p -> p.getId() == id)
                .findFirst()
                .map(Mono::just)
                .orElse(Mono.empty());
    }

    public Flux<Product> findByNameContaining(final String fragment) {
        final String lowercaseFragment = fragment.toLowerCase();
        return Flux.fromStream(
                products.stream().filter(product ->
                        product.getName().toLowerCase().contains(lowercaseFragment))
        );
    }
}
