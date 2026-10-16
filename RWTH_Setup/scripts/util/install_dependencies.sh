#!/bin/bash
TARGET_IP=$1
set -e
: "${CPS_FISCHERTECHNIK_DIR:?Set CPS_FISCHERTECHNIK_DIR to the cps-fischertechnik checkout}"
CPS_FISCHERTECHNIK_DIR=$(cd -- "$CPS_FISCHERTECHNIK_DIR" && pwd)
REQUIREMENTS="$CPS_FISCHERTECHNIK_DIR/rppmcontroller/requirements.txt"
[[ -f "$REQUIREMENTS" ]] || { echo "Requirements not found: $REQUIREMENTS" >&2; exit 1; }
SCRIPT_DIR=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." && pwd)
cd "$SCRIPT_DIR"
echo "Downloading deps on control pc"
if [[ -d ./downloads ]]; then rm -r ./downloads; fi
python3.7 -m pip download -r "$REQUIREMENTS" --platform arm64 --no-deps -d ./downloads
python3.7 -m pip download pytest-runner setuptools pytest wheel "Cython<3.0" "importlib-metadata>=0.12" "tomli>=1.0.0" debugpy --platform arm64 --only-binary=:all: -d ./downloads
echo "Syncing deps to RevPi"
rsync -r ./downloads/ pi@$TARGET_IP:/home/pi/python_deps/ --delete
echo "Installing deps on RevPi"
ssh pi@$TARGET_IP "ls /home/pi/rppmcontroller/.venv || python3 -m venv /home/pi/rppmcontroller/.venv"
ssh pi@$TARGET_IP "source /home/pi/rppmcontroller/.venv/bin/activate && cd /home/pi/python_deps/ && pip install * -f ./ --no-index && cd /home/pi/rppmcontroller && pip install -e . --no-index --no-build-isolation"
