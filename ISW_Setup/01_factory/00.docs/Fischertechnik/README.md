[Zurück zur Startseite](../README.md)<br />

## SPS
### Pin Consumption
see the official Fischertechnik documentation on the pin layout by using the linked files in the table
| Fischertechnik Station | Input | Output |
| ------ | ------ | ------ |
| [Conveyor Belt](Dokumente/TDB_50464_KUNDENDOKUMENTATION_KOMPLETT_TRANSPORTBAND_24V_V2.pdf) | 3DI | 2DO |
| [Vacuum](Dokumente/TDB_536630-VAKUUM_SAUGGREIFER_24V.pdf)| 9DI (6)* | 8DO|
| [3D Gripper](Dokumente/TDB_536630-VAKUUM_SAUGGREIFER_24V.pdf) | 10DI (4)* | 8DO|
| [High Bay](Dokumente/TDB_536631-AUTOMATISIERTES_HOCHREGAL_24V.pdf)| 10DI (4)* | 8DO|
| [Punching Machine](Dokumente/TDB_96785_KUNDENDOKUMENTATION_KOMPLETT_STANZMASCHINE_24V_V4.pdf) | 4DI| 4DO|
| [Multi Processing Station](Dokumente/TDB_536632-MULTI_BEARBEITUNGSSTATION_24V.pdf) | 9DI| 14DO|
| [Indexed Line](Dokumente/TDB_96790_KUNDENDOKUMENTATION_KOMPLETT_TAKTSTRASSE_24V_V3.pdf) | 9DI| 10DO|
| [Sorting station](Dokumente/TDB_536633-SORTIERSTRECKE_MIT_FARBERKENNUNG_24V.pdf) | 6DI + 1AI| 5DO|

* values in paranthesis indicate the number of encoder pins, e.g. the Vacuum uses 9 digital inputs and 6 of them are used as encoder signals

### Pin Management
* The impulse switch needs to be debounced (750us on revpi)
* The encoder pins on some stations need to be swapped in order to achieve positive counts in the direction away from the reference switch (else it needs to be handled in software):
** B1 and B2 of the Warehouse
** B5 and B6 of the Vacuum Gripper

[Zurück zur Startseite](../README.md)