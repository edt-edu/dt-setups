[Zurück zur Startseite](../../README.md)<br />

# Builden mit Poetry

## Poetry installieren

Die Installation erfolgt über folgende Zeile im Terminal:<br>
```sudo apt install python3-poetry```

## Anleitung zum builden und installieren mit Poetry

Änderungen am RevPi-Package werden seitens einer IDE vorgenommen, anschließend gebuildet, auf die RevPis übertragen und zum Schluss dort gebuildet. Dafür werden folgende Schritte abgearbeitet: <br>

1. Das Package builden<br>
   Mittels ```cd``` in den oberen Revpi-Ordner wechseln (in dem sich die *pyproject.toml* befindet) und ```poetry build``` im Terminal eingeben. Anschließend befindet sich das gebuildete Package im *dist/*-Ordner als .whl.
2. Die .whl auf einen RevPi kopieren<br>
   Z.B. per Terminal, wenn vorher in den *dist/*-Ordner gewechselt wird: ```scp <name-des-package>.whl pi@<ip-des-RevPi>:```
3. Das alte Package auf dem RevPi deinstallieren<br>
   In einem Terminal auf einem RevPi folgendes eingeben: ```pip3 unistall revpi -y```
4. Das neue Package auf dem RevPi installieren<br>
   In einem Terminal auf einem RevPi folgendes eingeben: ```pip3 install <name-des-package>.whl```

## Die Steuerungs-API starten

1. Den MQTT-Server starten<br>
   Der Server wird seitens eines Steuerungs-PCs gestartet mit:```mosquitto -c /etc/mosquitto/conf.d/mosquitto.conf```
2. Den Code auf einem RevPi starten<br>
   ```python3 -m revpi --mqtt=<mqtt-server-ip>```<br>
   Optional lassen sich folgenden Argumente ergänzen:
   - ```-v``` für debugging Informationen
   - ```--island=island1``` um die Konfiguration des entsprechenden Insel zu laden. Standartmäßig wird Island1 geladen. Die anderen Inseln werden anhand der anhängenden Zahl 1-4 geladen.
   - ```--json``` damit die messages von MQTT als JSONs versendet und empfangen werden. Standartmäßig wird ein bytearray verwendet. 

[Zurück zur Startseite](../../README.md)<br />