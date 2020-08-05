package com.chriswilty.spring.webflux;

import com.chriswilty.spring.webflux.model.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.reactive.server.WebTestClient.RequestHeadersSpec;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.Map;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WebfluxExamplesApplicationTests {

	@Autowired
	private WebTestClient webClient;

	@Test
	void jsonQuoteStream_Found() {
		testQuoteStreamWhenFound(1L, "json", MediaType.APPLICATION_STREAM_JSON);
	}

	@Test
	void jsonQuoteStream_IdNotFound() {
		// Id above 100 simulates a Not Found scenario.
		final RequestHeadersSpec<?> request = webClient.get()
				.uri("/products/{id}/quote/json", Map.of("id", 101L))
				.accept(MediaType.APPLICATION_STREAM_JSON);

		testQuoteStreamWhenNotFound(request);
	}

	@Test
	void sseQuoteStream_Found() {
		testQuoteStreamWhenFound(2L, "sse", MediaType.TEXT_EVENT_STREAM);
	}

	@Test
	void sseQuoteStream_IdNotFound() {
		// Id above 100 simulates a Not Found scenario.
		final RequestHeadersSpec<?> request = webClient.get()
				.uri("/products/{id}/quote/sse", Map.of("id", 102L))
				.accept(MediaType.TEXT_EVENT_STREAM);

		testQuoteStreamWhenNotFound(request);
	}

	@Test
	void getProduct_Found() {
		final long id = 2L;

		webClient.get()
				.uri("/products/{id}", Map.of("id", id))
				.accept(MediaType.APPLICATION_JSON)
			.exchange()
				.expectStatus().isOk()
				.expectBody(Product.class)
				.value(product -> {
					assertThat(product.getId()).isEqualTo(id);
					assertThat(product.getName()).isNotBlank();
					assertThat(product.getDescription()).isNotBlank();
					assertThat(product.getPrice()).isPositive();
				});
	}

	@Test
	void getProduct_NotFound() {
		webClient.get()
				.uri("/products/{id}", Map.of("id", 101))
				.accept(MediaType.APPLICATION_JSON)
			.exchange()
				.expectStatus().isNotFound();
	}

	@Test
	void getProducts() {
		webClient.get()
				.uri("/products")
				.accept(MediaType.APPLICATION_JSON)
			.exchange()
				.expectStatus().isOk()
				.expectBodyList(Product.class)
				.value(products -> {
					assertThat(products).isNotEmpty();
					products.forEach(product -> assertThat(product).isNotNull());
				});
	}

	@Test
	void getProducts_WithName() {
		final String name = "Thing";

		webClient.get()
				.uri("/products?name={name}", name)
				.accept(MediaType.APPLICATION_JSON)
			.exchange()
				.expectStatus().isOk()
				.expectBodyList(Product.class)
				.value(products -> products.forEach(product ->
						assertThat(product.getName()).containsIgnoringCase(name)
				));

	}

	private void testQuoteStreamWhenNotFound(final RequestHeadersSpec<?> request) {
		final Flux<Product> result = request.exchange()
				.expectStatus().isNotFound()
				.returnResult(Product.class)
				.getResponseBody();

		StepVerifier.create(result)
				.expectSubscription()
				.verifyComplete();
	}

	private void testQuoteStreamWhenFound(final long id, final String uriSuffix, final MediaType mediaType) {
		final Consumer<Product> productVerifier = product -> {
			assertThat(product.getId()).isEqualTo(id);
			assertThat(product.getPrice()).isPositive();
		};

		final Flux<Product> result = webClient
				.get()
				.uri("/products/{id}/quote/" + uriSuffix, Map.of("id", id))
				.accept(mediaType)
			.exchange()
				.expectStatus().isOk()
			.returnResult(Product.class)
				.getResponseBody()
				.take(3);

		StepVerifier.create(result)
				.expectSubscription()
				.assertNext(productVerifier)
				.assertNext(productVerifier)
				.assertNext(productVerifier)
				.verifyComplete();
	}

}
