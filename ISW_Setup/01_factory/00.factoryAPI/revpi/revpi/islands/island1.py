"""Machine description for island 1"""

conveyor11 = {
    'type': 'conveyor',
    'ports': {
        "senseLeft": "dio1_I_1",
        "senseRight": "dio1_I_2",
        "senseImpulse": "dio1_Counter_3",

        "actRight": "dio1_O_1",
        "actLeft": "dio1_O_2",
    },
}

clawGripper12 = {
    'type': 'claw_gripper',
    'ports': {
        "senseClawOpen": "dio1_I_5",
        "impulseCounterClaw": "dio1_Counter_6",
        "senseArmEndIn": "dio1_I_7",
        "impulseCounterArm": "dio1_Counter_8",
        "senseVerticalEndUp": "dio1_I_9",
        "senseRotEndRight": "dio1_I_10",
        "encoderVertical": "dio1_Counter_11",
        "encoderRot": "dio1_Counter_13",

        "actClawOpen": "dio1_O_3",
        "actClawClose": "dio1_O_4",
        "actArmOut": "dio1_O_5",
        "actArmIn": "dio1_O_6",
        "actVerticalDown": "dio1_O_7",
        "actVerticalUp": "dio1_O_8",
        "actRotRight": "dio1_O_9",
        "actRotLeft": "dio1_O_10",
    },
    'configuration': {
        'vertical': {
            'forward_limit': 2900,
            'accuracy': 5,
        },
        'rotor': {
            'forward_limit': 4000,
            'accuracy': 5,
        },
        'claw': {
            'forward_limit': 13,
        },
        'arm': {
            'forward_limit': 81,
        },
    },
}

conveyor13 = {
    'type': 'conveyor',
    'ports': {
        "senseLeft": "dio2_I_1",
        "senseRight": "dio2_I_2",
        "senseImpulse": "dio2_Counter_3",

        "actRight": "dio2_O_1",
        "actLeft": "dio2_O_2",
    },
}

sortingLine14 = {
    'type': 'sorting_line',
    'ports': {
        "impulseCounter": "dio2_Counter_4",
        "senseInputLightBarrier": "dio2_I_5",
        "senseMiddleLightBarrier": "dio2_I_6",
        "senseWhiteLightBarrier": "dio2_I_7",
        "senseRedLightBarrier": "dio2_I_8",
        "senseBlueLightBarrier": "dio2_I_9",

        "actMotorConveyor": "dio2_O_3",
        "actCompressorOn": "dio2_O_4",
        "actWhiteEjector": "dio2_O_5",
        "actRedEjector": "dio2_O_6",
        "actBlueEjector": "dio2_O_7",
    },
    'configuration': {
        'white_eject_time': 0.3,
        'red_eject_time': 1.3,
        'blue_eject_time': 2.2,
    }
}

warehouse15 = {
    'type': 'warehouse',
    'ports': {
        "senseHorizontalEnd": "dio3_I_1",
        "senseLightBarrierIn": "dio3_I_2",
        "senseLightBarrierOut": "dio3_I_3",
        "senseVerticalEndUp": "dio3_I_4",
        "encoderHorizontal": "dio3_Counter_5",
        "encoderVertical": "dio3_Counter_7",
        "senseArmOut": "dio3_I_9",
        "senseArmIn": "dio3_I_10",

        "actConveyorOut": "dio3_O_1",
        "actConveyorIn": "dio3_O_2",
        "actHorizontalToRack": "dio3_O_3",
        "actHorizontalToConveyor": "dio3_O_4",
        "actVerticalDown": "dio3_O_5",
        "actVerticalUp": "dio3_O_6",
        "actArmOut": "dio3_O_7",
        "actArmIn": "dio3_O_8",
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

vacuumGripper16 = {
    'type': 'vacuum_gripper',
    'ports': {
        "senseVerticalEndUp": "dio4_I_1",
        "senseArmEndIn": "dio4_I_2",
        "senseRotEndRight": "dio4_I_3",
        "encoderVertical": "dio4_Counter_5",
        "encoderArm": "dio4_Counter_7",
        "encoderRot": "dio4_Counter_9",

        "actVerticalUp": "dio4_O_1",
        "actVerticalDown": "dio4_O_2",
        "actArmIn": "dio4_O_3",
        "actArmOut": "dio4_O_4",
        "actRotRight": "dio4_O_5",
        "actRotLeft": "dio4_O_6",
        "actCompressorOn": "dio4_O_7",
        "actValve": "dio4_O_8",
    },
    'configuration': {
        'arm': {
            'forward_limit': 1696,
            'accuracy': 5,
        },
        'vertical': {
            'forward_limit': 1800,
            'accuracy': 5,
        },
        'rotor': {
            'forward_limit': 2960,
            'accuracy': 5,
            'stop_offset': 30,
        },
    },
}

indexedLine17 = {
    'type': 'indexed_line',
    'ports': {
        "sensePushButton1Front": "dio4_I_4",
        "sensePushButton1Back": "dio4_I_11",
        "sensePushButton2Front": "dio4_I_12",
        "sensePushButton2Back": "dio4_I_13",
        "senseSlider1": "dio4_I_14",
        "senseMilling": "dio5_I_1",
        "senseLoading": "dio5_I_2",
        "senseDrilling": "dio5_I_3",
        "senseConveyorSwap": "dio5_I_4",

        "actMotorSlider1Backward": "dio4_O_10",
        "actMotorSlider1Forward": "dio4_O_9",
        "actMotorSlider2Backward": "dio4_O_12",
        "actMotorSlider2Forward": "dio4_O_11",
        "actConveyorBeltFeed": "dio4_O_13",
        "actConveyorBeltMilling": "dio4_O_14",
        "actMillingMachine": "dio5_O_11",
        "actConveyorBeltDrilling": "dio5_O_12",
        "actDrillingMachine": "dio5_O_13",
        "actConveyorBeltSwap": "dio5_O_14",
    },
    'configuration': {
        'drill_time': 2.0,
        'mill_time': 2.0,
        'conveyor_movement_time': 1.0,
    }
}

clawGripper18 = {
    'type': 'claw_gripper',
    'ports': {
        "senseClawOpen": "dio5_I_5",
        "impulseCounterClaw": "dio5_Counter_6",
        "senseArmEndIn": "dio5_I_7",
        "impulseCounterArm": "dio5_Counter_8",
        "senseVerticalEndUp": "dio5_I_9",
        "senseRotEndRight": "dio5_I_10",
        "encoderVertical": "dio5_Counter_11",
        "encoderRot": "dio5_Counter_13",

        "actClawOpen": "dio5_O_3",
        "actClawClose": "dio5_O_4",
        "actArmOut": "dio5_O_5",
        "actArmIn": "dio5_O_6",
        "actVerticalDown": "dio5_O_7",
        "actVerticalUp": "dio5_O_8",
        "actRotRight": "dio5_O_9",
        "actRotLeft": "dio5_O_10",
    },
    'configuration': {
        'arm': {
            'forward_limit': 81,
        },
        'vertical': {
            'forward_limit': 2900,
            'accuracy': 5,
        },
        'rotor': {
            'forward_limit': 4000,
            'accuracy': 5,
        },
        'claw': {
            'forward_limit': 13,
        },
    },
}

machines = {
    "1-1-conveyor": conveyor11,
    "1-2-clawGripper": clawGripper12,
    "1-3-conveyor": conveyor13,
    "1-4-sortingLine": sortingLine14,
    "1-5-warehouse": warehouse15,
    "1-6-vacuumGripper": vacuumGripper16,
    "1-7-indexedLine": indexedLine17,
    "1-8-clawGripper": clawGripper18,
}
