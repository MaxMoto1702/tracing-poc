package com.unikoom.sleuth.instrument.sqs

import brave.Span
import io.awspring.cloud.messaging.listener.QueueMessageHandler
import org.springframework.messaging.Message
import org.springframework.messaging.converter.MessageConverter

class SqsQueueMessageHandler(
    private val handlerAdapter: TracingMethodMessageHandlerAdapter,
    messageConverters: List<MessageConverter?>?
) : QueueMessageHandler(messageConverters) {

    override fun handleMessage(message: Message<*>) {
        handlerAdapter
            .wrapMethodMessageHandler(
                message,
                { super.handleMessage(it) }) { span: Span, anyMessage: Message<*> ->
                messageSpanTagger(
                    span,
                    anyMessage
                )
            }
    }

    private fun messageSpanTagger(span: Span, message: Message<*>) {
//        span.remoteServiceName("com/unikoom/sleuth/instrument/sqs")
        span.remoteServiceName("aws")
        if (message.headers[LOGICAL_RESOURCE_ID] != null) {
            span.tag(
                "sqs.queue_url",
                message.headers[LOGICAL_RESOURCE_ID].toString()
            )
        }
    }
}
