#!/bin/bash

TARGET_IP=192.168.178.201

# TODO: Delete other files on pi?

rsync -r ../../rppmcontroller/ pi@$TARGET_IP:/home/pi/rppmcontroller
rsync -r ./island1_main.py pi@$TARGET_IP:/home/pi/rppmcontroller/rppmcontroller/example/
rsync -r ./island1_config.yml pi@$TARGET_IP:/home/pi/rppmcontroller/rppmcontroller/example/
