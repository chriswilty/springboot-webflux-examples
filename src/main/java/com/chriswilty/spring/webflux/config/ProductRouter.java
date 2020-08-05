package com.chriswilty.spring.webflux.config;

import com.chriswilty.spring.webflux.web.ProductHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.accept;

@Configuration
public class ProductRouter {

    @Bean
    public RouterFunction<ServerResponse> productRoutes(final ProductHandler productHandler) {
        return RouterFunctions.route()
                .path("/products", builder -> builder
                        // Intended for inter-service communication:
                        .GET("/{id}/quote/json",
                                accept(MediaType.APPLICATION_STREAM_JSON),
                                productHandler::getQuoteJsonStream
                        )
                        // Intended for server-webclient communication:
                        .GET("/{id}/quote/sse",
                                accept(MediaType.TEXT_EVENT_STREAM),
                                productHandler::getQuoteEventStream
                        )
                        .GET("/{id}",
                                accept(MediaType.APPLICATION_JSON),
                                productHandler::getProduct
                        )
                        .GET("",
                                accept(MediaType.APPLICATION_JSON),
                                productHandler::getProducts
                        )
                ).build();
    }
}
