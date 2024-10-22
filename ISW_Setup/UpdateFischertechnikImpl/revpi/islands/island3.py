"""Machine description for island3"""
# TODO Verify ports
# TODO Limits
# TODO punching machine

vacuumGripper31 = {
    'type': 'vacuum_gripper',
    'ports': {
        "senseVerticalEndUp": "dio1_I_1",
        "senseArmEndIn": "dio1_I_2",
        "senseRotEndRight": "dio1_I_3",
        "encoderVertical": "dio1_Counter_5",
        "encoderArm": "dio1_Counter_7",
        "encoderRot": "dio1_Counter_9",

        "actVerticalUp": "dio1_O_1",
        "actVerticalDown": "dio1_O_2",
        "actArmIn": "dio1_O_3",
        "actArmOut": "dio1_O_4",
        "actRotRight": "dio1_O_5",
        "actRotLeft": "dio1_O_6",
        "actCompressorOn": "dio1_O_7",
        "actValve": "dio1_O_8",
    },
    'configuration': {
        'arm': {
            'forward_limit': 1696,
            'accuracy': 5,
        },
        'vertical': {
            'forward_limit': 1692,
            'accuracy': 5,
        },
        'rotor': {
            'forward_limit': 2960,
            'accuracy': 5,
            'stop_offset': 30,
        },
    },
}

clawGripper32 = {
    'type': 'claw_gripper',
    'ports': {
        "senseClawOpen": "dio2_I_5",
        "impulseCounterClaw": "dio2_Counter_6",
        "senseArmEndIn": "dio2_I_7",
        "impulseCounterArm": "dio2_Counter_8",
        "senseVerticalEndUp": "dio2_I_9",
        "senseRotEndRight": "dio2_I_10",
        "encoderVertical": "dio2_Counter_11",
        "encoderRot": "dio2_Counter_13",

        "actClawOpen": "dio2_O_3",
        "actClawClose": "dio2_O_4",
        "actArmOut": "dio2_O_5",
        "actArmIn": "dio2_O_6",
        "actVerticalDown": "dio2_O_7",
        "actVerticalUp": "dio2_O_8",
        "actRotRight": "dio2_O_9",
        "actRotLeft": "dio2_O_10",
    },
    'configuration': {
        'arm': {
            'forward_limit': 81,
        },
        'vertical': {
            'forward_limit': 2600,
            'accuracy': 5,
        },
        'rotor': {
            'forward_limit': 4000,
            'accuracy': 5,
        },
        'claw': {
            'forward_limit': 20,
        },
    },
}

punchingMachine33 = {
    'type': 'punching_machine',
    'ports': {
        "senseLeft": "dio1_I_11",
        "senseRight": "dio1_I_12",
        "senseUp": "dio1_I_13",
        "senseDown": "dio1_I_14",

        "actRight": "dio1_O_9",
        "actLeft": "dio1_O_10",
        "actUp": "dio1_O_11",
        "actDown": "dio1_O_12",
    },
    'configuration': {},
}

conveyor34 = {
    'type': 'conveyor',
    'ports': {
        "senseLeft": "dio2_I_1",
        "senseRight": "dio2_I_2",
        "senseImpulse": "dio2_Counter_3",

        "actRight": "dio2_O_1",
        "actLeft": "dio2_O_2",
    },
}

vacuumGripper35 = {
    'type': 'vacuum_gripper',
    'ports': {
        "senseVerticalEndUp": "dio3_I_1",
        "senseArmEndIn": "dio3_I_2",
        "senseRotEndRight": "dio3_I_3",
        "encoderVertical": "dio3_Counter_5",
        "encoderArm": "dio3_Counter_7",
        "encoderRot": "dio3_Counter_9",

        "actVerticalUp": "dio3_O_1",
        "actVerticalDown": "dio3_O_2",
        "actArmIn": "dio3_O_3",
        "actArmOut": "dio3_O_4",
        "actRotRight": "dio3_O_5",
        "actRotLeft": "dio3_O_6",
        "actCompressorOn": "dio3_O_7",
        "actValve": "dio3_O_8",
    },
    'configuration': {
        'arm': {
            'forward_limit': 1696,
            'accuracy': 5,
        },
        'vertical': {
            'forward_limit': 1692,
            'accuracy': 5,
        },
        'rotor': {
            'forward_limit': 2960,
            'accuracy': 5,
            'stop_offset': 30,
        },
    },
}

clawGripper36 = {
    'type': 'claw_gripper',
    'ports': {
        "senseClawOpen": "dio4_I_5",
        "impulseCounterClaw": "dio4_Counter_6",
        "senseArmEndIn": "dio4_I_7",
        "impulseCounterArm": "dio4_Counter_8",
        "senseVerticalEndUp": "dio4_I_9",
        "senseRotEndRight": "dio4_I_10",
        "encoderVertical": "dio4_Counter_11",
        "encoderRot": "dio4_Counter_13",

        "actClawOpen": "dio4_O_3",
        "actClawClose": "dio4_O_4",
        "actArmOut": "dio4_O_5",
        "actArmIn": "dio4_O_6",
        "actVerticalDown": "dio4_O_7",
        "actVerticalUp": "dio4_O_8",
        "actRotRight": "dio4_O_9",
        "actRotLeft": "dio4_O_10",
    },
    'configuration': {
        'arm': {
            'forward_limit': 81,
        },
        'vertical': {
            'forward_limit': 2600,
            'accuracy': 5,
        },
        'rotor': {
            'forward_limit': 4000,
            'accuracy': 5,
        },
        'claw': {
            'forward_limit': 20,
        },
    },
}

punchingMachine37 = {
    'type': 'punching_machine',
    'ports': {
        "senseLeft": "dio3_I_11",
        "senseRight": "dio3_I_12",
        "senseUp": "dio3_I_13",
        "senseDown": "dio3_I_14",

        "actRight": "dio3_O_9",
        "actLeft": "dio3_O_10",
        "actUp": "dio3_O_11",
        "actDown": "dio3_O_12",
    },
    'configuration': {},
}

conveyor38 = {
    'type': 'conveyor',
    'ports': {
        "senseLeft": "dio4_I_1",
        "senseRight": "dio4_I_2",
        "senseImpulse": "dio4_Counter_3",

        "actRight": "dio4_O_1",
        "actLeft": "dio4_O_2",
    },
}

warehouse39 = {
    'type': 'warehouse',
    'ports': {
        "senseHorizontalEnd": "dio5_I_1",
        "senseLightBarrierIn": "dio5_I_2",
        "senseLightBarrierOut": "dio5_I_3",
        "senseVerticalEndUp": "dio5_I_4",
        "encoderHorizontal": "dio5_Counter_5",
        "encoderVertical": "dio5_Counter_7",
        "senseArmOut": "dio5_I_9",
        "senseArmIn": "dio5_I_10",

        "actConveyorOut": "dio5_O_1",
        "actConveyorIn": "dio5_O_2",
        "actHorizontalToRack": "dio5_O_3",
        "actHorizontalToConveyor": "dio5_O_4",
        "actVerticalDown": "dio5_O_5",
        "actVerticalUp": "dio5_O_6",
        "actArmOut": "dio5_O_7",
        "actArmIn": "dio5_O_8",
    },
    'configuration': {
        'rack': {
            # Arrays in the format [horizontal][vertical]
            'vertical_positions': [
                [66, 61, 70],
                [764, 764, 790],
                [1540, 1532, 1546]
            ],
            'vertical_offset': 25,
            'horizontal_positions': [
                [1456, 2613, 3783],
                [1455, 2625, 3773],
                [1464, 2632, 3781]
            ],
            'horizontal_offset': 30,
            'lower_time': 0.5,
            'lift_time': 0.7,
        },
        'conveyor': {
            'horizontal': 15,
            'vertical_get': 1440,
            'vertical_put': 1309,
            'lower_time': 0.5,
            'lift_time': 0.5,
        }
    }
}

vacuumGripper310 = {
    'type': 'vacuum_gripper',
    'ports': {
        "senseVerticalEndUp": "dio6_I_1",
        "senseArmEndIn": "dio6_I_2",
        "senseRotEndRight": "dio6_I_3",
        "encoderVertical": "dio6_Counter_5",
        "encoderArm": "dio6_Counter_7",
        "encoderRot": "dio6_Counter_9",

        "actVerticalUp": "dio6_O_1",
        "actVerticalDown": "dio6_O_2",
        "actArmIn": "dio6_O_3",
        "actArmOut": "dio6_O_4",
        "actRotRight": "dio6_O_5",
        "actRotLeft": "dio6_O_6",
        "actCompressorOn": "dio6_O_7",
        "actValve": "dio6_O_8",
    },
    'configuration': {
        'arm': {
            'forward_limit': 1696,
            'accuracy': 5,
        },
        'vertical': {
            'forward_limit': 1692,
            'accuracy': 5,
        },
        'rotor': {
            'forward_limit': 2960,
            'accuracy': 5,
            'stop_offset': 30,
        },
    },
}

warehouse311 = {
    'type': 'warehouse',
    'ports': {
        "senseHorizontalEnd": "dio7_I_1",
        "senseLightBarrierIn": "dio7_I_2",
        "senseLightBarrierOut": "dio7_I_3",
        "senseVerticalEndUp": "dio7_I_4",
        "encoderHorizontal": "dio7_Counter_5",
        "encoderVertical": "dio7_Counter_7",
        "senseArmOut": "dio7_I_9",
        "senseArmIn": "dio7_I_10",

        "actConveyorOut": "dio7_O_1",
        "actConveyorIn": "dio7_O_2",
        "actHorizontalToRack": "dio7_O_3",
        "actHorizontalToConveyor": "dio7_O_4",
        "actVerticalDown": "dio7_O_5",
        "actVerticalUp": "dio7_O_6",
        "actArmOut": "dio7_O_7",
        "actArmIn": "dio7_O_8",
    },
    'configuration': {
        'rack': {
            # Arrays in the format [horizontal][vertical]
            'vertical_positions': [
                [66, 61, 70],
                [764, 764, 790],
                [1540, 1532, 1546]
            ],
            'vertical_offset': 25,
            'horizontal_positions': [
                [1456, 2613, 3783],
                [1455, 2625, 3773],
                [1464, 2632, 3781]
            ],
            'horizontal_offset': 30,
            'lower_time': 0.5,
            'lift_time': 0.7,
        },
        'conveyor': {
            'horizontal': 15,
            'vertical_get': 1440,
            'vertical_put': 1309,
            'lower_time': 0.5,
            'lift_time': 0.5,
        }
    }
}

machines = {
    "3-1-vacuumGripper": vacuumGripper31,
    "3-2-clawGripper": clawGripper32,
    "3-3-punchingMachine": punchingMachine33,
    "3-4-conveyor": conveyor34,
    "3-5-vacuumGripper": vacuumGripper35,
    "3-6-clawGripper": clawGripper36,
    "3-7-punchingMachine": punchingMachine37,
    "3-8-conveyor": conveyor38,
    "3-9-warehouse": warehouse39,
    "3-10-vacuumGripper": vacuumGripper310,
    "3-11-warehouse": warehouse311,
}
