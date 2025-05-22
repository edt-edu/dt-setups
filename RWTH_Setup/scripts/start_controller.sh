#!/usr/bin/env bash

# ./start_controller <island_number> [-d]
# Upload and start the controller for the specified island
# -d: Install required dependencies beforehand

ISLAND_NUMBER=$1

# check island number
case "$ISLAND_NUMBER" in
  1)
    TARGET_IP=192.168.178.201
    ;;
  2)
    TARGET_IP=192.168.178.202
    ;;
  -h|--help|help|?)
    echo "./start_controller <island_number> [-d]"
    echo "Upload and start the controller for the specified island"
    echo "-d: Install required dependencies beforehand"
    exit 0
    ;;
  *)
    echo "Unsupported island: $ISLAND_NUMBER. Please provide a valid island number. Use ./start_controller --help for help"
    exit 1
    ;;
esac

if [[ "$2" == "-d" ]]; then
  echo "Installing dependencies for island $ISLAND_NUMBER..."
  ./util/install_dependencies.sh $TARGET_IP || exit 1
fi

echo "Uploading controller for island $ISLAND_NUMBER..."

rsync -r ../../rppmcontroller/ "pi@$TARGET_IP:/home/pi/rppmcontroller"
rsync -r "./island${ISLAND_NUMBER}_main.py" "pi@$TARGET_IP:/home/pi/rppmcontroller/rppmcontroller/example/"
rsync -r "./island${ISLAND_NUMBER}_config.yml" "pi@$TARGET_IP:/home/pi/rppmcontroller/rppmcontroller/example/"

echo "Starting controller for island $ISLAND_NUMBER..."

trap 'ssh pi@$TARGET_IP \"pgrep -f island${ISLAND_NUMBER}_main.py | xargs kill\"' INT
ssh "pi@$TARGET_IP" "cd /home/pi/rppmcontroller && source .venv/bin/activate && python ./rppmcontroller/example/island${ISLAND_NUMBER}_main.py"