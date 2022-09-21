package com.unikoom.sleuth.instrument.sqs

import brave.propagation.Propagation
import org.apache.commons.logging.LogFactory
import org.springframework.messaging.support.MessageHeaderAccessor
import org.springframework.messaging.support.NativeMessageHeaderAccessor
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.StringUtils
import java.nio.charset.StandardCharsets

/**
 * This always sets native headers in defence of STOMP issues discussed [here](https://github.com/spring-cloud/spring-cloud-sleuth/issues/716#issuecomment-337523705).
 *
 * @author Marcin Grzejszczak
 */
internal enum class MessageHeaderPropagation : Propagation.Setter<MessageHeaderAccessor, String>,
    Propagation.Getter<MessageHeaderAccessor, String> {
    INSTANCE;

    override fun put(accessor: MessageHeaderAccessor, key: String, value: String) {
        try {
            doPut(accessor, key, value)
        } catch (ex: Exception) {
            if (log.isDebugEnabled) {
                log.debug(
                    "An exception happened when we tried to retrieve the [" + key
                            + "] from message", ex
                )
            }
        }
        val legacyKey = LEGACY_HEADER_MAPPING[key]
        legacyKey?.let { doPut(accessor, it, value) }
    }

    private fun doPut(accessor: MessageHeaderAccessor, key: String, value: String) {
        accessor.setHeader(key, value)
        if (accessor is NativeMessageHeaderAccessor) {
            ensureNativeHeadersAreMutable(accessor).setNativeHeader(key, value)
        } else {
            var nativeHeaders = accessor
                .getHeader(NativeMessageHeaderAccessor.NATIVE_HEADERS)
            if (nativeHeaders == null) {
                nativeHeaders = LinkedMultiValueMap<Any, Any>()
                accessor.setHeader(
                    NativeMessageHeaderAccessor.NATIVE_HEADERS,
                    nativeHeaders
                )
            }
            if (nativeHeaders is Map<*, *>) {
                @Suppress("UNCHECKED_CAST")
                val copy = toNativeHeaderMap(nativeHeaders as Map<String, List<String>>?)
                copy[key] = listOf(value)
                accessor.setHeader(NativeMessageHeaderAccessor.NATIVE_HEADERS, copy)
            }
        }
    }

    private fun toNativeHeaderMap(map: Map<String, List<String>>?): MutableMap<String, List<String>> {
        return if (map != null) LinkedMultiValueMap(map) else emptyMap<String, List<String>>().toMutableMap()
    }

    override fun get(accessor: MessageHeaderAccessor, key: String): String? {
        try {
            val value = doGet(accessor, key)
            if (StringUtils.hasText(value)) {
                return value
            }
        } catch (ex: Exception) {
            if (log.isDebugEnabled) {
                log.debug(
                    "An exception happened when we tried to retrieve the [" + key
                            + "] from message", ex
                )
            }
        }
        return legacyValue(accessor, key)
    }

    private fun legacyValue(accessor: MessageHeaderAccessor, key: String): String? {
        val legacyKey = LEGACY_HEADER_MAPPING[key]
        return legacyKey?.let { doGet(accessor, it) }
    }

    private fun doGet(accessor: MessageHeaderAccessor, key: String): String? {
        if (accessor is NativeMessageHeaderAccessor) {
            val result = accessor.getFirstNativeHeader(key)
            if (result != null) {
                return result
            }
        } else {
            val nativeHeaders = accessor
                .getHeader(NativeMessageHeaderAccessor.NATIVE_HEADERS)
            if (nativeHeaders is Map<*, *>) {
                val result = nativeHeaders[key]
                if (result is List<*> && !result.isEmpty()) {
                    return result[0].toString()
                }
            }
        }
        val result = accessor.getHeader(key)
        return if (result != null) {
            if (result is ByteArray) {
                String((result as ByteArray?)!!, StandardCharsets.UTF_8)
            } else result.toString()
        } else null
    }

    override fun toString(): String {
        return "MessageHeaderPropagation{}"
    }

    companion object {
        private val log = LogFactory.getLog(
            MessageHeaderPropagation::class.java
        )
        private val LEGACY_HEADER_MAPPING: MutableMap<String, String> = HashMap()
        private const val TRACE_ID_NAME = "X-B3-TraceId"
        private const val SPAN_ID_NAME = "X-B3-SpanId"
        private const val PARENT_SPAN_ID_NAME = "X-B3-ParentSpanId"
        private const val SAMPLED_NAME = "X-B3-Sampled"
        private const val FLAGS_NAME = "X-B3-Flags"

        init {
            LEGACY_HEADER_MAPPING[TRACE_ID_NAME] = "spanTraceId"
            LEGACY_HEADER_MAPPING[SPAN_ID_NAME] = "spanId"
            LEGACY_HEADER_MAPPING[PARENT_SPAN_ID_NAME] = "spanParentSpanId"
            LEGACY_HEADER_MAPPING[SAMPLED_NAME] = "spanSampled"
            LEGACY_HEADER_MAPPING[FLAGS_NAME] = "spanFlags"
        }

        @Suppress("unused")
        fun propagationHeaders(
            headers: Map<String, *>,
            propagationHeaders: List<String?>
        ): Map<String, *> {
            val headersToCopy: MutableMap<String, Any> = HashMap()
            for ((key, value) in headers) {
                if (propagationHeaders.contains(key)) {
                    headersToCopy[key] = value!!
                }
            }
            return headersToCopy
        }

        @Suppress("unused")
        fun removeAnyTraceHeaders(
            accessor: MessageHeaderAccessor,
            keysToRemove: List<String?>
        ) {
            for (keyToRemove in keysToRemove) {
                accessor.removeHeader(keyToRemove!!)
                if (accessor is NativeMessageHeaderAccessor) {
                    if (accessor.isMutable()) {
                        // 1184 native headers can be an immutable map
                        ensureNativeHeadersAreMutable(accessor)
                            .removeNativeHeader(keyToRemove)
                    }
                } else {
                    val nativeHeaders = accessor
                        .getHeader(NativeMessageHeaderAccessor.NATIVE_HEADERS)
                    if (nativeHeaders is MutableMap<*, *>) {
                        nativeHeaders.remove(keyToRemove)
                    }
                }
            }
        }

        /**
         * Since for some reason, the native headers sometimes are immutable even though the
         * accessor says that the headers are mutable, then we have to ensure their
         * mutability. We do so by first making a mutable copy of the native headers, then by
         * removing the native headers from the headers map and replacing them with a mutable
         * copy. Workaround for #1184
         *
         * @param nativeAccessor accessor containing (or not) native headers
         * @return modified accessor
         */
        private fun ensureNativeHeadersAreMutable(
            nativeAccessor: NativeMessageHeaderAccessor
        ): NativeMessageHeaderAccessor {
            var nativeHeaderMap: MutableMap<String, List<String>> = nativeAccessor.toNativeHeaderMap()
            nativeHeaderMap =
                if (nativeHeaderMap is LinkedMultiValueMap<*, *>) nativeHeaderMap else LinkedMultiValueMap(
                    nativeHeaderMap
                )
            nativeAccessor.removeHeader(NativeMessageHeaderAccessor.NATIVE_HEADERS)
            nativeAccessor.setHeader(
                NativeMessageHeaderAccessor.NATIVE_HEADERS,
                nativeHeaderMap
            )
            return nativeAccessor
        }
    }
}
