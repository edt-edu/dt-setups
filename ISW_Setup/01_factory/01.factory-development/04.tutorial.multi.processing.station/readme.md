# Tutorial: MultiProcessingStation Anleitung

Diese Anleitung führt dich durch die Einrichtung und den Betrieb der MultiProcessingStation.

## Schritt 1: Schaltkasten anschließen
Verbinde den **Schaltkasten** mit einer Stromquelle, um die Anlage mit Strom zu versorgen.

## Schritt 2: RevolutionPi einrichten
Schließe den **RevolutionPi** wie folgt an:
- **Monitor**
- **Maus**
- **Tastatur**

## Schritt 3: Anlage starten
Schalte die Anlage ein, indem du den **Schalter am Schaltkasten** umlegst.

## Schritt 4: In RevolutionPi einloggen
Warte auf den Anmeldebildschirm am Monitor.
Melde dich im **RevolutionPi** mit den folgenden Zugangsdaten an:
- **Benutzername:** `pi`
- **Passwort:** `2m6h5z`

## Schritt 5: Programme ausführen
Starte die folgenden Python-Skripte in dieser Reihenfolge, um die MultiProcessingStation zu betreiben:

```bash
python3 MultiProcessingMappings.py
python3 MultiProcessingDIOs.py
```


## Schritt 6: Programme ausführen
Falls ein Not-Aus erforderlich ist, führe den folgenden Befehl aus:
```bash
python3 MultiProcessingDIOs.py emergency_stop
```
