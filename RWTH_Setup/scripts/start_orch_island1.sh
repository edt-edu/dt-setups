#!/bin/bash
TARGET_IP=192.168.178.201
script_dir=$(dirname -- "$( readlink -f -- "$0"; )")
# Keep all predefined messages but override host
mkdir "$script_dir/generated"
yq e ".connection.host = \"$TARGET_IP\"" ../../rppmorchestrator/rppmorchestrator/config.yml > generated/orch-config-island1.yml

ssh pi@$TARGET_IP pgrep -f island1_main.py && echo "Controller is running" || { echo "Controller on RevPi $TARGET_IP is not running"; exit 1; }

(cd ../../rppmorchestrator && source .venv/bin/activate && cd ./rppmorchestrator && python RevPiPyOrchestratorTUI.py "$script_dir/generated/orch-config-island1.yml")
