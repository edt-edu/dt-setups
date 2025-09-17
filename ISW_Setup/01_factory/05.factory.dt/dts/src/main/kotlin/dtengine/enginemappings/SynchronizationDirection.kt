package dts.dtengine.enginemappings

/**
 * This enum provides information to DTE about the direction of the synchronization
 */
enum class SynchronizationDirection {
    GATEWAY_TO_DB,
    DB_TO_GATEWAY,
    BOTH,

}