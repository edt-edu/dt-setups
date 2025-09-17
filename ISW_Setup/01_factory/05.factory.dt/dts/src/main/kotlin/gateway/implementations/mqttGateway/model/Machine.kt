package dts.gateway.implementations.mqttGateway.model

import dts.gateway.implementations.mqttGateway.status.MachineStatus

/**
 * Represents a machine on an island. The machine name follows the pattern
 * `<island>-<number>-<type>` (e.g. `1-2-clawGripper`)
 */
data class Machine(
    val name: String,
    val island: Int,
    val number: Int,
    val type: String,
    var status: MachineStatus? = null
) {
    companion object {
        private val NAME_REGEX = Regex("""(\d+)-(\d+)-(.+)""")

        /**
         * Parses the given [name] according to the standard machine naming
         * convention `I-M-TYPE`
         */
        fun fromName(name: String): Machine {
            val match = NAME_REGEX.matchEntire(name)
            return if (match != null) {
                val (island, number, type) = match.destructured
                Machine(name, island.toInt(), number.toInt(), type)
            } else {
                Machine(name, -1, -1, name)
            }
        }
    }
}
