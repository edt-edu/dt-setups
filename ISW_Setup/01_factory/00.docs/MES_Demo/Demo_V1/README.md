[Zurück zur Startseite](../../README.md)<br />

# Demo V1
Diese Demo implementiert einen einzelnen, festen Durchlauf für Insel 1. Ein MES ist noch nicht implementiert. Die Befehle werden ausschließlich in sequenzieller Folge abgearbeitet. Bis auf das verbinden zur Website, werden die restlichen Schritte auf dem Modellfabrik-PC unter der Modellfabrik ausgeführt.<br>
*Passwort:* ```schnitzel```

## Starten der Demo

Für alle folgenden Schritte ein eigenes Terminal starten ```Strg + t```.

1. Den Mosquitto-Broker starten:<br>
    ```mosquitto -c ~/etc/mosquitto/conf.d/mosquitto.conf```<br>
    <br>
    *Hinweis:* Sollte beim starten der Fehler ```Error: Address already in use``` auftauchen, folgende Schritte ausführen:<br>
    ```ps -ef | grep mosquitto```<br>
    Jede PID in folgendem * nacheinander eintragen und durchführen:<br>
    ```sudo kill *```<br>
    Beispiel: ```sudo kill 325 1262```

2. Die Website starten:<br>
    Zuerst muss die Website mittels npm gestartet werden:<br>
    ```cd ~/mes/Modellfabrik_Website```<br>
    ```npm run devStart```<br>
    Anschließend kann über ein beliebigen Browser, auf einem beliebigen Gerät über den Link ```localhost:6008``` auf die Website verbunden werden.<br>
    <br>
    *Hinweis:* Darauf achten dass das Gerät, welches auf die Website verbindet mit dem Netzwerk ```ISW_Modellfabrik``` verbunden ist.

3. Insel 1 starten:
    Die Insel wird mittels ssh gestartet:<br>
    ```ssh pi@192.168.1.110```<br>
    *Passwort:* t41cxb<br>
    Anschließend die Insel starten:<br>
    ```python3 -m revpi --mqtt=192.168.1.102 -v --island=island1 --json```<br>
    <br>
    *Hinweis:* Bei der IP im Argument ```--mqtt=192.168.1.102``` handelt es sich um die des Rechners, der den MQTT-Broker hostet. Sollte ein anderer PC verwendet werden, kann dessen IP mittels ```ifconfig``` ermittelt werden.

4. Das Kontrollprogramm (MES) starten:
    ```cd ~/mes/modellfabrik_Demo_V1/build/libs```<br>
    ```java -jar Modellafbrik_MES-1.0-SNAPSHOT.jar```<br>
    <br>
    *Hinweis:* Sollten Änderungen am Code vorgenommen werden, kann das Projekt wie folgt neu gebuildet werden:<br>
    ```cd ~/mes/Modellfabrik_Demo_V1```<br>
    ```./gradlew clean build```<br>
    Anschließend kann das Kontrollprogramm wie vorher beschrieben gestartet werden.

Ist alles gestartet, kann der Demo-Durchlauf durch das versenden eines beliebigen Joghurts von der Website (es kann auch einfach auf Order, ohne Joghurt gedrückt werden) gestartet werden. Dafür sollte sich ein Plastikzylinder mit Deckel auf der oberen Ablage, der an der Logistikbucht liegenden IO-Station befinden. Sobald der Durchlauf einmal fertig ist (sobald sich der Joghurt im Hochregallager befindet) kann dieser neu gestartet werden, indem ein weiteres mal auf Order gedrückt wird.

[Zurück zur Startseite](../../README.md)<br />