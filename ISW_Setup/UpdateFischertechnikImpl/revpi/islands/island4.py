"""Machine description for island4"""
# TODO Verify ports
# TODO Limits

clawGripper41 = {
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

conveyor42 = {
    'type': 'conveyor',
    'ports': {
        "senseLeft": "dio1_I_1",
        "senseRight": "dio1_I_2",
        "senseImpulse": "dio1_Counter_3",

        "actRight": "dio1_O_1",
        "actLeft": "dio1_O_2",
    },
}

conveyor43 = {
    'type': 'conveyor',
    'ports': {
        "senseLeft": "dio2_I_11",
        "senseRight": "dio2_I_12",
        "senseImpulse": "dio2_Counter_13",

        "actRight": "dio2_O_9",
        "actLeft": "dio2_O_10",
    },
}

vacuumGripper44 = {
    'type': 'vacuum_gripper',
    'ports': {
        "senseVerticalEndUp": "dio2_I_1",
        "senseArmEndIn": "dio2_I_2",
        "senseRotEndRight": "dio2_I_3",
        "encoderVertical": "dio2_Counter_5",
        "encoderArm": "dio2_Counter_7",
        "encoderRot": "dio2_Counter_9",

        "actVerticalUp": "dio2_O_1",
        "actVerticalDown": "dio2_O_2",
        "actArmIn": "dio2_O_3",
        "actArmOut": "dio2_O_4",
        "actRotRight": "dio2_O_5",
        "actRotLeft": "dio2_O_6",
        "actCompressorOn": "dio2_O_7",
        "actValve": "dio2_O_8",
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

warehouse45 = {
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

machines = {
    "4-1-clawGripper": clawGripper41,
    "4-2-conveyor": conveyor42,
    "4-3-conveyor": conveyor43,
    "4-4-vacuumGripper": vacuumGripper44,
    "4-5-warehouse": warehouse45,
}
