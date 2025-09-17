[Zurück zur Startseite](../../README.md)<br />

# Unterbau
Der Unterbau besteht aus Rexroth Profilen in 45x45mm sowie den zugehörigen Verbindern, sofern sichtbar in der Design Line Edition. Bei den Verkleidungsplatten handelt es sich um HPL-Platten in anthrazit.<br />
Der Unterbau bietet die Möglichkeit, die Fischertechnik-Stationen gegenüber den Fahrflächen des Turtlebots ca. 20 cm erhöht zu platzieren, wodurch einerseits eine Sicherheitsbarriere entsteht und andererseits eine gute Greifhöhe für den Turtlebot erreicht wird, damit dieser mit seinem Arm die Werkstücke zwischen den Inseln umlagern kann.

# Stationsbeschreibung
Insgesamt sind auf dem Unterbau 4 verschiedene Inseln angeordnet, wobei zwei davon zusammengeschoben sind. Die grobe Anordnung der einzelnen Fischertechnik-Stationen auf den Inseln ergibt sich aus der angehängten Datei [Fabriklayout](Dokumente/Fabriklayoutplanung-V0-7-real.pptx). Jeder der Inseln ist dabei zur Identifikation eine Nummer zugeordnet; beginnend oben rechts mit Nummer 1 sind den weiteren Inseln im Uhrzeigersinn die Folgenummern zugewiesen.<br />
Die auf den Inseln platzierten Fischertechnik-Stationen sind ebenfalls entsprechend der Anordnung nummeriert und werden durch Kombination mit der Inselnummer eindeutig indentifiziert.<br />
Auf jeder der Inseln wird ein wesentlicher Produktionsschritt ausgeführt, sodass das Produkt beim Herstellungsprozess mehrere Inseln passieren muss, manche allerdings je nach Kundenwunsch bei fehlendem "Ausstattungsmerkmal" auch überspringen kann. Im Folgenden sind die Funktionalitäten der einzelnen Inseln näher beschrieben:

## Insel 1
"sorting + stirring"

Die Insel beinhaltet die folgenden Stationen mit ihren entsprechenden IDs:
| Maschine| ID|
| ------ | ------ |
| Förderband | 1.1-Conv |
| 3D-Roboter| 1.2-Grip|
| Förderband | 1.3-Conv|
| Sortierstrecke | 1.4-Sort|
| Hochregallager | 1.5-Store|
| Vakuumsauggreifer | 1.6-Vac|
| Taktstraße | 1.7-Indexed|
| 3D-Roboter | 1.8-Grip|

An dieser Insel erfolgt die Anlieferung von Bechern beliebiger Farbe an der TB-Logistikstation, die dann in der Sortierstation nach Farbe getrennt werden (diese Farbe muss dem MES im Hintergrund bekannt sein, da die Sortierstation ohne Farbsensor betrieben wird). Die Becher können dann im Lagerhaus gelagert werden und wieder entnommen werden, wenn ein Auftrag für diese Yoghurtsorte vorliegt. Dann durchläuft dieser Becher je nach Kundenwunsch noch die Rühranlage und wird zurück an die TB-Logistikstation verbracht.

## Insel 2
"freezing"

Die Insel beinhaltet die folgenden Stationen mit ihren entsprechenden IDs:
| Maschine| ID|
| ------ | ------ |
| 3D-Roboter| 2.1-Grip|
| 3D-Roboter| 2.2-Grip|
| Förderband | 2.3-Conv|
| Förderband | 2.4-Conv|
| Vakuumsauggreifer | 2.5-Vac|
| Multibearbeitsstation | 2.6-Freeze|

An dieser Insel erfolgt die Anlieferung von "befüllten" Bechern an der TB-Logistikstation. Nach dem Durchlauf durch den Freezer, wird der Becher an gleicher Stelle wieder vom TB abgeholt.

## Insel 3
"storage + lid"

Die Insel beinhaltet die folgenden Stationen mit ihren entsprechenden IDs:
| Maschine    | ID |
|------------|----|
|Vakuumsauggreifer | 3.1-Vac    |
|3D-Roboter | 3.2-Grip   |
|Stanzmaschine | 3.3-Press  |
|Förderband| 3.4-Conv   |
|Vakuumsauggreifer | 3.5-Vac    |
|3D-Roboter| 3.6-Grip   |
|Stanzmaschine | 3.7-Press  |
|Förderband| 3.8-Conv   |
|Hochregallager| 3.9-Store  |
|Vakuumsauggreifer | 3.10-Vac   |
|Hochregallager| 3.11-Store |

Auf dieser Insel sind 3 Produktionslinien untergebracht, mit jeweils separaten TB-Logistikstationen. Eine Linie zur Einlagerung von (Zwischen-/Ausgangs-)Produkten sowie zwei Linien zum Aufpressen der Deckel auf die Becher (Redundanz).

## Insel 4
"topping"

Die Insel beinhaltet die folgenden Stationen mit ihren entsprechenden IDs:
| Maschine     |   ID        |
|----------|-----------|
|3D-Roboter|4.1-Grip| 
|  Förderband        | 4.2-Conv  |
|          Förderband| 4.3-Conv  |
|        Vakuumsauggreifer  | 4.4-Vac   |
|     Hochregallager     | 4.5-Store |

An dieser Insel erfolgt die Anlieferung von "befüllten" Bechern an der TB-Logistikstation. Nach dem Aufbringen des Toppings, wird der Becher an gleicher Stelle wieder vom TB abgeholt.


# Schaltschrank
Im Schaltschrank sind 4 RevPi Cores als SPS mitsamt DIO Erweiterungskarten zur elektrischen Anbindung der Fischertechnik-Stationen untergebracht. Des weiteren ist ein Switch verbaut, der er ermöglicht, alle RevPis über die nach außen geführten LAN-Buchsen mit einem Netzwerk zu verbinden.<br />
Die Energieversorgung wird von zwei Netzteilen 24V/20A bereitgestellt. Die einzelnen Inseln mit den zugehörigen Cores sind jeweils separat mit 6A (Insel 1, 2 und 4) oder 10A (Insel 3) abgesichert. Die Verdrahtung der Stationen mit den Cores kann dieser [Datei](Dokumente/Kabellängen_und_Pin-Mapping-2.xlsx) entnommen werden. Die angesteckten DIO-Erweiterungskarten sind an die Stationen über 34- und 26-polige Flachbandkabel angeschlossen. Die Namen der Erweiterungskarten sowie der Pins der Erweiterungskarten können in PiCtory konfiguriert werden.<br />
Eine grundsätzliche Einführung in die Cores und Erweiterungsmodule findet sich auf der [Revolution-Pi-Webseite](https://revolutionpi.de/tutorials/quick-start-guide).

[Zurück zur Startseite](../../README.md)