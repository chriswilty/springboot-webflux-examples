package com.chriswilty.spring.webflux.service;

import com.chriswilty.spring.webflux.model.Product;
import com.chriswilty.spring.webflux.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Random;
import java.util.stream.Stream;

@Service
public class ProductService {

    private final Random random = new Random();

    private final ProductRepository productRepository;

    @Autowired
    public ProductService(final ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Flux<Product> getProducts() {
        return productRepository.findAll();
    }

    public Mono<Product> getProduct(final long id) {
        return productRepository.findById(id);
    }

    public Flux<Product> getProductsByName(@NonNull final String name) {
        return productRepository.findByNameContaining(name);
    }

    public Flux<Product> getPrice(final long id) {
        return productRepository.findById(id).flatMapMany(product ->
                Flux.generate(
                        product::getPrice,
                        (state, sink) -> {
                            final BigDecimal nextPrice = state.add(wobble(product.getPrice()));
                            sink.next(product.toBuilder().price(nextPrice).build());
                            return nextPrice;
                        }
                )
        );
    }

    private BigDecimal wobble(final BigDecimal basePrice) {
        final int basePounds = basePrice
                .divide(BigDecimal.ONE, 0, RoundingMode.UP)
                .toBigInteger()
                .intValue();

        return new BigDecimal(random.nextInt(basePounds) - (basePounds / 2))
                .divide(new BigDecimal(100), 2, RoundingMode.UNNECESSARY);
    }
}
