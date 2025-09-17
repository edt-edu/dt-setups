package dts.dtsystem.datastructures

import java.security.Timestamp
import java.util.UUID

class PayLoad() {
    lateinit var timestamp: Timestamp
    lateinit var sourceID: UUID
    lateinit var operation: Functionality
    lateinit var content: Map<String, Any?>
}