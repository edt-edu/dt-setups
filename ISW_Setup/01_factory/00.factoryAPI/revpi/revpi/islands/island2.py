"""Machine description for Island 2"""
# TODO Verify ports
# TODO Limits
# TODO multi processing station

clawGripper21 = {
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
        'arm': {
            'forward_limit': 81,
        },
    },
}

clawGripper22 = {
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
        'arm': {
            'forward_limit': 81,
        },
    },
}

conveyor23 = {
    'type': 'conveyor',
    'ports': {
        "senseLeft": "dio1_I_1",
        "senseRight": "dio1_I_2",
        "senseImpulse": "dio1_Counter_3",

        "actRight": "dio1_O_1",
        "actLeft": "dio1_O_2",
    },
}

conveyor24 = {
    'type': 'conveyor',
    'ports': {
        "senseLeft": "dio2_I_1",
        "senseRight": "dio2_I_2",
        "senseImpulse": "dio2_Counter_3",

        "actRight": "dio2_O_1",
        "actLeft": "dio2_O_2",
    },
}

vacuumGripper25 = {
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
            'forward_limit': 1450,
            'accuracy': 5,
        },
        'vertical': {
            'forward_limit': 1692,
            'accuracy': 5,
        },
        'rotor': {
            'forward_limit': 2800,
            'accuracy': 5,
            'stop_offset': 30,
        },
    },
}

freezer26 = {
    'type': 'freezer',
    'ports': {
        "senseTurntablePosVacuum": "dio4_I_1",
        "senseTurntablePosConveyor": "dio4_I_2",
        "senseDelivery": "dio4_I_3",
        "senseTurntablePosSaw": "dio4_I_4",
        "senseVacuumGripperAtTurntable": "dio4_I_5",
        "senseOvenFeederIn": "dio4_I_6",
        "senseOvenFeederOut": "dio4_I_7",
        "senseVacuumGripperAtOven": "dio4_I_8",
        "senseOven": "dio4_I_9",

        "actRotClockwise": "dio4_O_1",
        "actRotCounterclockwise": "dio4_O_2",
        "actConveyorForward": "dio4_O_3",
        "actSaw": "dio4_O_4",
        "actOvenInward": "dio4_O_5",
        "actOvenOutward": "dio4_O_6",
        "actGripperToOven": "dio4_O_7",
        "actGripperToTurntable": "dio4_O_8",
        "actOvenLight": "dio4_O_9",
        "actCompressor": "dio4_O_10",
        "actValve": "dio4_O_11",
        "actLowerValve": "dio4_O_12",
        "actValveOvenDoor": "dio4_O_13",
        "actValveFeeder": "dio4_O_14",
    },
    'configuration': {},
}

machines = {
    "2-1-clawGripper": clawGripper21,
    "2-2-clawGripper": clawGripper22,
    "2-3-conveyor": conveyor23,
    "2-4-conveyor": conveyor24,
    "2-5-vacuumGripper": vacuumGripper25,
    "2-6-freezer": freezer26,
}
