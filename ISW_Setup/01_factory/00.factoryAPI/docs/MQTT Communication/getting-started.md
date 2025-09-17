
# Verbinden mit Islands und MQTT Setup

## Schritt-für-Schritt-Anleitung

### 1. Auf PC2 einloggen und WLAN einrichten
- Logge dich auf **PC2 (rechten PC)** ein.
- **Passwort:** `_Tb2`
- Starte die Modellfabrik verbinde dich nach kurzer Wartezeit mit dem WLAN `ISW_Modellfabrik` mit dem Passwort `_F4Brik+m0deLL`.

### 2. Mosquitto starten
- Öffne ein Terminal und starte den Mosquitto MQTT Broker:
  ```bash
  mosquitto
  ```

### 3. IP-Adresse finden
- Öffne ein **neues Terminal** und führe den Befehl `ifconfig` aus, um die IP-Adresse des PCs zu finden:
  ```bash
  ifconfig
  ```
  - Notiere die IP-Adresse (sie wird später benötigt).

### 4. Mit einer Insel verbinden
- Verbinde dich per SSH mit der gewünschten Insel (z.B. Insel 2):
  ```bash
  ssh pi@192.168.1.120
  ```

### 5. Zu Projektordner navigieren
- Navigiere im SSH-Terminal zu dem folgenden Projektordner:
  ```bash
  cd factory-development-new-api-01.implementation/01.implementation/Python/
  ```

### 6. RevPi mit MQTT verbinden
- Starte RevPi mit der gefundenen IP-Adresse des MQTT-Brokers(aus Schritt 3) und erhalte einen Statusbericht:
- `--island=island2` wählt die Insel aus (Standard ist Insel 1). `--mqtt=<IP-Adresse>` gibt den MQTT Broker an. `-v` fordert die Informationen an.
  ```bash
  python3 -m revpi --island=island2 --mqtt=<IP-Adresse> -v
  ```
  Wenn JSON verwendet werden will:
  ```bash
  python3 -m revpi --island=island2 --mqtt=<IP-Adresse> -v --json
  ```
  - Ersetze `<IP-Adresse>` mit der tatsächlichen IP-Adresse von Schritt 3.

- Führe die MQTT-Nachrichten aus.