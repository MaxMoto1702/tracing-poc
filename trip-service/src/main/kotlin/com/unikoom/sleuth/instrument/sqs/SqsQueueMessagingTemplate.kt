package com.unikoom.sleuth.instrument.sqs

import com.amazonaws.services.sqs.AmazonSQSAsync
import com.fasterxml.jackson.databind.ObjectMapper
import io.awspring.cloud.core.env.ResourceIdResolver
import io.awspring.cloud.messaging.core.QueueMessagingTemplate
import org.springframework.cloud.sleuth.Span
import org.springframework.cloud.sleuth.Tracer
import org.springframework.messaging.Message
import org.springframework.messaging.core.MessagePostProcessor

class SqsQueueMessagingTemplate(
    private val tracer: Tracer,
    amazonSqs: AmazonSQSAsync,
    resourceIdResolver: ResourceIdResolver?,
    objectMapper: ObjectMapper,
) : QueueMessagingTemplate(amazonSqs, resourceIdResolver, objectMapper) {
    override fun doConvert(
        payload: Any,
        headers: MutableMap<String, Any>?,
        postProcessor: MessagePostProcessor?,
    ): Message<*> {
        val span = tracer.spanBuilder()
            .name("send message")
            .kind(Span.Kind.PRODUCER)
            .remoteServiceName("aws")
            .start()
//        val span = tracer.nextSpan()
//            .remoteServiceName("aws")
//            .start()
        return try {
            tracer.withSpan(span).use {
                // ...
                // You can tag a span
//                newSpan.tag("taxValue", taxValue)
                // ...
                val message = super.doConvert(
                    payload,
                    (headers ?: mutableMapOf()) + mutableMapOf(
                        "X-B3-TraceId" to span?.context()?.traceId(),
//                "X-B3-ParentSpanId" to span?.context()?.spanId(),
                        "X-B3-SpanId" to span.context().spanId(),
                        "X-B3-Sampled" to "1",
                    ),
                    postProcessor
                )
                // You can log an event on a span
                span.event("messageSent")
                message
            }
        } finally {
            // Once done remember to end the span. This will allow collecting
            // the span to send it to a distributed tracing system e.g. Zipkin
            span.end()
        }
    }
}
