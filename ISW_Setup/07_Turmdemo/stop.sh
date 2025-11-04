#!/bin/bash

#sudo docker compose down 
sudo docker compose -f docker-compose.yml #-f SysOn/docker-compose.yml down
sudo docker stop xzg_mbdo_devops_demo-app-1
sudo docker stop xzg_mbdo_devops_demo-database-1
sudo docker stop xzg_mbdo_devops_backend_sysml_service
echo "demo stopped."s

sleep 2s
echo "Demo status:"
sudo docker ps -a 

#rm stoped containers
sudo docker container prune -f

sleep 2s
echo "Demo status:"
sudo docker ps -a 