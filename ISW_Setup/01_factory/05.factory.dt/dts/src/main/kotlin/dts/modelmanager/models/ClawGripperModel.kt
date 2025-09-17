package dts.modelmanager.models

import com.influxdb.client.domain.WritePrecision
import com.influxdb.client.kotlin.InfluxDBClientKotlin
import com.influxdb.client.write.Point
import okio.withLock
import java.time.Instant
import java.util.concurrent.locks.ReentrantLock

class ClawGripperModel (
    modelID: String,
    client: InfluxDBClientKotlin,
    island: String,
    val rotorMax: Int = 4000,
    val armMax: Int = 81,
    val verticalMax: Int = 2600,
    val clawMax: Int = 20,
    signalNewData: (Set<String>, String) -> Unit,
    private val awaitNanosTimeout: Long = 10_000_000_000L,
) : AbstractModel(modelID, client = client, island = island, signalNewData = signalNewData) {
    
    private val lock = ReentrantLock()

    private val tags = mapOf("Machine Type" to "VacuumGripper", "Machine ID" to modelID, "Island" to island)

    /**
     * Update only while holding the lock.
     */
    var status = ClawGripperStatus(
        "stopped", "reset", 0, true, null,
        "stopped", "reset", 0, true,
        "stopped", "reset", 0, true, null,
        "stopped", "reset", 0, true,
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
    private var clawAction: String? = null
    private var clawTarget: Int? = null
    private val clawSignal = lock.newCondition()

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
            while (armAction != null) {
                logger.info { "Waiting for arm to $armAction" }
                armSignal.awaitNanos(awaitNanosTimeout)
            }
        }
    }

    fun waitForArmTo(target: Int) {
        armTo(target)
        waitForArm()
    }

    fun resetArm() {
        lock.withLock {
            armTarget = null
            armAction = "reset"
            armSignal.signalAll()
        }
        signalNewData(setOf("$modelID/control"), "reset arm")
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

    fun grip(target: Int) {
        if (target < 0 || target > clawMax)
            throw IllegalArgumentException()
        lock.withLock {
            if (target <= status.clawPosition)
                return
            clawTarget = target
            clawAction = "to=$target"
            clawSignal.signalAll()
        }
        signalNewData(setOf("$modelID/control"), "grip")
    }

    fun release() {
        lock.withLock {
            clawTarget = null
            clawAction = "reset"
            clawSignal.signalAll()
        }
        signalNewData(setOf("$modelID/control"), "release")
    }

    fun waitForClaw() {
        lock.withLock {
            while (clawAction != null) {
                logger.info { "Waiting for claw to $clawAction" }
                clawSignal.awaitNanos(awaitNanosTimeout)
            }
        }
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
        val armActionDone = armAction != null && (
                (armTarget != null && armTarget!! <= status.armPosition)
                        || (armTarget == null && status.armStatus != "resetting")
                )
        if (armActionDone) {
            logger.info { "Signal arm target reached" }
            armAction = null
            armSignal.signalAll()
        }
        if (verticalTarget != null && verticalTarget == status.verticalTarget && status.verticalStatus != "targeting") {
            logger.info { "Signal vertical target reached" }
            verticalAction = null
            verticalSignal.signalAll()
        }
        val clawActionDone = clawAction != null && (
                (clawTarget != null && clawTarget!! <= status.clawPosition)
                        || (clawTarget == null && status.clawStatus != "resetting")
                )
        if (clawActionDone) {
            logger.info { "Signal claw target reached" }
            clawAction = null
            clawSignal.signalAll()
        }
    }

    override fun getModel(): MutableMap<String, String> {
        return modelData
    }

    override fun getValue(id: String): String {
        return modelData[id] ?: ""
    }

    override suspend fun writeCommandHist(property: String, value: String) {
        val writeApi = client.getWriteKotlinApi()
        val point = Point.measurement("$modelID/command")
            .addTags(mutableMapOf("Machine Type" to "Claw Gripper", "Machine ID" to modelID,"Island" to island))
            .addField(property,value).time(Instant.now().toEpochMilli(), WritePrecision.MS)
        writeApi.writePoint(point)
        logger.info { "Property $property of $modelID set to $value" }
    }

    override suspend fun writeStatus(properties: Map<String,Any?>) {
        val newStatus: ClawGripperStatus
        try {
            newStatus = cgStatusFromMap(properties)
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
        val control: MutableMap<String, Any?> = mutableMapOf()
        if (rotorAction != null) {
            control["rotate"] = rotorAction
        }
        if (armAction != null) {
            control["arm"] = armAction
        }
        if (verticalAction != null) {
            control["vertical"] = verticalAction
        }
        if (clawAction != null) {
            control["claw"] = clawAction
        }
        return control
    }

}

data class ClawGripperStatus(
    var rotorStatus: String,
    var rotorReason: String,
    var rotorPosition: Int,
    var rotorLimit: Boolean,
    var rotorTarget: Int?,

    var armStatus: String,
    var armReason: String,
    var armPosition: Int,
    var armLimit: Boolean,

    var verticalStatus: String,
    var verticalReason: String,
    var verticalPosition: Int,
    var verticalLimit: Boolean,
    var verticalTarget: Int?,

    var clawStatus: String,
    var clawReason: String,
    var clawPosition: Int,
    var clawLimit: Boolean,
)

fun cgStatusFromMap(status: Map<String, Any?>): ClawGripperStatus {
    return ClawGripperStatus(
        status["rotor-status"] as String,
        status["rotor-reason"] as String,
        status["rotor-position"] as Int,
        status["limit-switch-right"] as Boolean,
        status["rotor-target"] as Int?,

        status["arm-status"] as String,
        status["arm-reason"] as String,
        status["arm-position"] as Int,
        status["limit-switch-in"] as Boolean,

        status["vertical-status"] as String,
        status["vertical-reason"] as String,
        status["vertical-position"] as Int,
        status["limit-switch-up"] as Boolean,
        status["vertical-target"] as Int?,

        status["claw-status"] as String,
        status["claw-reason"] as String,
        status["claw-position"] as Int,
        status["limit-switch-open"] as Boolean,
    )
}