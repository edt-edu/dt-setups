# Factory Setup Instruction

This document provides an overview of the steps required to set up a Fischertechnik Training Factory. It includes 
references to the relevant documentation for wiring, hardware configuration, software updates and troubleshooting. 
Follow the sections below to complete the installation.


## Bill of Materials

A list with all needed components can be found here: [Bill of Materials](./Fischertechnik_BOM.xlsx)


## Electrical cabinet for power wiring

The electrical cabinet contains the power supply units, circuit breakers and the wiring between the Revolution Pi (RevPi) 
controllers and the digital I/O (DIO) modules.

**Important:** Parts of the electrical installation must be carried out by a certified electrician.

Detailed installation instructions are available here: 
[Electrical cabinet](RWTH_Planung_Verkabelung.pptx)

## Connecting a new Robot

After the electrical cabinet has been installed, each Fischertechnik robot must be connected to the corresponding RevPi DIO 
module. This includes creating the required wiring and configuring the digital input and output ports in PiCtory.

A step-by-step guide for wiring and configuration can be found here: [Connecting a new Robot](Connect_New_Robot.md)

## Updating RevPi

RevPi controllers are delivered with a preinstalled operating system. Depending on the delivery date, the installed 
version may be outdated or no longer supported by the current software environment.

It is recommended to verify the installed OS version before using the system. If necessary, update the operating system 
by following the instructions provided here: [Update RevPi OS](../../references/documentation/revpi/Upgrade_RevPi_OS.md)

## Known Hardware Issues

A continuously updated collection of common problems, their causes, and recommended solutions is available in the 
hardware issues documentation.

For troubleshooting information, refer to: 
[Hardware Issus](../../references/hardware-issues)
