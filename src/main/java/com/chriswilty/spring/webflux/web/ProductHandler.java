package com.chriswilty.spring.webflux.web;

import com.chriswilty.spring.webflux.model.Product;
import com.chriswilty.spring.webflux.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Optional;

@Slf4j
@Component
public class ProductHandler {
    private static final Mono<ServerResponse> NOT_FOUND = ServerResponse.notFound().build();

    private static Mono<ServerResponse> badRequest(final String message) {
        return ServerResponse.badRequest().bodyValue(message);
    }

    private final ProductService productService;

    @Autowired
    public ProductHandler(ProductService productService) {
        this.productService = productService;
    }

    public Mono<ServerResponse> getProducts(final ServerRequest request) {
        final Optional<String> name = request.queryParam("name");
        return ServerResponse
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(
                        name.map(productService::getProductsByName).orElseGet(productService::getProducts),
                        Product.class
                );
    }

    // NOTE: application/stream+json is due to be deprecated in Spring 5.3 in favour of application/x-ndjson
    public Mono<ServerResponse> getQuoteJsonStream(final ServerRequest request) {
        return getQuoteStream(request, MediaType.APPLICATION_STREAM_JSON);
    }

    public Mono<ServerResponse> getQuoteEventStream(final ServerRequest request) {
        return getQuoteStream(request, MediaType.TEXT_EVENT_STREAM);
    }

    public Mono<ServerResponse> getProduct(final ServerRequest request) {
        try {
            final long id = Long.parseLong(request.pathVariable("id"));
            return productService.getProduct(id)
                    .flatMap(product -> ServerResponse
                            .ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(product)
                    )
                    .switchIfEmpty(NOT_FOUND);
        } catch (NumberFormatException e) {
            return ServerResponse.badRequest().bodyValue("Product id must be numeric");
        }
    }

    private Mono<ServerResponse> getQuoteStream(final ServerRequest request, final MediaType mediaType) {
        try {
            final long id = Long.parseLong(request.pathVariable("id"));
            final Flux<Product> priceFlux = productService.getPrice(id)
                    .delayElements(Duration.ofMillis(1000))
                    .log();

            return priceFlux.hasElements().flatMap(exists -> exists
                    ? ServerResponse.ok().contentType(mediaType).body(priceFlux, Product.class)
                    : NOT_FOUND
            );
        } catch (NumberFormatException e) {
            return badRequest("ID must be numeric");
        }
    }

}
