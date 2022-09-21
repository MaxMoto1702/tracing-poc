package com.unikoom.tracingdemo.tripservice.config

import brave.messaging.MessagingTracing
import brave.propagation.Propagation
import com.amazonaws.services.sqs.AmazonSQSAsync
import com.fasterxml.jackson.databind.ObjectMapper
import com.unikoom.sleuth.instrument.sqs.MessageHeaderPropagation
import com.unikoom.sleuth.instrument.sqs.SqsQueueMessageHandlerFactory
import com.unikoom.sleuth.instrument.sqs.SqsQueueMessagingTemplate
import com.unikoom.sleuth.instrument.sqs.TracingMethodMessageHandlerAdapter
import io.awspring.cloud.core.env.ResourceIdResolver
import org.springframework.beans.factory.ObjectProvider
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.cloud.sleuth.Tracer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.converter.MessageConverter
import org.springframework.messaging.support.MessageHeaderAccessor

@Configuration
class SleuthSqsConfig {

    @Bean("awsTraceMessagePropagationSetter")
    fun traceMessagePropagationSetter(): Propagation.Setter<MessageHeaderAccessor, String> {
        return MessageHeaderPropagation.INSTANCE
    }

    @Bean("awsTraceMessagePropagationGetter")
    fun traceMessagePropagationGetter(): Propagation.Getter<MessageHeaderAccessor, String> {
        return MessageHeaderPropagation.INSTANCE
    }

    @Bean
    fun tracingMethodMessageHandlerAdapter(
        messagingTracing: MessagingTracing,
        @Qualifier("awsTraceMessagePropagationGetter") traceMessagePropagationGetter: Propagation.Getter<MessageHeaderAccessor, String>
    ): TracingMethodMessageHandlerAdapter {
        return TracingMethodMessageHandlerAdapter(
            messagingTracing,
            traceMessagePropagationGetter,
        )
    }

    @Bean
    fun queueMessageHandlerFactory(
        tracingMethodMessageHandlerAdapter: TracingMethodMessageHandlerAdapter,
        messageConverters: MutableList<MessageConverter>,
        queueMessagingTemplate: SqsQueueMessagingTemplate,
    ): SqsQueueMessageHandlerFactory {
        val factory = SqsQueueMessageHandlerFactory(tracingMethodMessageHandlerAdapter)
        factory.messageConverters = messageConverters
        factory.setSendToMessagingTemplate(queueMessagingTemplate)
        return factory
    }

    @Bean
    fun queueMessagingTemplate(
        tracer: Tracer,
        amazonSqs: AmazonSQSAsync,
        resourceIdResolver: ObjectProvider<ResourceIdResolver>,
        objectMapper: ObjectMapper
    ): SqsQueueMessagingTemplate {
        return SqsQueueMessagingTemplate(
            tracer,
            amazonSqs,
            resourceIdResolver.ifAvailable,
            objectMapper
        )
    }
}
