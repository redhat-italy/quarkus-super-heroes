#!/bin/bash

#Open Docker, only if is not running
if (! docker stats --no-stream ); then
  # On Mac OS this would be the terminal command to launch Docker
  sudo systemctl start docker
 #Wait until Docker daemon is running and has completed initialisation
while (! docker stats --no-stream ); do
  # Docker takes a few seconds to initialize
  echo "Waiting for Docker to launch..."
  sleep 1
done
fi
sleep 20

docker-compose -f monitoring/docker-compose/monitoring.yml up 
sleep 20


