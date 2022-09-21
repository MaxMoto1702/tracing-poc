package com.unikoom.tracingdemo.tripservice.config

import brave.baggage.BaggageField
import brave.baggage.CorrelationScopeConfig.SingleCorrelationField
import brave.context.slf4j.MDCScopeDecorator
import brave.propagation.CurrentTraceContext.ScopeDecorator
import io.netty.handler.logging.LogLevel
import kotlinx.coroutines.reactor.mono
import mu.KotlinLogging
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import reactor.netty.transport.logging.AdvancedByteBufFormat


@Configuration
class TripServiceApplicationConfig {
    val log = KotlinLogging.logger { }

    @Bean
    fun httpClient(): HttpClient {
        return HttpClient.create()
//            .wiretap(true)
            .wiretap(
                "reactor.netty.http.client.HttpClient",
                LogLevel.DEBUG,
                AdvancedByteBufFormat.TEXTUAL
            );
    }

    @Bean
    fun analyticClient(httpClient: HttpClient): WebClient {
        return WebClient
            .builder()
            .filters { exchangeFilterFunctions ->
                exchangeFilterFunctions.add(logRequest());
                exchangeFilterFunctions.add(logResponse());
            }
            .clientConnector(ReactorClientHttpConnector(httpClient))
            .baseUrl("http://localhost:8080/")
            .build()
    }

    @Bean
    fun cityClient(httpClient: HttpClient): WebClient {
        return WebClient
            .builder()
//            .filters { exchangeFilterFunctions ->
//                exchangeFilterFunctions.add(logRequest());
//                exchangeFilterFunctions.add(logResponse());
//            }
//            .clientConnector(ReactorClientHttpConnector(httpClient))
            .baseUrl("http://localhost:8081/")
            .build()
    }

    @Bean
    fun eventClient(httpClient: HttpClient): WebClient {
        return WebClient
            .builder()
//            .filters { exchangeFilterFunctions ->
//                exchangeFilterFunctions.add(logRequest());
//                exchangeFilterFunctions.add(logResponse());
//            }
//            .clientConnector(ReactorClientHttpConnector(httpClient))
            .baseUrl("http://localhost:8082/")
            .build()
    }

    @Bean
    fun placeClient(httpClient: HttpClient): WebClient {
        return WebClient
            .builder()
//            .filters { exchangeFilterFunctions ->
//                exchangeFilterFunctions.add(logRequest());
//                exchangeFilterFunctions.add(logResponse());
//            }
//            .clientConnector(ReactorClientHttpConnector(httpClient))
            .baseUrl("http://localhost:8083/")
            .build()
    }

    @Bean
    fun routeClient(httpClient: HttpClient): WebClient {
        return WebClient
            .builder()
//            .filters { exchangeFilterFunctions ->
//                exchangeFilterFunctions.add(logRequest());
//                exchangeFilterFunctions.add(logResponse());
//            }
//            .clientConnector(ReactorClientHttpConnector(httpClient))
            .baseUrl("http://localhost:8084/")
            .build()
    }

    @Bean
    fun tripBuilderClient(httpClient: HttpClient): WebClient {
        return WebClient
            .builder()
//            .filters { exchangeFilterFunctions ->
//                exchangeFilterFunctions.add(logRequest());
//                exchangeFilterFunctions.add(logResponse());
//            }
//            .clientConnector(ReactorClientHttpConnector(httpClient))
            .baseUrl("http://localhost:8085/")
            .build()
    }

    @Bean
    fun tripClient(httpClient: HttpClient): WebClient {
        return WebClient
            .builder()
//            .filters { exchangeFilterFunctions ->
//                exchangeFilterFunctions.add(logRequest());
//                exchangeFilterFunctions.add(logResponse());
//            }
//            .clientConnector(ReactorClientHttpConnector(httpClient))
            .baseUrl("http://localhost:8086/")
            .build()
    }

    @Bean
    fun tripIdField(): BaggageField {
        return BaggageField.create("trip-id")
    }

    @Bean
    fun mdcScopeDecorator(): ScopeDecorator {
        return MDCScopeDecorator.newBuilder()
            .clear()
            .add(
                SingleCorrelationField.newBuilder(tripIdField())
                    .flushOnUpdate()
                    .build()
            )
            .build()
    }
}


fun logRequest(): ExchangeFilterFunction {
    return ExchangeFilterFunction.ofRequestProcessor { request ->
        mono {
            KotlinLogging.logger { }.debug { "${request.method()} ${request.url()} headers ${request.headers()}" }
            request
        }
    }
}

fun logResponse(): ExchangeFilterFunction {
    return ExchangeFilterFunction.ofResponseProcessor { response ->
        mono {
            KotlinLogging.logger { }.debug { "${response.statusCode()} headers ${response.headers()}" }
            response
        }
    }
}
