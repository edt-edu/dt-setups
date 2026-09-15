package dts.mqtt


/**
 * Get a list of all wildcard levels in a topic compared to a subscription.
 *
 * For the '#' wildcard, multilevel matches are separated by the level separator '/'.
 * The topics should match.
 *
 * @param subscription The subscription topic with wildcards
 * @param topic The topic string
 * @return The wildcard matches in order
 */
fun getWildcardTopics(subscription: String, topic: String): List<String> {
    val subs = subscription.split("/")
    val topics = topic.split("/")
    if (subs.size > topics.size) {
        throw Exception("Topic does not match")
    }
    val wildcards: MutableList<String> = mutableListOf()
    for (i in subs.indices) {
        val sub = subs[i]
        if (sub == "+") {
            wildcards.add(topics[i])
        } else if (sub == "#") {
            assert(i == subs.size - 1)
            wildcards.add(topics.subList(i, topics.size).joinToString("/"))
        }
    }
    return wildcards
}

/**
 * Replace topic wildcard with the given replacements.
 *
 * The topics should match.
 *
 * @param subscription The subscription topic with wildcards
 * @param wildcards The wildcards to replace in order
 * @return The topic with all wildcards replaced
 */
fun setWildcardTopics(subscription: String, wildcards: List<String>): String {
    val subs = subscription.split("/")
    val topics: MutableList<String> = mutableListOf()
    var windex = 0
    for (i in subs.indices) {
        var topic = subs[i]
        if (topic == "+") {
            topic = wildcards[windex++]
        } else if (topic == "#") {
            assert(i == subs.size - 1)
            assert(windex == wildcards.size - 1)
            topic = wildcards[windex++]
        }
        topics.add(topic)
    }
    return topics.joinToString("/")
}
