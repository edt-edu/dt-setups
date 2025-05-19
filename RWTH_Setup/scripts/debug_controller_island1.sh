#!/bin/bash
trap "ssh pi@192.168.178.201 \"pgrep -f island1_main.py | xargs kill\"" INT
ssh pi@192.168.178.201 "cd /home/pi/rppmcontroller && source .venv/bin/activate && python -m debugpy --listen 0.0.0.0:5678 ./rppmcontroller/example/island1_main.py"
