package dts.mqtt

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

class TopicParserTest {

    companion object {
        @JvmStatic fun wildcardMatches(): List<Arguments> = listOf(
            Arguments.of("a/b/c/d", "a/b/c/d", arrayOf<String>()),

            Arguments.of("+/b/c/d", "a/b/c/d", arrayOf("a")),
            Arguments.of("a/+/+/d", "a/b/c/d", arrayOf("b", "c")),
            Arguments.of("a/+/c/d", "a/b/c/d", arrayOf("b")),
            Arguments.of("+/+/+/+", "a/b/c/d", arrayOf("a", "b", "c", "d")),

            Arguments.of("#", "a/b/c/d", arrayOf("a/b/c/d")),
            Arguments.of("a/#", "a/b/c/d", arrayOf("b/c/d")),
            Arguments.of("a/b/#", "a/b/c/d", arrayOf("c/d")),
            Arguments.of("a/b/c/#", "a/b/c/d", arrayOf("d")),

            Arguments.of("+/b/c/#", "a/b/c/d", arrayOf("a", "d")),

            Arguments.of("a/+/topic", "a//topic", arrayOf("")),
            Arguments.of("#", "/a/topic", arrayOf("/a/topic")),
            Arguments.of("/#", "/a/topic", arrayOf("a/topic")),
            Arguments.of("a/topic/+", "a/topic/", arrayOf("")),
            Arguments.of("a/topic/#", "a/topic/", arrayOf(""))
        )
    }

    @ParameterizedTest
    @MethodSource("wildcardMatches")
    fun getWildcardTopic(subscription: String, topic: String, expected: Array<String>) {
        val wildcards = getWildcardTopics(subscription, topic)

        assertArrayEquals(expected, wildcards.toTypedArray())
    }

    @ParameterizedTest
    @MethodSource("wildcardMatches")
    fun setWildcardTopic(subscription: String, expected: String, wildcards: Array<String>) {
        val topic = setWildcardTopics(subscription, wildcards.toList())

        assertEquals(expected, topic)
    }
}