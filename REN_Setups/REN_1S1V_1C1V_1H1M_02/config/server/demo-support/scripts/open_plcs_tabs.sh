#!/bin/bash
gnome-terminal --tab --title="PLC 1 / RevPi103156" -- bash -c "ssh pi@RevPi103156.local; exec bash"
gnome-terminal --tab --title="PLC 2 / RevPi103259" -- bash -c "ssh pi@RevPi103259.local; exec bash" 
gnome-terminal --tab --title="PLC 3 / RevPi103228" -- bash -c "ssh pi@RevPi103228.local; exec bash" 
