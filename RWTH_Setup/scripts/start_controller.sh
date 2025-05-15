#!/usr/bin/env bash

# ./start_controller <island_number> [-d]
# Upload and start the controller for the specified island
# -d: Install required dependencies beforehand

# check island number
case "$1" in
  1)
    TARGET_IP=192.168.178.201
    ;;
  2)
    TARGET_IP=192.168.178.201
    ;;
  *)
    echo "Unsupported island: $1. Please provide a valid island number."
    exit 1
    ;;
esac

if [[ "$2" == "-d" ]]; then
  echo "Installing dependencies for island $1"
  ./install_dependencies $TARGET_IP || exit 1
fi

echo "Uploading controller for island $1"

rsync -r ../../rppmcontroller/ pi@$TARGET_IP:/home/pi/rppmcontroller
rsync -r "./island$1_main.py" pi@$TARGET_IP:/home/pi/rppmcontroller/rppmcontroller/example/
rsync -r "./island$1_config.yml" pi@$TARGET_IP:/home/pi/rppmcontroller/rppmcontroller/example/

echo "Starting controller for island $1"

# TODO adjust how we start and kill the process - can we assume that only one controller is running at the same time?
trap 'ssh pi@$TARGET_IP \"pgrep -f island1_main.py | xargs kill\"' INT
ssh pi@192.168.178.201 "cd /home/pi/rppmcontroller && source .venv/bin/activate && python ./rppmcontroller/example/island1_main.py"