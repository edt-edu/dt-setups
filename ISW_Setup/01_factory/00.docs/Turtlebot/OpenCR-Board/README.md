[Zurück zur Startseite](../../README.md)<br />

# Einrichtung Dynamixelmotoren des OpenManipulators

## Schritt 1: Das OpenCR-Board herrichten

Zuerst dass OpenCR-Board aus dem Turtlebot3 abstecken und wie auf dem folgenden Bild
anschließen. Wichtig ist, dass zur Konfiguration der Motoren diese nicht untereinander
verbunden sein dürfen. Wenn der Arm schon aufgebaut ist muss er teilweise wieder
auseinander gebaut werden.
![Ausgebautes OpenCR-Board](Bilder/OpenCR_Board.jpg) <br>
**A**: Das USB-Kabel direkt an den PC anschließen. <br>
**B**: An diesem Anschluss den ersten Dynamixelmotor anschließen. <br>
**C**: Auch wenn durch den USB-Anschluss bereits eine gewisse Stromversorgung vorhanden
ist, muss zusätzlich noch die Hauptstromversorgung angeschlossen werden. <br>
**D**: Darauf achten dass der Stromschalter auch angeschalten ist. <br>

## Schritt 2: Die Arduino-IDE installieren, einrichten und Programm auf Arduino laden

Erst Arduino installieren: <br>
 ```sudo apt``` <br>
 <br>
Example Code holen: <br>
File/Preferences unter „Additional Boards Manager URLs“ folgenden
Link eingeben: <br>
```https://raw.githubusercontent.com/ROBOTIS-GIT/OpenCR/master/arduino/opencr_release/package_opencr_index.json``` <br>
<br>
OpenCR-Board installieren: <br>
Tools/Board: Arduino.../Boards Manager, opencr in Suche
eingeben und Version 1.5.2 installieren, anschließend wieder unter Tools/Board: Arduino…
OpenCR auswählen <br>
<br>
Wenn das OpenCR-Board angeschlossen ist unter Tools/Port den Port /dev/ttyACM0 (oder
so ähnlich) auswählen.
Das benötigte Programm, welches auf das OpenCR-Board geladen werden muss findet
sich unter dem Reiter .../Examples/OpenCR/Etc/usb_to_dxl. Auswählen und hochladen.
Fertig mit dem Arduinoteil. <br>
<br>
[Link zur OpenCR-Anleitung vom Robotis](https://emanual.robotis.com/docs/en/parts/controller/opencr10/)

## Schritt 3: Dynamixel Wizard 2 installieren und Konfigurieren

Jetzt kommt das eigentliche Konfigurieren der Motoren. Dafür muss der Dynamixel Wizard
2 installiert, gestartet und der erste Motor angeschlossen werden.
Im Wizard muss zuerst unter dem Reiter Tools/Options/Scan alles mit einem Häckchen
versehen werden. Zusätzlich muss darauf geachtet werden, dass bei „Set ID range to
scan“ die Reichweite von 0 bis 255 eingestellt ist.
Da Initial die einzelnen Einstellungen nicht bekannt sind ist es besser alles abzusuchen.
Sobald die Suche läuft und der Motor gefunden wurde, wird dieser angezeigt und die
weitere Suche kann abgebrochen werden.
Nun muss jeder Motor, welcher nicht der Greifermotor ist wie im Referenzbild 1 eingestellt
werden (Am Besten jede Zeile durchgehen und vergleichen). Die ID jedes Motors nach
Bauanleitung des Armes vergeben. <br>
![Referenzbild 1](Bilder/Referenzbild1.png) <br>
Für den Greifermotor muss die Konfiguration nach Referenzbild 2 erfolgen. <br>
![Referenzbild 2](Bilder/Referenzbild2.png) <br>
Wenn jeder Motor eingestellt ist kann der Arm zusammengebaut werden und sollte
funktionieren. <br>
<h4>Nicht vergessen das richtige Programm wieder auf das OpenCR-Board zu laden!</h4>

[Zurück zur Startseite](../../README.md)<br />