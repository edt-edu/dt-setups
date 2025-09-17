[Zurück zur Startseite](../../README.md)<br />

# Passwörter der PCs
Linker Dual-Boot-PC:<br>
* **Ubuntu:** ```_Tb1```
* **Windows:** ```schnitzel```

Rechter Dual-Boot-PC:<br>
* **Ubuntu:** ```_Tb2```
* **Windows:** ```schnitzel```

Kleiner Windows-PC:<br>
* ```_Temp0\_```

# Hochfahren der Fabrik
Router und Schaltschrank einstecken.
Die Fabrik und die RevPis werden gestartet, indem der Hauptschalter am Schaltschrank umgelegt wird. 
Kurz warten bis SteuerungsPC mit ISW_Modellfabrik WLAN verbunden ist.
Start des Steuerungsprogramms auf dem SteuerungsPC, aktuell für Station 4 zb ToppingExecutorLogic oder für Station 2 und 4 Demo.
Per SSH-Verbindung kann dann auf jedem einzelnen RevPi das Programm RevPiMain.py im Verzeichnis "01.implementation/Python" ausgeführt werden.
```
cd 01.implementation/Python
python3 RevPiMain.py
```
Ab diesem Zeitpunkt sind die RevPis in der Lage, Befehle von der JSON API entgegenzunehmen und auszuführen.


Anmerkung: Aufbau SSH-Verbindung
* Bsp mit Putty für SSH arbeiten (Username: pi)
* IP-Adressen der RevPi-Core Module lassen sich über die Landing Page des Routers (http://192.168.1.1/) finden; grundsätzlich für die 4 angeschlossenen Cores und den Steuerungsrechner fix konfiguriert:
* CORE1, Insel Sorting and Stirring: **IP:** ```192.168.1.110``` **PW:** ```t41cxb``` **IP per Ethernet:** ```141.58.122.165```
* CORE2, Insel Freeze: **IP:** ```192.168.1.120``` **PW:** ```g2x028``` **IP per Ethernet:** ```141.58.122.168```
* CORE3, Insel Storage and Lid: **IP:** ```192.168.1.130``` **PW:** ```h7myfn``` **IP per Ethernet:** ```141.58.122.171```
* CORE4, Insel Topping: **IP:** ```192.168.1.140``` **PW:** ```lysehd``` **IP per Ethernet:** ```141.58.122.172```
* Steuerungsrechner: **IP:** ```192.168.1.xxx```     (```xxx``` je nach PC unterschiedlich, mit ```ifconfig``` im Terminal auslesen)
* TODO Turtlebot

Anmerkung Dateitransfer bei Änderung in Python Files:
* RevPis nicht direkt an Git angebunden, manueller Dateitransfer bei Änderungen
* WinSCP für Datentransfer
* RevPiMain CORENumber anpassen

Anmerkung Config IP-Adressen angeschlossener Geräte
* WLan-Passwort: ```345665465```
* ```http://192.168.1.1/``` bei Verbindung mit ISW_Modellfabrik WLAN aufrufen
* Anmelden mit ```_F4Brik+m0deLL```
* erweiterte Einstellungen, Netz, Lan-Einstellungen aufrufen und Regeln hinzufügen
* (löschen von Regeln ebenfalls möglich, Reservierungszeitraum beachten bzw Routerneustart)

[Zurück zur Startseite](../../README.md)