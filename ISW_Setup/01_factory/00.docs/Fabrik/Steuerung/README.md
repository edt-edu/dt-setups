[Zurück zur Startseite](../../README.md)<br />

Für alle Befehle die eine Argument \<int\> benötigen ist eine Obergrenze vorhanden. Es kann also kein Wert angegeben werden, welcher eine Station zu weit fahren lassen und beschädigen könnte.

# Station: Conveyor
| Befehl | Funktion |
| --- | --- |
| ```'{ "motor": "right" }'``` | Lässt das Förderband durchgängig nach Rechts fahren (von Seitens entgegen der Steckerleiste gesehen). |
| ```'{ "motor": "left" }'``` | Lässt das Förderband durchgängig nach Links fahren (von Seitens entgegen der Steckerleiste gesehen). |
| ```'{ "motor": "right-to=<int>" }'``` | Lässt das Förderband \<int\>-Schritte nach Rechts fahren (von Seitens entgegen der Steckerleiste gesehen). |
| ```'{ "motor": "left-to=<int>" }'``` | Lässt das Förderband \<int\>-Schritte nach Links fahren (von Seitens entgegen der Steckerleiste gesehen). |
| ```'{ "motor": "stop" }'``` | Stoppt das Förderband. |

# Station: Gripper
| Befehl | Funktion |
| --- | --- |
| **rotate** | **Steuert ausschließlich die Rotation des Turms des Greifers.** |
| ```'{ "rotate": "to=<int>" }'``` | Rotiert den Turm bis zur Position \<int\>. 0 = koplett nach rechts rotiert, 4000 = komplett nach links rotiert. |
| ```'{ "rotate": "right" }'``` | Rotiert den Turm rechts herum und stoppt automatisch wenn die Untergrenze erreicht ist. |
| ```'{ "rotate": "left" }'``` | Rotiert den Turm links herum und stoppt automatisch wenn die Obergrenze erreicht ist. |
| ```'{ "rotate": "stop" }'``` | Stoppt die Rotation des Turmes. |
| **arm** | **Steuert ausschließlich das Ein- und Ausfahren des Greifarms.** |
| ```'{ "arm": "to=<int>" }'``` | Fährt den Arm bis zur Position \<int\> aus. 0 = ganz eingefahren, 81 = ganz ausgefahren. |
| ```'{ "arm": "in" }'``` | Fährt den Arm komplett ein. |
| ```'{ "arm": "out" }'``` | Fährt den Arm komplett aus. |
| ```'{ "arm": "stop" }'``` | Stoppt das Ein- oder Ausfahren des Arms. |
| | Hinweis: Der "to=<int>" Befehl speichert intern seine aktuelle Position ab. Sollte nach dem verwenden von "in", "out" oder "stop" der "to=<int>" Befehl verwendet werden, wird die aktuelle Position nicht geändert und "to=<int>" wird nicht wie erwartet fubktionieren. Um dies zu beheben muss der "in" Befehl angewandt werden und der Arm gänzlich eingefahren werden. Damit ist die aktuelle Position zurückgesetzt und der "to=<int>" Befehl kann wie erwartet verwendet werden. ||
| **vertical** | **Steuert ausschließlich das Hoch- und Runterfahren des Arms.** |
| ```'{ "vertical": "to=<int>" }'``` | Fährt den Arm bis zur Position \<int\>. 0 = oberster Punkt, 2600 = unterster Punkt. |
| ```'{ "vertical": "up" }'``` | Fährt den Arm bis zum obersten Punkt herauf. |
| ```'{ "vertical": "down" }'``` | Fährt den Arm bis zum untersten Punkt herunter. |
| ```'{ "vertical": "Stop" }'``` | Stoppt das Hoch- oder Runterfahren des Arms. |
| **claw** | **Steuert ausschließlich das Öffnen und Schließen der Greifzange.** |
| ```'{ "claw": "to=<int>" }'``` | Schließt die Greifzange um einen angegebenen \<int\> Wert. \<int\> < 14. |
| ```'{ "claw": "close" }'``` | Schließt die Greifzange komplett. |
| ```'{ "claw": "reset" }'``` | Öffnet die Greifzange komplett. |
| ```'{ "claw": "stop" }'``` | Stoppt das Öffnen oder Schließen der Greifzange. |

# Station: Vacuum Gripper
| Befehl | Funktion |
| --- | --- |
| **rotate** | **Steuert ausschließlich die Rotation des Turms des Vakuumgreifers.** |
| ```'{ "rotate": "to=<int>" }'``` | Rotiert den Turm bis zur Position \<int\>. 0 = koplett nach rechts rotiert, 4000 = komplett nach links rotiert. |
| ```'{ "rotate": "right" }'``` | Rotiert den Turm rechts herum und stoppt automatisch wenn die Untergrenze erreicht ist. |
| ```'{ "rotate": "left" }'``` | Rotiert den Turm links herum und stoppt automatisch wenn die Obergrenze erreicht ist. |
| ```'{ "rotate": "stop" }'``` | Stoppt die Rotation des Turmes. |
| **arm** | **Steuert ausschließlich das Ein- und Ausfahren des Arms.** |
| ```'{ "arm": "to=<int>" }'``` | Fährt den Arm bis zur Position \<int\> aus. 0 = ganz eingefahren, 1696 = ganz ausgefahren. |
| ```'{ "arm": "in" }'``` | Fährt den Arm komplett ein. |
| ```'{ "arm": "out" }'``` | Fährt den Arm komplett aus. |
| ```'{ "arm": "stop" }'``` | Stoppt das Ein- oder Ausfahren des Arms. |
| **vertical** | **Steuert ausschließlich das Hoch- und Runterfahren des Arms.** |
| ```'{ "vertical": "to=<int>" }'``` | Fährt den Arm bis zur Position \<int\>. 0 = oberster Punkt, 1692 = unterster Punkt. |
| ```'{ "vertical": "up" }'``` | Fährt den Arm bis zum obersten Punkt herauf. |
| ```'{ "vertical": "down" }'``` | Fährt den Arm bis zum untersten Punkt herunter. |
| ```'{ "vertical": "Stop" }'``` | Stoppt das Hoch- oder Runterfahren des Arms. |
| **compressor** | **Steuert ausschließlich den Kompressor des Vakuumgreifers.** |
| ```'{ "compressor": <bool> }'``` | true = schaltet den Kompressor ein, false = schaltet den Kompressor aus. |
| **valve** | **Steuert ausschließlich das Ventil.** |
| ```'{ "valve": <bool> }'``` | true = schaltet das Ventil auf greifen, false = schaltet das Ventil auf lösen. |

# Station: Indexed Line 
| Befehl | Funktion |
| --- | --- |
| **action** | **Steuert Mühle und Bohrer der Taktstraße.** |
| ```'{ "action": "mill" }'``` | Aktiviert die Mühle für zwei Sekunden, sofern die entsprechende Lichtschranke unterbrochen ist. |
| ```'{ "action": "drill" }'``` | Aktiviert den Bohrer für zwei Sekunden, sofern die entsprechende Lichtschranke unterbrochen ist. |
| **transfer_from_to** | **Steuert den Transport der Taktstraße. Benötigt** ```"action": "transfer"```**.** |
| ```'{ "action": "transfer",``` <br /> ```   "transfer_from_to": "feed_to_mill" }'``` | Befördert den Joghurt vom Eingang bis zur Mühle. |
| ```'{ "action": "transfer",``` <br /> ```   "transfer_from_to": "mill_to_drill" }'``` | Befördert den Joghurt von der Mühle bis zum Bohrer. |
| ```'{ "action": "transfer",``` <br /> ```   "transfer_from_to": "drill_to_end" }'``` | Befördert den Joghurt vom Bohrer bis zum Ausgang. |

# Station: Sorting Line
| Befehl | Funktion |
| --- | --- |
| **action** | **Steuert den Transport innerhalb der Sortierstraße.** |
| ```'{ "action": "move to ejectors" }'``` | Befördert den Joghurt vom Eigang bis zur Lichtschranke kurz vor dem Sortierabschnitt. |
| ```'{ "action": "resolve failure" }'``` | Setzt den Status ```critical failure while sorting``` zurück, wenn dieser Auftrat. |
| **color** | **Bestimmt in welche Ausgabe der Joghurt im Sortierabschnitt gestoßen wird. Benötigt** ```"action": "sort"```**.** |
| ```'{ "action": "sort",``` <br /> ```   "color": "white" }'``` | Stößt den Joghurt in die weiße Ausgabe. |
| ```'{ "action": "sort",``` <br /> ```   "color": "red" }'``` | Stößt den Joghurt in die rote Ausgabe. |
| ```'{ "action": "sort",``` <br /> ```   "color": "blue" }'``` | Stößt den Joghurt in die blaue Ausgabe. |

# Station: Warehouse
| Befehl | Funktion |
| --- | --- |
| **store** | **Lagert eine Box aus der Annahme in der Ablage der angegebenen Spalte und Zeile.** |
| ```'{ "action": "store",``` <br /> ```   "row": <int>,``` <br /> ```   "column": <int> }'``` | Von seitens des Turms gesehen: row = 0, rechts; column = 0, oben. Je row und column gehen von 0-2. |
| **retrieve** | **Holt eine Box aus der Ablage der angegebenen Spalte und Zeile und liefert sie zur Annahme.** |
| ```'{ "action": "retrieve",``` <br /> ```   "row": <int>,``` <br /> ```   "column": <int> }'``` | Von seitens des Turms gesehen: row = 0, rechts; column = 0, oben. Je row und column gehen von 0-2. |

[Zurück zur Startseite](../../README.md)<br />