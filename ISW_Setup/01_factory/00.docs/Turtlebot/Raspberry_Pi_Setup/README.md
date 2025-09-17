[Zurück zur Startseite](../../README.md)<br />

# Installation und Einrichtung des Turtlebot3 sowie des Entwickler-PC

In diesem Ordner befinden sich alle benötigten Dateien zur Installation von ROS2 Humble, sowie Erklärungen zur Installation und Einrichtung der Voraussetzungen. Ziel dieses Ordners ist es die Installation möglichst einfach durchführbar zu halten. <br>
<h4>Wichtig!</h4>
Sollte sich im weiteren Verlauf der Entwicklung irgendetwas an der Installation ändern, z. B. neue Packages oder Einstellungen des Raspberry Pi, dann sollen diese Bitte in diesem Ordner vermerkt, oder gegebenfalls in einer der .sh Dateien geändert und Dokumentiert werden. <br>
Bei Problemen wärend der Installation kann auch auf die Website von [Robotis](https://emanual.robotis.com/docs/en/platform/turtlebot3/manipulation/#turtlebot3-with-openmanipulator) zurückgegriffen werden. Dabei 
ist zu beachten, dass oben die richtige ROS-Verison ausgewählt werden muss, in diesem Fall **Humble**. <br>
<br>
![Robotis Versions Auswahl Humble](./Bilder/Robotis_versions_auswahl_humble.png)

## 1. Entwickler-PC

### Docker Container
Alternativ kann ein Docker Container Benutzt werden.
Benötigt wird:
* Docker für Desktop

Docker für Desktop starten und folgende Befehle in einem Terminal ausführen:

[Auf dem Computer]
<br>docker pull osrf/ros:humble-desktop<br>
<br>docker run -it osrf/ros:humble-desktop<br>

<br>docker cp install_ros2_humble_docker.sh [Id des Containers]:/.<br>

[Im DockerContainer]
<br>bash install_ros2_humble_docker<br>

### 1.1 Ubuntu 22.04.2 (Jammy Jellyfish)

Um Sinnvoll auf dem Turtlebot3 programmieren zu können, das heisst nicht mit einem Terminal-Editor, werden alle Nodes des Turtlebot3 zuerst auf einem PC programmiert und hinterher auf den Turtlebot3 geladen. Dafür wird **Ubuntu 22.04.2** benötigt. Um dieses zu Installieren werden folgende Sachen benötigt:
* Ein USB-Stick: Alle Dateien sollten vorher gesichert werden, da sie sonst bei der Installation von Ubuntu gelöscht werden
* [Ubuntu 22.04.2 ISO](https://releases.ubuntu.com/jammy/): Es wird die Desktop(64bit) Version benötigt
* [Rufus](https://rufus.ie/de/): Zur Installation der ISO-Datei auf den Stick
* Die **install_ros2_humble_pc.sh** aus diesem Verzeichnis

### Schritt 1:

Einen freien USB-Stick am PC einstecken und anschließend Rufus starten. Rufus muss nicht installiert werden und startet daher direkt.<br>
<br>
![Rufus](./Bilder/Rufus.png)

### Schritt 2:

Darauf achten, dass unter **Laufwerk** der richtige USB-Stick ausgewählt ist.<br>
<br>
Auf **Auswahl** drücken und dort die vorher heruntergeladene ISO-Datei von Ubuntu 22.04.2 auswählen.

### Schritt 3:

Unten auf **Start** drücken.<br>
<br>
Im folgenden Fenster **Im ISO-Image-Modus schreiben (empfohlen)** auswählen.<br>
<br>
Im darauf folgenden Fenster **OK** auswählen.<br>
<br>
Damit ist die Einrichtung des Installations-Sticks abgeschlossen.

### Schritt 4:

Den Installations-Stick in den gewünschten PC einstecken, auf dem gearbeitet werden soll und im Boot-Menü (meistens beim Start wiederholt F12 drücken) den Stick auswählen. Diese Anleitung geht nicht auf die Ausführliche Installation von Ubuntu ein. Hinweis: Ubuntu lässt sich auch neben Windows installieren.

### Schritt 5:

Wichtig ist zu beginn jedes Starts des PCs einmal<br> 
`sudo apt update`<br> 
und<br> 
`sudo apt upgrade`<br> 
im Terminal einzugeben, damit Ubuntu immer auf dem neuesten Stand ist.<br>

### 1.2 Installation und Einrichtung von ROS2

Als nächstes muss noch die **install_ros2_humble_pc.sh** ausgeführt werden. Dafür muss diese zuerst in den persönlichen Ordner gezogen und anschließend folgende Befehl im Terminal eingegeben werden:<br> 
`bash install_ros2_humble_pc.sh`.<br>
Während der Ausführung werden im Terminal einige Bestätigungen erwartet, um die Installationen zu bestätigen.<br>
Die Installation kann am PC einige Minuten dauern, richtet aber alles für ROS benötigte ein und erstellt den Workspace-Ordner **turtlebot3_ws**.

### 1.3 src-Ordner aus dem Git builden

Zum Schluss muss der **src**-Ordner gebuildet werden. Dafür wird dieser aus dem Git in den **turtlebot3_ws**-Ordner heruntergeladen und folgende Befehle im Terminal eingegeben:<br> 
`~/turtlebot3_ws`<br>
und anschließend<br> 
`colcon build --parallel-workers 1`.<br>
<h4>Damit ist die Installation und Einrichtung vom Entwickler-PC fertig!</h4>

## 2. Turtlebot3 Raspberry Pi

### 2.1 Ubuntu Server 22.04.3 (Jammy Jellyfish)

ROS2-Humble muss jetzt noch auf dem Turtlebot3 selbst installiert werden. Da es keinen Grund für eine direkte grafische Ausgabe des Turtlebot3 gibt, wird die Server-Version installiert. 
Dafür werden folgende Sachen benötigt:
* Die Mini-SD-Karte aus dem Raspberry Pi des Turtlebot3
* Den [Raspberry Pi Imager](https://www.raspberrypi.com/software/) (Ob für Windows oder Ubuntu ist dabei egal)
* [Ubuntu Server 22.04.3 ISO](https://ubuntu.com/download/server): Darauf achten das Ubuntu Server 22.04.3 LTS heruntergeladen wird und nicht die neueste Verison.
* Die **install_ros2_humble_sbc.sh** aus diesem Verzeichnis

### Schritt 1:

Raspberry Pi Imager installieren, die Mini-SD-Karte einstecken und den Raspberry Pi Imager starten. <br>
<br>
![Raspberry Pi Imager](./Bilder/Pi_Imager.png)
<br>
### Schritt 2:

1. **OS WÄHLEN** auswählen.
2. **Eigenes Image** auswählen.
3. Anschließend die Ubuntu-Server-ISO auswählen und öffnen.

### Schritt 3:

**SD_KARTE WÄHLEN** drücken und die entsprechende Mini-SD-Karte des Turtlebot3 auswählen.

### Schritt 4:

Durch das drücken des unten rechts befindlichen Zahnrads können zwei Einstellungen vorweg vorgenommen werden:
1. **Benutzername und Passwort setzen:** Häckchen setzen. Bisher wird immer **Ubuntu** als Benutzername und **turtlebot** verwendet.
2. **Wifi einrichten:** Häckchen setzen und die entsprechende SSID und das Passwort eingeben.

### Schritt 5:

Zum Schluss  **SCHREIBEN** auswählen. <br>
Sobald der Imager fertig ist kann die SD-Karte wieder entnommen werden und in den Raspberry Pi des Turtlebot3 eingesteck werden.

### 2.2 Einrichten des Internets:

Zum einrichten der Internetverbindung, sofern vorher noch nicht geschehen/geklappt, gibt es drei Möglichkeiten:
* Die SD-Karte über ein Ubuntu-Laptop / PC mit Mini-SD-Karten-Leser bearbeiten.
* Die SD-Karte über ein Windows-PC mittels [PuTTY](https://www.putty.org/) bearbeitem.
* Oder den Raspberry Pi des Turtlebot3 direkt mit einem HDMI-Kabel an einen Bildschirm anschließen. Zusätzlich muss noch eine Tastatur an den Raspberry Pi angeschlossen werden.

Hier wird davon ausgegangen, dass der Raspberry Pi des Turtlebot3 direkt an den Bildschirm anschlossen wird.

### Schritt 1:

Den Turtlebot3 und damit den Raspberry Pi starten, indem der Turtlebot3 mit Strom versorgt und anschließend den schwarzen Schalter vorne am OpenCR-Board umlegt.

### Schritt 2:

Sich mit **Ubuntu** anmelden und das Passwort **turtlebot** eingeben.

### Schritt 3:

Folgenden Befehl im Terminal eingeben: `sudo nano ~/../../etc/netplan/50-cloud-init.yaml` und Inhalt wie im folgenden Bild um richtige **WIFI_SSID** und **WIFI_PASSWORD** bearbeiten: <br>
<br>
![netplan](./Bilder/netplan.png)
<br>
<br>
Inhalt mittels **Strg + s** speichern und mit **Strg + x** verlassen. <br>
Damit kann der Raspberry Pi neugestartet werden, ohne den Bildschirm und der Tastatur.

### 2.3 Installation und Einrichtung von ROS2

Zur Installation empfiehlt es sich mittels SSH vom PC (Windows:PuTTY, Ubuntu) auf den Turtlebot3 zu verbinden:<br>
`ssh ubuntu@ip_des_turtlebot3`<br>
Wie bei der Installation am PC muss jetzt die **install_ros2_humble_sbc.sh** in den persönlichen Ordner gezogen werden *(Hinweis: Den scp-Befehl verwenden)* und anschließend mit <br>
`bash install_ros2_humble_sbc.sh` <br> 
installiert werden. Die darauf folgende Prozedur ist dieselbe wie beim PC.

### 2.4 src-Ordner aus dem GIT builden

Zum Schluss muss der **src**-Ordner wie am PC noch gebuildet werden. Dafür aus dem Git den **turtlebot3_ws**-Ordner herunterladen und folgende Befehle im Terminal eingeben:<br> 
`~/turtlebot3_ws`<br>
und anschließend<br> 
`colcon build --parallel-workers 1`.<br>
<h4>Damit ist die Installation und Einrichtung vom Turtlebot3 fertig!</h4>

[Zurück zur Startseite](../../README.md)<br />