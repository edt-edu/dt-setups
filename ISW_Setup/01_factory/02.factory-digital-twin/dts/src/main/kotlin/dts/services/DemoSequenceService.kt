package dts.services

import dts.DigitalTwinEngine
import dts.gateway.AbstractGateway
import dts.logger
import dts.modelmanager.ModelManager
import dts.modelmanager.models.ClawGripperModel
import dts.modelmanager.models.VacuumGripperModel
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class DemoSequenceService(val mm: ModelManager, val gateway: AbstractGateway) : AbstractServiceConnection(serviceID = "DemoSequenceService") {
    val lock = ReentrantLock()
    val condition = lock.newCondition()

    private val vg16 = mm.returnAbstractModel("1-6-vacuumGripper") as VacuumGripperModel
    private val cg12 = mm.returnAbstractModel("1-2-clawGripper") as ClawGripperModel
    private val cg18 = mm.returnAbstractModel("1-8-clawGripper") as ClawGripperModel

    private val runSortSequence = Thread {
        gateway.setValue("1-4-sortingLine/control", mapOf("action" to "move to ejectors", "color" to "white"))
        logger.info { "Waiting for sorting line" }
        waitForProperty(gateway, "1-4-sortingLine") {p -> p["state"] == "package waiting at ejectors"}

        gateway.setValue("1-4-sortingLine/control", mapOf("action" to "sort", "color" to "white"))
        logger.info { "Waiting for sorting line" }
        waitForProperty(gateway, "1-4-sortingLine") {p -> p["state"] == "ready"}

        logger.info { "fertig" }
    }

    private val runSequenceReverse = Thread {
        /**
        gateway.setValue("1-3-conveyor/control", mapOf("motor" to "right"))
        logger.info { "Waiting for conveyor movement" }
        waitForProperty(gateway, "1-3-conveyor") {p -> p["reason"] == "position-limit" && p["rightSensor"] == false}

        gateway.setValue("1-6-vacuumGripper/control", mapOf("vertical" to "to=10"))
        logger.info { "Waiting for VC vertical" }
        waitForProperty(gateway, "1-6-vacuumGripper") {p -> p["vertical-reason"] == "target-reached" && p["vertical-target"] == 10}

        gateway.setValue("1-6-vacuumGripper/control", mapOf("arm" to "to=0"))
        logger.info { "Waiting for VC arm" }
        waitForProperty(gateway, "1-6-vacuumGripper") {p -> p["arm-reason"] == "target-reached" && p["arm-target"] == 0}

        gateway.setValue("1-6-vacuumGripper/control", mapOf("rotate" to "to=400"))
        logger.info { "Waiting for VC rotation" }
        waitForProperty(gateway, "1-6-vacuumGripper") {p -> p["rotor-reason"] == "target-reached" && p["rotor-target"] == 400}

        gateway.setValue("1-6-vacuumGripper/control", mapOf("vertical" to "to=1200"))
        logger.info { "Waiting for VC rotation" }
        waitForProperty(gateway, "1-6-vacuumGripper") {p -> p["vertical-reason"] == "target-reached" && p["vertical-target"] == 1200}

        gateway.setValue("1-6-vacuumGripper/control", mapOf("valve" to true, "compressor" to true))
        Thread.sleep(1000)

        gateway.setValue("1-6-vacuumGripper/control", mapOf("vertical" to "to=10"))
        logger.info { "Waiting for VC vertical" }
        waitForProperty(gateway, "1-6-vacuumGripper") {p -> p["vertical-reason"] == "target-reached" && p["vertical-target"] == 10}

        gateway.setValue("1-6-vacuumGripper/control", mapOf("rotate" to "to=1430"))
        logger.info { "Waiting for VC rotation" }
        waitForProperty(gateway, "1-6-vacuumGripper") {p -> p["rotor-reason"] == "target-reached" && p["rotor-target"] == 1430}

        gateway.setValue("1-6-vacuumGripper/control", mapOf("arm" to "to=1380"))
        logger.info { "Waiting for VC arm" }
        waitForProperty(gateway, "1-6-vacuumGripper") {p -> p["arm-reason"] == "target-reached" && p["arm-target"] == 1380}

        gateway.setValue("1-6-vacuumGripper/control", mapOf("vertical" to "to=330"))
        logger.info { "Waiting for VC vertical" }
        waitForProperty(gateway, "1-6-vacuumGripper") {p -> p["vertical-reason"] == "target-reached" && p["vertical-target"] == 330}

        gateway.setValue("1-6-vacuumGripper/control", mapOf("valve" to false, "compressor" to false))
        Thread.sleep(1000)

        gateway.setValue("1-6-vacuumGripper/control", mapOf("vertical" to "to=10"))
        logger.info { "Waiting for VC vertical" }
        waitForProperty(gateway, "1-6-vacuumGripper") {p -> p["vertical-reason"] == "target-reached" && p["vertical-target"] == 10}*/

        gateway.setValue("1-5-warehouse/control", mapOf("action" to "store", "column" to 0, "row" to 0))
        logger.info { "Waiting for warehouse operation to finish" }
        waitForProperty(gateway, "1-5-warehouse") {p -> p["state"] == "ready"}

        logger.info { "fertig" }
    }

    private val runSequence = Thread {
        do {
            logger.info { "Waiting for machine initialisation" }
            Thread.sleep(1000)
        } while (!mm.allModelsUp())
        logger.info { "Resetting machines" }
        reset()
        logger.info { "Starting sequence" }

        sequenceRetrieveFromWarehouse(0, 0)

        sortSequence("blue")

        getFromSortToIndexedLine("blue")

        processOnIndexedLine()

        storeFromIndexedLineInWarehouse(0, 0)

        logger.info { "fertig" }
    }

    /**
     * Store an item from the ramp in the warehouse.
     *
     * ramp2 > 1-6-vacuumGripper > 1-5-warehouse
     */
    private fun storeFromIndexedLineInWarehouse(row: Int, column: Int) {
        // Move item from ramp to warehouse
        vg16.waitForTargetReached(rotor = 1760, arm = 1500, vertical = 1660)
        vg16.grip()
        Thread.sleep(500)
        vg16.waitForVerticalTo(10)
        vg16.waitForTargetReached(rotor = 1427, arm = 1380)
        vg16.waitForVerticalTo(300)
        vg16.release()
        Thread.sleep(100)
        vg16.waitForVerticalTo(10)
        gateway.setValue("1-5-warehouse/control", mapOf("action" to "store", "column" to column, "row" to row))
    }

    /**
     * Process an item.
     *
     * 1-7-indexedLine > 1-8-clawGripper > ramp2
     */
    private fun processOnIndexedLine(mill: Boolean = true, drill: Boolean = true) {
        cg18.rotateTo(2150) // clawGripper 1-8 preparation

        gateway.setValue("1-7-indexedLine/control", mapOf("action" to "transfer", "transfer_from_to" to "feed_to_mill"))
        waitForProperty(gateway, "1-7-indexedLine") { p -> p["package_at_mill"] == true }
        if (mill) {
            Thread.sleep(400)
            gateway.setValue("1-7-indexedLine/control", mapOf("action" to "mill"))
            //waitForProperty(gateway, "1-7-indexedLine"){ p -> p["state"] == "ready" }
            Thread.sleep(2500)
        }
        gateway.setValue(
            "1-7-indexedLine/control",
            mapOf("action" to "transfer", "transfer_from_to" to "mill_to_drill")
        )
        waitForProperty(gateway, "1-7-indexedLine") { p -> p["package_at_drill"] == true }
        if (drill) {
            Thread.sleep(400)
            gateway.setValue("1-7-indexedLine/control", mapOf("action" to "drill"))
            //waitForProperty(gateway, "1-7-indexedLine"){ p -> p["state"] == "ready" }
            Thread.sleep(2500)
        }
        gateway.setValue("1-7-indexedLine/control", mapOf("action" to "transfer", "transfer_from_to" to "drill_to_end"))
        //waitForProperty(gateway, "1-7-indexedLine"){ p -> p["package_at_end"] == true }
        Thread.sleep(6000)

        // Step test preparation
        //vg16.rotateTo(2200)
        //cg18.rotateTo(2150
        // Move item from index line end to ramp
        cg18.waitForRotation()
        cg18.waitForVerticalTo(2280)
        cg18.grip(13)
        cg18.waitForClaw()
        cg18.waitForVerticalTo(1700)
        cg18.waitForRotationTo(3500)
        cg18.waitForVerticalTo(2600)
        cg18.release()
        cg18.verticalTo(500)
    }

    /**
     * Put sorted item onto the indexed line.
     *
     * 1-4-indexedLine > 1-6-vacuumGripper > 1-7-indexedLine
     */
    private fun getFromSortToIndexedLine(color: String) {
        if (color != "blue") {
            TODO("Only implemented for blue")
        }
        vg16.rotateTo(2480)
        vg16.waitForRotation()

        vg16.waitForTargetReached(arm = 50, vertical = 1550)
        vg16.grip()
        Thread.sleep(500)
        vg16.waitForVerticalTo(500)

        // Rotate arm to index line
        vg16.waitForTargetReached(rotor = 2130, arm = 1430)
        vg16.waitForVerticalTo(1100)
        vg16.release()
        Thread.sleep(100)
        vg16.waitForVerticalTo(10)
    }

    /**
     * Sort the item.
     *
     * 1-3-conveyor > 1-2-clawGripper > 1-1-conveyor > 1-4-sortingLine
     */
    private fun sortSequence(color: String) {
        if (!setOf("white", "red", "blue").contains(color)) {
            throw IllegalArgumentException()
        }
        cg12.grip(10)
        cg12.waitForRotationTo(95)
        cg12.waitForVerticalTo(2380)
        cg12.grip(14)
        cg12.waitForClaw()
        cg12.waitForVerticalTo(1700)
        cg12.armTo(80)
        cg12.waitForRotationTo(2000)
        cg12.waitForArm()
        cg12.release()
        cg12.waitForVerticalTo(1550)
        gateway.setValue("1-1-conveyor/control", mapOf("motor" to "left"))
        waitForProperty(gateway, "1-1-conveyor") { p -> p["reason"] == "position-limit" }
        gateway.setValue("1-1-conveyor/control", mapOf("motor" to "left-to=10"))
        gateway.setValue("1-4-sortingLine/control", mapOf("action" to "move to ejectors"))
        waitForProperty(gateway, "1-4-sortingLine") { p -> p["state"] == "package waiting at ejectors" }
        gateway.setValue("1-4-sortingLine/control", mapOf("action" to "sort", "color" to color))

        val needColor = when (color) {
            "white" -> "isWhiteStored"
            "red" -> "isRedStored"
            "blue" -> "isBlueStored"
            else -> throw Exception()
        }
        waitForProperty(gateway, "1-4-sortingLine") { p -> p[needColor] == true }
    }

    /**
     * Retrieve the requested container from the warehouse and move it to the end of the first conveyor belt.
     *
     * 1-5-warehouse > 1-6-vacuumGripper > 1-3-conveyor
     */
    private fun sequenceRetrieveFromWarehouse(row: Int, column: Int) {
        if (row < 0 || row > 3 || column < 0 || column > 3) {
            throw IllegalArgumentException()
        }
        logger.info { "retrieving ($row,$column) from warehouse" }
        gateway.setValue("1-5-warehouse/control", mapOf("action" to "retrieve", "column" to column, "row" to row))
        logger.info { "moving VC to warehouse pickup" }
        vg16.waitForRotationTo(1430)
        vg16.waitForArmTo(1380)
        logger.info { "Waiting for warehouse operation to finish" }
        waitForProperty(gateway, "1-5-warehouse") {p -> p["state"] == "at_pickup" || p["state"] == "unknown_pickup"}

        vg16.waitForVerticalTo(330)
        vg16.grip()
        Thread.sleep(500)
        vg16.waitForVerticalTo(10)
        vg16.waitForArmTo(0)

        vg16.waitForRotationTo(380)
        vg16.waitForVerticalTo(1200)
        vg16.release()
        Thread.sleep(100)
        vg16.waitForVerticalTo(10)

        gateway.setValue("1-3-conveyor/control", mapOf("motor" to "left"))
        waitForProperty(gateway, "1-3-conveyor") {p -> p["reason"] == "position-limit" && p["leftSensor"] == true}
    }

    /**
     * Reset all the actuators.
     *
     * Move claw and vacuum grippers up, in and release items.
     */
    private fun reset() {
        cg12.verticalTo(10)
        vg16.verticalTo(10)
        cg18.verticalTo(10)
        cg12.waitForVertical()
        vg16.waitForVertical()
        cg18.waitForVertical()

        cg12.release()
        vg16.release()
        cg18.release()

        cg12.resetArm()
        vg16.armTo(10)
        cg12.resetArm()
        cg12.waitForArm()
        vg16.waitForArm()
        cg18.waitForArm()
    }


    override fun executeTask(taskToExecute: String, digitalTwinEngine: DigitalTwinEngine) {
    }

    private fun waitForProperty(gateway: AbstractGateway, machine: String, subStatus: String, target: String) {
        val isOk: (Map<String, Any?>) -> Boolean = {propertyMap -> propertyMap.containsKey(subStatus) && propertyMap[subStatus] == target}
        waitForProperty(gateway, machine, isOk)
    }

    private fun waitForProperty(gateway: AbstractGateway, machine: String, isOk: (Map<String, Any?>) -> Boolean) {
        lock.withLock {
            var propertyMap = gateway.getValue("$machine/status") as? Map<String, Any?>?: mapOf()
            while (!isOk(propertyMap)) {
                condition.await()
                propertyMap = gateway.getValue("$machine/status") as? Map<String, Any?>?: mapOf()
            }
        }
    }

    override fun serviceUpdate(digitalTwinEngine: DigitalTwinEngine, observedProperty: String) {

    }

    override fun listen() {
        TODO("Not yet implemented")
    }

    init {
        runSequence.start()
    }
}
