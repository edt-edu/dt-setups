package dts.modelmanager.models

import com.influxdb.client.domain.WritePrecision
import com.influxdb.client.kotlin.InfluxDBClientKotlin
import com.influxdb.client.write.Point
import okio.withLock
import java.time.Instant
import java.util.concurrent.locks.ReentrantLock

class VacuumGripperModel (
    modelID: String,
    client: InfluxDBClientKotlin,
    island: String,
    val rotorMax: Int = 2960,
    val armMax: Int = 1696,
    val verticalMax: Int = 1692,
    signalNewData: (Set<String>, String) -> Unit,
    private val awaitNanosTimeout: Long = 10_000_000_000L,
) : AbstractModel(modelID, client = client, island = island, signalNewData = signalNewData) {

    private val lock = ReentrantLock()

    private val tags = mapOf("Machine Type" to "VacuumGripper", "Machine ID" to modelID, "Island" to island)

    /**
     * Update only while holding the lock.
     */
    var status = VacuumGripperStatus(
        "stopped", "reset", 0, true, null,
        "stopped", "reset", 0, true, null,
        "stopped", "reset", 0, true, null,
        false,
        false,
    )
    private var rotorAction: String? = null
    private var rotorTarget: Int? = null
    private val rotorSignal = lock.newCondition()
    private var armAction: String? = null
    private var armTarget: Int? = null
    private val armSignal = lock.newCondition()
    private var verticalAction: String? = null
    private var verticalTarget: Int? = null
    private val verticalSignal = lock.newCondition()
    private var compressor = false
    private var valve = false

    fun rotateTo(target: Int) {
        if (target < 0 || target > rotorMax)
            throw IllegalArgumentException()
        lock.withLock {
            rotorTarget = target
            rotorAction = "to=$target"
            rotorSignal.signalAll()
        }
        signalNewData(setOf("$modelID/control"), "rotate to=$target")
    }

    fun waitForRotation() {
        lock.withLock {
            while (rotorAction != null || rotorTarget != status.rotorTarget || status.rotorStatus == "targeting") {
                logger.info { "Waiting for rotation to $rotorTarget" }
                rotorSignal.awaitNanos(awaitNanosTimeout)
            }
        }
    }

    fun waitForRotationTo(target: Int) {
        rotateTo(target)
        waitForRotation()
    }

    fun armTo(target: Int) {
        if (target < 0 || target > armMax)
            throw IllegalArgumentException()
        lock.withLock {
            armTarget = target
            armAction = "to=$target"
            armSignal.signalAll()
        }
        signalNewData(setOf("$modelID/control"), "arm to=$target")
    }

    fun waitForArm() {
        lock.withLock {
            while (armAction != null || armTarget != status.armTarget || status.armStatus == "targeting") {
                logger.info { "Waiting for arm to $rotorTarget" }
                armSignal.awaitNanos(awaitNanosTimeout)
            }
        }
    }

    fun waitForArmTo(target: Int) {
        armTo(target)
        waitForArm()
    }

    fun verticalTo(target: Int) {
        if (target < 0 || target > verticalMax)
            throw IllegalArgumentException()
        lock.withLock {
            verticalTarget = target
            verticalAction = "to=$target"
            verticalSignal.signalAll()
        }
        signalNewData(setOf("$modelID/control"), "vertical to=$target")
    }

    fun waitForVertical() {
        lock.withLock {
            while (verticalAction != null || verticalTarget != status.verticalTarget || status.verticalStatus == "targeting") {
                logger.info { "Waiting for vertical to $rotorTarget" }
                verticalSignal.awaitNanos(awaitNanosTimeout)
            }
        }
    }

    fun waitForVerticalTo(target: Int) {
        verticalTo(target)
        waitForVertical()
    }

    fun waitForTargetReached(rotor: Int? = null, arm: Int? = null, vertical: Int? = null) {
        if (rotor != null) {
            rotateTo(rotor)
        }
        if (arm != null) {
            armTo(arm)
        }
        if (vertical != null) {
            verticalTo(vertical)
        }
        waitForRotation()
        waitForArm()
        waitForVertical()
    }

    fun grip() {
        compressor = true
        valve = true
        signalNewData(setOf("$modelID/control"), "grip")
    }

    fun release() {
        compressor = false
        valve = false
        signalNewData(setOf("$modelID/control"), "release")
    }

    /**
     * Signal if an action completed.
     *
     * The lock must be locked when this method is called.
     */
    private fun signalIfActionComplete() {
        if (rotorTarget != null && rotorTarget == status.rotorTarget && status.rotorStatus != "targeting") {
            logger.info { "Signal rotor target reached" }
            rotorAction = null
            rotorSignal.signalAll()
        }
        if (armTarget != null && armTarget == status.armTarget && status.armStatus != "targeting") {
            logger.info { "Signal arm target reached" }
            armAction = null
            armSignal.signalAll()
        }
        if (verticalTarget != null && verticalTarget == status.verticalTarget && status.verticalStatus != "targeting") {
            logger.info { "Signal vertical target reached" }
            verticalAction = null
            verticalSignal.signalAll()
        }
    }

//    init {
//        runBlocking {
//            for (property in modelProperties) {
//                modelData[property] = getLastValue(property)
//            }
//        }
//    }

    override fun getModel(): MutableMap<String, String> {
        return modelData
    }

    override fun getValue(id: String): String {
        return modelData[id] ?: ""
    }

    override suspend fun writeCommandHist(property: String, value: String) {
        val writeApi = client.getWriteKotlinApi()
        val point = Point.measurement("$modelID/command")
            .addTags(tags)
            .addField(property, value)
            .time(Instant.now().toEpochMilli(), WritePrecision.MS)
        writeApi.writePoint(point)
        logger.info { "Property $property of $modelID set to $value" }
    }

    override suspend fun writeStatus(properties: Map<String,Any?>) {
        val newStatus: VacuumGripperStatus
        try {
            newStatus = vcStatusFromMap(properties)
        } catch (e: Exception) {
            logger.error { "Invalid properties $properties for the model $modelID. Abort writing data to model" }
            return
        }
        lock.withLock {
            status = newStatus
            signalIfActionComplete()
        }
        val writeApi = client.getWriteKotlinApi()
        val point = Point.measurement("$modelID/status")
            .addTags(tags)
            .addFields(properties)
            .time(Instant.now().toEpochMilli(), WritePrecision.MS)
        writeApi.writePoint(point)
    }

    override fun getControl(): Map<String, Any?> {
        val control: MutableMap<String, Any?> = mutableMapOf("compressor" to compressor, "valve" to valve)
        if (rotorAction != null) {
            control["rotate"] = rotorAction
        }
        if (armAction != null) {
            control["arm"] = armAction
        }
        if (verticalAction != null) {
            control["vertical"] = verticalAction
        }
        return control
    }

}

data class VacuumGripperStatus(
    var rotorStatus: String,
    var rotorReason: String,
    var rotorPosition: Int,
    var rotorLimit: Boolean,
    var rotorTarget: Int?,

    var armStatus: String,
    var armReason: String,
    var armPosition: Int,
    var armLimit: Boolean,
    var armTarget: Int?,

    var verticalStatus: String,
    var verticalReason: String,
    var verticalPosition: Int,
    var verticalLimit: Boolean,
    var verticalTarget: Int?,

    var compressor: Boolean,
    var valve: Boolean,
) {
    fun setProperty(property: String, value: Any?) {
        when (property) {
            "rotor-status" -> rotorStatus = value as String
            "rotor-reason" -> rotorReason = value as String
            "rotor-position" -> rotorPosition = value as Int
            "limit-switch-right" -> rotorLimit = value as Boolean
            "rotor-target" -> rotorTarget = value as Int?

            "arm-status" -> armStatus = value as String
            "arm-reason" -> armReason = value as String
            "arm-position" -> armPosition = value as Int
            "limit-switch-in" -> armLimit = value as Boolean
            "arm-target" -> armTarget = value as Int?

            "vertical-status" -> verticalStatus = value as String
            "vertical-reason" -> verticalReason = value as String
            "vertical-position" -> verticalPosition = value as Int
            "limit-switch-up" -> verticalLimit = value as Boolean
            "vertical-target" -> verticalTarget = value as Int?

            "compressor" -> compressor = value as Boolean
            "valve" -> valve = value as Boolean
        }
    }
}

fun vcStatusFromMap(status: Map<String, Any?>): VacuumGripperStatus {
    return VacuumGripperStatus(
        status["rotor-status"] as String,
        status["rotor-reason"] as String,
        status["rotor-position"] as Int,
        status["limit-switch-right"] as Boolean,
        status["rotor-target"] as Int?,

        status["arm-status"] as String,
        status["arm-reason"] as String,
        status["arm-position"] as Int,
        status["limit-switch-in"] as Boolean,
        status["arm-target"] as Int?,

        status["vertical-status"] as String,
        status["vertical-reason"] as String,
        status["vertical-position"] as Int,
        status["limit-switch-up"] as Boolean,
        status["vertical-target"] as Int?,

        status["compressor"] as Boolean,
        status["valve"] as Boolean,
    )
}
