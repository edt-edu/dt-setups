#!/bin/bash
TARGET_IP=$1
set -e
echo "Downloading deps on control pc"
rm -r ./downloads
python3.7 -m pip download -r ../../rppmcontroller/requirements.txt --platform arm64 --no-deps -d ./downloads
python3.7 -m pip download pytest-runner setuptools pytest wheel "Cython<3.0" "importlib-metadata>=0.12" "tomli>=1.0.0" debugpy --platform arm64 --only-binary=:all: -d ./downloads
echo "Syncing deps to RevPi"
rsync -r ./downloads/ pi@$TARGET_IP:/home/pi/python_deps/ --delete
echo "Installing deps on RevPi"
ssh pi@$TARGET_IP "ls /home/pi/rppmcontroller/.venv || +++python3 -m venv /home/pi/rppmcontroller/.venv"
ssh pi@$TARGET_IP "source /home/pi/rppmcontroller/.venv/bin/activate && cd /home/pi/python_deps/ && pip install * -f ./ --no-index && cd /home/pi/rppmcontroller && pip install -e . --no-index --no-build-isolation"
