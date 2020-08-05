# SpringBoot WebFlux / Reactor examples

Playing around with Spring Reactor, in particular streaming endpoints and websockets.

I have opted for the _functional_ style of defining endpoints using RouterFunction, rather than
Controller-based endpoints, simply because I wanted to give it a try. The jury's still out on
whether I like this style or not ...

Note that this project is Maven-based; I _still_ prefer Maven over Gradle 🤫

## Build and run tests

```
./mvnw clean verify
```

## Run the service

```
./mvnw spring-boot:run
```

## API

### Requests

* `GET /products[?name=]`  
  Gets a list of products, optionally filtered by name containing (case-insensitively).

* `GET /products/{id}`  
  Gets a product using its numeric id.

* `GET /products/{id}/quote/json`  
  Gets a stream of prices for a product, as a JSON stream.
  
  JSON streams are intended for server-to-server communication. For consumption by web browsers, we
  use instead _Server-Sent Events_ (see below).
  
  Note that content-type is currently "application/stream+json", however, Spring 5.3 will support
  the more universal "application/x-ndjson"
  ([line-delimited JSON](https://github.com/ndjson/ndjson-spec)) and deprecate the former.

* `GET /products/{id}/quote/sse`  
  Gets a stream of prices for a product, as a
  [Server-Sent Event](https://html.spec.whatwg.org/multipage/server-sent-events.html) stream.
  
  Server-Sent Event streams are intended for _push_ communication from a Server to a Web Client.
  They are useful when two-way messages are not required; they are more lightweight than e.g.
  WebSockets.

### Responses

* `200 Ok` with Product(s) in body:

```
{
  "id": number,
  "name": string,
  "description": string,
  "price": number
}
```

* `404 NotFound` if a Product with given ID was not found

* `400 BadRequest` for malformed inputs, e.g. ID is non-numeric
