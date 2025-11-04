#!/bin/bash
sudo docker compose up -d
#sudo docker compose -f SysOn/docker-compose.yml up -d
echo "containers started"

sleep 2s

echo "Container status:"
sudo docker ps -a

sleep 2s