[Zurück zur Startseite](../../README.md)<br />

# MQTT Topics

## Maschinen Namen

Zum ansteuern einer Maschine wird der entsprechende Name benötigt. Diese sind wie folgt aufgebaut: <br>
```I-M-TYPE``` <br>
- ```I```: Inselnummer
- ```M```: Maschinennummer
- ```TYPE``` Maschinentyp aufgebaut in lowerCamelCase 

| Insel 1 | Insel 2 | Insel 3 | Insel 4 |
| ---- | ---- | ---- | ---- |
| 1-1-conveyor | 2-1-clawGripper | 3-1-vacuumGripper | 4-1-clawGripper |
| 1-2-clawGripper | 2-2-clawGripper | 3-2-clawGripper | 4-2-conveyor |
| 1-3-conveyor | 2-3-conveyor | 3-3-punchingMachine | 4-3-conveyor |
| 1-4-sortingLine | 2-4-conveyor | 3-4-conveyor | 4-4-vacuumGripper |
| 1-5-warehouse | 2-5-vacuumGripper | 3-5-vacuumGripper | 4-5-warehouse |
| 1-6-vacuumGripper | 2-6-multiProcessing | 3-6-clawGripper |  |
| 1-7-indexedLine |  | 3-7-punchingMachine |  |
| 1-8-clawGripper |  | 3-8-conveyor |  |
|  |  | 3-9-warehouse |  |
|  |  | 3-10-vacuumGripper |  |
|  |  | 3-11-warehouse |  |

**Hinweis!** Die verwendeten Namen der Topics stimmen aktuell nicht mit denen auf den Maschinen auf der Modelfabrik überein.

## Insel Namen

- Insel 1: ```island1```
- Insel 2: ```island2```
- Insel 3: ```island3```
- Insel 4: ```island4```

## Status

### Maschinenstatus Topic

Es können alle Statusinformationen einer Maschine abgerufen werden: <br>
```<machine-name>/status``` <br>


### Inselstatus Topics

Es können alle Maschineninformationen einer Insel gleichzeitig abgerufen werden:
```<island-name>/QueryAll```

## Befehle

Um eine Maschine zu steuern wird nur dessen Name benötigt: ```<machine-name>/control```

### Maschinenbefehle

## Lifecycle Topics

### Status

- Es kann der allgemeine Inselstatus abgerufen werden: ```<island-name>/status``` Dabei können folgende Antworten empfangen werden:
    - ```online```: Die Insel ist Einsatzbereit.
    - ```shutting down```: Die Insel fährt runter.
    - ```failed```: Gibt an dass es Probleme mit der MQTT Verbindung gibt.
- Es kann der allgemeine Maschinenstatus abgerufen werden: ```<island-name>/machines/<machine-name>``` Dabei können folgende Antworten empfangen werden:
    - ```online```: Die Maschine ist einsatzbereit.
    - ```already created```: Sollte versucht werden diese Maschinen ein weiteres Mal zu erzeugen.
    - ```destroyed```: Erfolgreiches zerstören einer Maschine.
    - ```failed```: Gescheiterte erzeugung einer neuen Maschine.
    - ```unkown type```: Versuch eine Maschine zu erzeugen mit unbekanntem Maschinentyp
    - ```destroy aborted```: Die Maschine kann nicht zerstört werden, da sie aktuell einen Befehl ausführt.

### Befehle

- Es kann eine Insel beendet werden: ```<island-name>/stop``` Dabei muss irgend eine Nachricht gesendet werden.
- Es kann eine neue Maschine erzeugt werden: ```<island-name>/create/<machine-name>``` Dabei muss ein JSON-String mit der entsprechenden Maschinenkonfiguration gesendet werden.
- Es kann eine Maschine zerstört werden: ```<island-name>/destroy/<machine-name>``` Dabei muss irgend eine Nachricht gesendet werden.

### Logging

Log-Nachrichten vom Typ ```WARNING``` oder höher werden an ```<island-name>/errors``` gesendet.

[Zurück zur Startseite](../../README.md)<br />