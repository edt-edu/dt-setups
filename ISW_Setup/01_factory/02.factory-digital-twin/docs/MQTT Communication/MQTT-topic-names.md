# Documentation of MQTT topic names


## Names

### Island names

Island names should be unique.

The format should be `islandI` with `I` the island number.

So for the first island this would be `island1`.

### Machine names

Machine names should be unique between islands, e.g. include the island number.

The format should be `I-M-TYPE` with
- `I`: This island number
- `M`: The machine number
- `TYPE`: The machine type in lowerCamelCase

So for the second machine of island1, a claw gripper, this would be `1-2-clawGripper`.


## Machine status and control

### Machine status

Collection of key/value pairs send by the machine to `<machine-name>/status`.

They contain all status information.

To request all status information, send a message to `<island-name>/QueryAll` with any payload.

### Machine control

Collection of key/value pairs send by the DT to `<machine-name>/control`.

Optional keys have, in general, no default value and are not reset if absent in consecutive control messages.

### Machine Types

For a description of the message formats for the different machine types, see
- [Vacuum Gripper](mqtt_messages/vacuum_gripper)
- [Warehouse](mqtt_messages/warehouse)
- [Conveyor Belt](mqtt_messages/conveyor_belt)
- [Claw Gripper](mqtt_messages/claw_gripper)
- [Indexed Line](mqtt_messages/indexed_line)
- [Sorting Line](mqtt_messages/sorting_line)

The description for the status and control message payloads is given as for TypeScript objects (JSON).
However, the default encoding in MQTT messages is the [ByteArray](MQTT-Message-Format) encoding.


## Lifecycle topics

### Island status and control

The island status is broadcasted at `<island-name>/status` with the following payloads
- `online`: The island is up and running
- `shutting down`: The island is stopping/halted
- `failed`: Used as a MQTT will message to indicate connection problems

The island can be stopped using `<island-name>/stop` with any payload.


### Machine lifecycle

Machines can be created at `<island-name>/create/<machine-name>`.
The payload must be a JSON string representing the machine configuration.
See the machine pages for examples of this configuration.

Machines can be deleted at `<island-name>/destroy/<machine-name>` with any payload.

The lifecycle status of all machines is available at `<island-name>/machines/<machine-name>` with the payloads
- `online`: The machine is up and running
- `already created`: Send after a creation request for an already configured machine
- `destroyed`: To indicate successful machine destruction/cleanup
- `failed`: To indicate a failed creation
- `unknown type`: To indicate a creation request with an unknown machine type
- `destroy aborted`: The machine can not be destroyed at this point because of long running tasks

### Logging

Log messages of type `WARNING` or higher are send at `<island-name>/errors` in JSON format.
