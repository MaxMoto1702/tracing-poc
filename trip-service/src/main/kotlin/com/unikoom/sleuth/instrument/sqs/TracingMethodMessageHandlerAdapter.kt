package com.unikoom.sleuth.instrument.sqs

import brave.Span
import brave.Tracer
import brave.Tracing
import brave.messaging.MessagingTracing
import brave.propagation.Propagation
import brave.propagation.TraceContext
import brave.propagation.TraceContextOrSamplingFlags
import org.springframework.messaging.Message
import org.springframework.messaging.MessageHandler
import org.springframework.messaging.support.MessageHeaderAccessor
import java.util.function.BiConsumer

/**
 * Adds tracing extraction to an instance of
 * [org.springframework.messaging.handler.invocation.AbstractMethodMessageHandler]
 * in a reusable way. When sub-classing a provider specific class of that type you would
 * wrap the <pre>super.handleMessage(...)</pre> call with a call to this. See
 * [SqsQueueMessageHandler]
 * for an example.
 *
 * This implementation also allows for supplying a [java.util.function.BiConsumer]
 * instance that can be used to add queue specific tags and modifications to the span.
 *
 * @author Brian Devins-Suresh
 */
class TracingMethodMessageHandlerAdapter(
    messagingTracing: MessagingTracing,
    getter: Propagation.Getter<MessageHeaderAccessor, String>
) {
    private val tracing: Tracing
    private val tracer: Tracer
    private val extractor: TraceContext.Extractor<MessageConsumerRequest>
    private val getter: Propagation.Getter<MessageHeaderAccessor, String>

    init {
        tracing = messagingTracing.tracing()
        tracer = tracing.tracer()
        extractor = tracing.propagation().extractor(MessageConsumerRequest.GETTER)
        this.getter = getter
    }

    fun wrapMethodMessageHandler(
        message: Message<*>,
        messageHandler: MessageHandler,
        messageSpanTagger: BiConsumer<Span, Message<*>>,
    ) {
        val request = MessageConsumerRequest(message, getter)
        val extracted = extractAndClearHeaders(request)
        val consumerSpan = tracer.nextSpan(extracted)
        val listenerSpan = tracer.newChild(consumerSpan.context())
        if (!consumerSpan.isNoop) {
            consumerSpan.name("next-message").kind(Span.Kind.CONSUMER)
            messageSpanTagger.accept(consumerSpan, message)

            // incur timestamp overhead only once
            val timestamp = tracing.clock(consumerSpan.context())
                .currentTimeMicroseconds()
            consumerSpan.start(timestamp)
            val consumerFinish = timestamp + 1L // save a clock reading
            consumerSpan.finish(consumerFinish)

            // not using scoped span as we want to start with a pre-configured time
            listenerSpan.name("on-message").start(consumerFinish)
        }
        try {
            tracer.withSpanInScope(listenerSpan).use { ws -> messageHandler.handleMessage(message) }
        } catch (t: Throwable) {
            listenerSpan.error(t)
            throw t
        } finally {
            listenerSpan.finish()
        }
    }

    private fun extractAndClearHeaders(
        request: MessageConsumerRequest
    ): TraceContextOrSamplingFlags {
        val extracted = extractor.extract(request)
        for (propagationKey in tracing.propagation().keys()) {
            request.removeHeader(propagationKey)
        }
        return extracted
    }
}
