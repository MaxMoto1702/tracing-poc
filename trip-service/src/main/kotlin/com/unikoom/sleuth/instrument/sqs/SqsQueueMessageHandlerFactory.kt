package com.unikoom.sleuth.instrument.sqs

import io.awspring.cloud.messaging.config.QueueMessageHandlerFactory
import io.awspring.cloud.messaging.listener.QueueMessageHandler
import org.springframework.messaging.converter.MessageConverter
import org.springframework.util.CollectionUtils

class SqsQueueMessageHandlerFactory(
    private val handlerAdapter: TracingMethodMessageHandlerAdapter,
) :
    QueueMessageHandlerFactory() {

    override fun createQueueMessageHandler(): QueueMessageHandler {
        return if (CollectionUtils.isEmpty(getMessageConverters())) {
            SqsQueueMessageHandler(
                handlerAdapter,
                emptyList<MessageConverter>(),
            )
        } else SqsQueueMessageHandler(
            handlerAdapter,
            getMessageConverters(),
        )
    }
}
