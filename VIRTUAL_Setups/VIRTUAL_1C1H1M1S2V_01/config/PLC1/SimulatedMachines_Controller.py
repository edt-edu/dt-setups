import logging
from pathlib import Path
from typing import Any, Dict, List, Optional

from rppmcontroller.machine.conveyorbelt.ConveyorBelt import ConveyorBelt
from rppmcontroller.machine.conveyorbelt.ConveyorBeltSimpleSimulator import ConveyorBeltSimpleSimulator
from rppmcontroller.machine.highbay.HighBay import HighBay
from rppmcontroller.machine.highbay.HighBaySimpleSimulator import HighBaySimpleSimulator
from rppmcontroller.machine.multiprocessing.MultiProcessing import MultiProcessing
from rppmcontroller.machine.multiprocessing.MultiProcessingSimpleSimulator import MultiProcessingSimpleSimulator
from rppmcontroller.machine.sortingLine.SortingLine import SortingLine
from rppmcontroller.machine.sortingLine.SortingLineSimpleSimulator import SortingLineSimpleSimulator
from rppmcontroller.machine.vacuumgripper.VacuumGripper import VacuumGripper
from rppmcontroller.machine.vacuumgripper.VacuumGripperSimpleSimulator import VacuumGripperSimpleSimulator
from rppmcontroller.RevPiPyMachineController import RevPiPyMachineController

class SimulatedMiniFactoryController(RevPiPyMachineController):
    """
    Class allowing to stream commands to and from a simulated conveyor belt
    """

    def __init__(self, configurationFile : str = ""):
        """
        Init method of this class, starts all threads and everything is ready for receiving commands via Sockets and executing them
        """

        super().__init__( configurationFile=configurationFile)

        # Machine and simulator registries; support multiple instances per machine type.
        self.machines = []
        self.machineSimulators: Dict[Any, Any] = {}
        self.machinesByType: Dict[str, List[Any]] = {}

        # Legacy aliases (first instance for each type) kept for backward compatibility.
        self.conveyorBeltMachine = None
        self.vacuumGripperMachine = None
        self.multiProcessingMachine = None
        self.sortingLineMachine = None
        self.highBayMachine = None

        self.conveyorBeltSimulator = None
        self.vaccumGripperSimulator = None
        self.multiProcessingSimulator = None
        self.sortingLineSimulator = None
        self.highBaySimulator = None

        for machine_config in self._resolve_machine_configurations():
            self._add_machine_instance(machine_config["type"], machine_config["id"])

        self.currentlyExecuting = {m: None for m in self.machines}
        self.machineFeedback = {m: None for m in self.machines}
        self.commandFeedback = {m: None for m in self.machines}

    def _resolve_machine_configurations(self) -> List[Dict[str, str]]:
        default_machines = [
            {"type": "ConveyorBelt", "id": "ConveyorBelt01"},
            {"type": "VacuumGripper", "id": "VacuumGripper01"},
            {"type": "MultiProcessing", "id": "MultiProcessing01"},
            {"type": "SortingLine", "id": "SortingLine01"},
            {"type": "HighBay", "id": "HighBay01"},
        ]

        raw_config = self.controller_config.get("machines")
        if raw_config is None:
            return default_machines

        resolved: List[Dict[str, str]] = []
        if isinstance(raw_config, list):
            for entry in raw_config:
                resolved.extend(self._expand_machine_entry(entry))
        elif isinstance(raw_config, dict):
            for machine_type, entry in raw_config.items():
                resolved.extend(self._expand_machine_entry(entry, machine_type_hint=str(machine_type)))
        else:
            logging.warning("Unsupported 'machines' config format. Falling back to default setup.")
            return default_machines

        if not resolved:
            logging.warning("No valid machine config found under 'machines'. Falling back to default setup.")
            return default_machines

        return resolved

    def _expand_machine_entry(self, entry: Any, machine_type_hint: str = "") -> List[Dict[str, str]]:
        machine_type = self._canonical_machine_type(machine_type_hint)
        instances: List[Dict[str, str]] = []

        if isinstance(entry, str):
            inferred_type = self._infer_type_from_machine_id(entry)
            if inferred_type is None:
                inferred_type = machine_type
            if inferred_type is None:
                logging.warning(f"Cannot infer machine type for id '{entry}', skipping.")
                return []
            return [{"type": inferred_type, "id": entry}]

        if isinstance(entry, int):
            if machine_type is None:
                logging.warning(f"Count-based machine config requires a machine type key, skipping: {entry}")
                return []
            return self._generate_machine_ids(machine_type, entry)

        if isinstance(entry, list):
            for item in entry:
                instances.extend(self._expand_machine_entry(item, machine_type_hint=machine_type_hint))
            return instances

        if isinstance(entry, dict):
            explicit_type = self._canonical_machine_type(entry.get("type", ""))
            selected_type = explicit_type or machine_type

            ids = entry.get("ids")
            if isinstance(ids, list):
                for machine_id in ids:
                    if isinstance(machine_id, str) and selected_type is not None:
                        instances.append({"type": selected_type, "id": machine_id})
                return instances

            machine_id = entry.get("id")
            if isinstance(machine_id, str):
                if selected_type is None:
                    selected_type = self._infer_type_from_machine_id(machine_id)
                if selected_type is None:
                    logging.warning(f"Cannot determine machine type for id '{machine_id}', skipping.")
                    return []
                return [{"type": selected_type, "id": machine_id}]

            count = entry.get("count")
            if isinstance(count, int):
                if selected_type is None:
                    logging.warning(f"Count-based machine config requires a machine type, skipping: {entry}")
                    return []
                prefix = entry.get("prefix")
                if not isinstance(prefix, str):
                    prefix = selected_type
                return self._generate_machine_ids(selected_type, count, prefix)

        logging.warning(f"Invalid machine config entry, skipping: {entry}")
        return []

    def _generate_machine_ids(self, machine_type: str, count: int, prefix: Optional[str] = None) -> List[Dict[str, str]]:
        if count <= 0:
            return []
        id_prefix = prefix or machine_type
        return [
            {"type": machine_type, "id": f"{id_prefix}{index:02d}"}
            for index in range(1, count + 1)
        ]

    def _canonical_machine_type(self, machine_type: str) -> Optional[str]:
        normalized = str(machine_type).strip().replace("-", "").replace("_", "").replace(" ", "").lower()
        if not normalized:
            return None
        mapping = {
            "conveyorbelt": "ConveyorBelt",
            "vacuumgripper": "VacuumGripper",
            "multiprocessing": "MultiProcessing",
            "sortingline": "SortingLine",
            "highbay": "HighBay",
        }
        return mapping.get(normalized)

    def _infer_type_from_machine_id(self, machine_id: str) -> Optional[str]:
        lowered = machine_id.lower()
        if lowered.startswith("conveyorbelt"):
            return "ConveyorBelt"
        if lowered.startswith("vacuumgripper"):
            return "VacuumGripper"
        if lowered.startswith("multiprocessing"):
            return "MultiProcessing"
        if lowered.startswith("sortingline"):
            return "SortingLine"
        if lowered.startswith("highbay"):
            return "HighBay"
        return None

    def _add_machine_instance(self, machine_type: str, machine_id: str) -> None:
        machine = None
        simulator = None

        if machine_type == "ConveyorBelt":
            machine = ConveyorBelt(machine_id)
            simulator = ConveyorBeltSimpleSimulator(machine)
            if self.conveyorBeltMachine is None:
                self.conveyorBeltMachine = machine
                self.conveyorBeltSimulator = simulator
        elif machine_type == "VacuumGripper":
            machine = VacuumGripper(machine_id)
            simulator = VacuumGripperSimpleSimulator(controlledVacuumGripper=machine, encoderIncrement=33)
            if self.vacuumGripperMachine is None:
                self.vacuumGripperMachine = machine
                self.vaccumGripperSimulator = simulator
        elif machine_type == "MultiProcessing":
            machine = MultiProcessing(machine_id)
            simulator = MultiProcessingSimpleSimulator(machine)
            if self.multiProcessingMachine is None:
                self.multiProcessingMachine = machine
                self.multiProcessingSimulator = simulator
        elif machine_type == "SortingLine":
            machine = SortingLine(machine_id)
            simulator = SortingLineSimpleSimulator(machine)
            if self.sortingLineMachine is None:
                self.sortingLineMachine = machine
                self.sortingLineSimulator = simulator
        elif machine_type == "HighBay":
            machine = HighBay(machine_id)
            simulator = HighBaySimpleSimulator(machine)
            if self.highBayMachine is None:
                self.highBayMachine = machine
                self.highBaySimulator = simulator
        else:
            logging.warning(f"Unsupported machine type '{machine_type}', skipping id '{machine_id}'.")
            return

        self.machines.append(machine)
        self.machineSimulators[machine] = simulator
        self.machinesByType.setdefault(machine_type, []).append(machine)
        logging.info(f"Configured simulated machine {machine_type}:{machine_id}")

    def read(self) -> None:
        for simulator in self.machineSimulators.values():
            simulator.simulatedRead()

    def write(self) -> None:
        for simulator in self.machineSimulators.values():
            simulator.simulatedWrite()
   
    def reset(self) -> None:
        for machine in self.machinesByType.get("VacuumGripper", []):
            simulator = self.machineSimulators[machine]
            if machine.arm_reset_helper.must_reset():
                simulator.simulatedArmReset()
            if machine.rot_reset_helper.must_reset():
                simulator.simulatedRotationReset()
            if machine.vertical_reset_helper.must_reset():
                simulator.simulatedVerticalReset()

        for machine in self.machinesByType.get("HighBay", []):
            simulator = self.machineSimulators[machine]
            if machine.horizontal_reset_helper.must_reset():
                simulator.simulatedHorizontalReset()
            if machine.vertical_reset_helper.must_reset():
                simulator.simulatedVerticalReset()

if __name__ == "__main__":
    script_dir = Path(__file__).resolve().parent
    logging.basicConfig(format='%(asctime)s %(levelname)-5s: %(module)-30s,%(lineno)-3s: %(message)s', 
                        level=logging.DEBUG,
                        datefmt='%Y-%m-%d %H:%M:%S')
    handler = logging.FileHandler(script_dir / "logfile.log")
    logFormatter = logging.Formatter("%(levelname)-5s: %(module)-30s,%(lineno)-3s: %(message)s")
    handler.setFormatter(logFormatter)
    logging.getLogger().addHandler(handler)
    # init controller
    root = SimulatedMiniFactoryController(str(script_dir / "config.yml"))
    
    # start communication threads and main control loop
    root.start()