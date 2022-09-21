package com.unikoom.sleuth.instrument.sqs

import brave.Span
import brave.messaging.ConsumerRequest
import brave.propagation.Propagation
import org.springframework.messaging.Message
import org.springframework.messaging.support.MessageHeaderAccessor

class MessageConsumerRequest(
    val delegate: Message<*>,
    getter: Propagation.Getter<MessageHeaderAccessor, String>
) : ConsumerRequest() {
    val mutableHeaders: MessageHeaderAccessor
    val getter: Propagation.Getter<MessageHeaderAccessor, String>

    init {
        mutableHeaders = mutableAccessor(delegate)
        this.getter = getter
    }

    private fun mutableAccessor(message: Message<*>): MessageHeaderAccessor {
        val accessor = MessageHeaderAccessor.getAccessor(
            message,
            MessageHeaderAccessor::class.java
        )
        return if (accessor != null && accessor.isMutable) {
            accessor
        } else MessageHeaderAccessor.getMutableAccessor(delegate)
    }

    override fun spanKind(): Span.Kind {
        return Span.Kind.CONSUMER
    }

    override fun unwrap(): Any {
        return delegate
    }

    override fun operation(): String {
        return "receive"
    }

    override fun channelKind(): String {
        return "queue"
    }

    override fun channelName(): String {
        return delegate.headers[LOGICAL_RESOURCE_ID].toString()
    }

    fun getHeader(name: String): String? {
        return getter[mutableHeaders, name]
    }

    fun removeHeader(name: String?) {
        mutableHeaders.removeHeader(name!!)
    }

    companion object {
        val GETTER: Propagation.Getter<MessageConsumerRequest, String> =
            object : Propagation.Getter<MessageConsumerRequest, String> {
                override fun get(request: MessageConsumerRequest, name: String): String? {
                    return request.getHeader(name)
                }

                override fun toString(): String {
                    return "MessageConsumerRequest::getHeader"
                }
            }
    }
}
