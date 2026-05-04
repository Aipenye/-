#!/bin/bash

# Exit immediately if a command exits with a non-zero status
set -e

echo "Building backend Docker image..."
cd backend
sudo docker build -t backend .

echo "Backend image built successfully."

echo "Building frontend Docker image..."
cd ../frontend
sudo docker build -t frontend .

echo "Frontend image built successfully."

echo "Starting Docker Compose from project root..."
cd ..
sudo docker compose up -d

echo "All services are up and running in detached mode."
echo "Website URL: http://localhost:5173 "