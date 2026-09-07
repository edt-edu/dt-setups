# Instruction for connecting a new Robot

This instruction describes the steps to connect a new Fischertechnik Robot to the RevPi DIOs and how to configure the 
Input/Output Ports in Pictory.

## 1. Prepare RevPi DIO

The grey connector with the orange cable-locking mechanism must be connected to the RevPI as seen in the picture below.

<img src="RevPi_DIO_with_connector.jpg" width="400" height="500">

## 2. Connect cable to RevPi DIO

The cable must be connected to the grey connector as specified in the [RWTH-PinPlan.xlsx](https://git.rwth-aachen.de/mbdo/mbdo-impl/-/blob/master/RWTH_Setup/references/RWTH-PinPlan.xlsx?ref_type=heads). The Inputs 'I1, I2 ...' and
Outputs 'O1, O2...' reference the pins of the RevPi DIO as shown in the figure below. The 34 pins of the ribbon cable
are numbered beginning from the red edge of the cable.

<img src="RevPi_DIO_Input_Output_plan.jpg" width="500" height="500">

## 3. Terminate other end of the cable

The other end of the cable must be terminated in a 34-Pin connector. It is important that the small peg on the connector
is facing in the direction shown in the picture and the red edge of the cable is on the correct side.

<img src="Cable_with_34_Pin_Connector.jpg" width="400" height="500">

## 4. Extension cable (Optional)

If needed, an extension cable can be build. Otherwise, proceed with the next step.

The one end of the extension cable must be terminated with a 34-Pin connector. It is important to watch the orientation 
of the small peg on the connector and the red edge of the ribbon cable.

<img src="Cable_with_34_Pin_Connector.jpg" width="400" height="500">

The other end of the cable must be terminated with a grey receiving connector. It is important to watch the orientation
of the small nudge and the red edge of the ribbon cable.

<img src="Cable_with_34_Pin_Extension_Connector.jpg" width="400" height="500">

## 5. PiCtory

The RevPi DIO must be configured depending on the type of machine it is supposed to control.

Visit http://192.168.178.201 and log in.

<img src="PiCtory_login.jpg" width="500" height="400">

Turn on the administrative access.

<img src="PiCtory_Administrative_Access.jpg" width="500" height="300">

Start PiCtory using the "Open" button.

<img src="Start_Pictory.jpg" width="1000" height="250">

Select the DIO you want to configure.

<img src="PiCtory_select_DIO.jpg" width="1000" height="300">

Every input and output needs a unique name. Note that the names have to be unique for all DIOs connected to one RevPi Core.

<img src="PiCtory_port_names.jpg" width="500" height="500">

If the connected machine has encoder motors, the input mode for the respective inputs must be set to 'Encoder'. Note that
the pair of encoder ports must always be an uneven and the following even port number, e.g. '3' and '4' is possible but
'4' and '5' is not possible.

<img src="PiCtory_set_encoders.jpg" width="500" height="500">

## 6. PiTest

PiTest can be used to test if all the pins are connected correct.

Establish a SSH connection to the RevPi: `ssh pi@192.168.178.201`

With `piTest -r name_of_DIO_input_port` you can read the signals from the robots outputs that are received by the DIOs
input pins.

With `piTest -w name_of_DIO_ouput_port,value` you can write a boolean value (`0` or `1`) to DIOs output pins which are 
connected to the robots inputs.

#### Example for the Multi Processing Station:

`piTest -w I_1_i06,1` lets the turntable of the Multi Processing Station turn clockwise.

`piTest -r O_2_i06` prints (to console output) the signal from the reference switch near the conveyor belt, that is activated when the 
turntable reaches the conveyor belt.

