class MPMapper:
    def __init__(self, rpi):
        self.rpi = rpi
        self.FT_output_mapping = MultiProcessingMappings.FT_output_mapping
        self.FT_input_mapping = MultiProcessingMappings.FT_input_mapping
        self.RevPi_output_mapping = MultiProcessingMappings.RevPi_output_mapping
        self.RevPi_input_mapping = MultiProcessingMappings.RevPi_input_mapping
        self.RPIO = {"O_1":self.rpi.io.O_1, "O_2":self.rpi.io.O_2,
                     "O_3":self.rpi.io.O_3, "O_4":self.rpi.io.O_4,
                     "O_5":self.rpi.io.O_5, "O_6":self.rpi.io.O_6,
                     "O_7":self.rpi.io.O_7, "O_8":self.rpi.io.O_8,
                     "O_9":self.rpi.io.O_9, "O_10":self.rpi.io.O_10,
                     "O_11":self.rpi.io.O_11, "O_12":self.rpi.io.O_12,
                     "O_13":self.rpi.io.O_13, "O_14":self.rpi.io.O_14,
                     }
        self.RPII = {"I_1":self.rpi.io.I_1, "I_2":self.rpi.io.I_2,
                     "I_3":self.rpi.io.I_3, "I_4":self.rpi.io.I_4,
                     "I_5":self.rpi.io.I_5, "I_6":self.rpi.io.I_6,
                     "I_7":self.rpi.io.I_7, "I_8":self.rpi.io.I_8,
                     "I_9":self.rpi.io.I_9, "I_10":self.rpi.io.I_10,
                     "I_11":self.rpi.io.I_11, "I_12":self.rpi.io.I_12,
                     "I_13":self.rpi.io.I_13, "I_14":self.rpi.io.I_14,
                     }


class MultiProcessingMappings:

    FT_output_mapping = {
        "motor_clockwise": 17,
        "motor_counterclockwise": 18,
        "motor_conveyor_forward": 19,
        "motor_saw": 20,
        "motor_oven_in": 21,
        "motor_oven_out": 22,
        "motor_vacuum_to_oven": 23,
        "motor_vacuum_to_turntable": 24,
        "light_oven": 25,
        "compressor": 26,
        "valve_vacuum": 27,
        "valve_lowering": 28,
        "valve_oven_door": 29,
        "valve_feeder": 30
    }
    FT_input_mapping = {
        "plus_power_supply_actuators": 1,
        "plus_power_supply_sensors": 2,
        "minus_power_supply_actuators": 3,
        "minus_power_supply_sensors": 4,
        "reference_switch_turntable_pos_vacuum": 5,
        "reference_switch_vacuum_pos_belt": 6,
        "light_barrier_end_conv_belt": 7,
        "reference_switch_turntable_pos_saw": 8,
        "reference_switch_vacuum_pos_turntable": 9,
        "reference_switch_oven_feeder_inside": 10,
        "reference_switch_oven_feeder_outside": 11,
        "reference_switch_vacuum_pos_oven": 12,
        "light_barrier_oven": 13,
        "None1": 14,
        "None2": 15,
        "None3": 16
    }

    RevPi_output_mapping = {
        17: "O_1", 18: "O_2",
        19: "O_3", 20: "O_4",
        21: "O_5", 22: "O_6",
        23: "O_7", 24: "O_8",
        25: "O_9", 26: "O_10",
        27: "O_11", 28: "O_12",
        29: "O_13", 30: "O_14"
    }

    RevPi_input_mapping = {
        1: "N/A", 2: "N/A",
        3: "N/A", 4: "N/A",
        5: "I_1", 6: "I_2",
        7: "I_3", 8: "I_4",
        9: "I_5", 10: "I_6",
        11: "I_7", 12: "I_8",
        13: "I_9", 14: "I_10",
        15: "I_11", 16: "I_12",
        17: "I_13", 18: "I_14"
    }