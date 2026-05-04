# XLRIT-optimization-2025

[![Docker Deployment Check](https://github.com/GiPHouse/XLRIT-optimization-2025/actions/workflows/docker-test.yml/badge.svg?event=check_run)](https://github.com/GiPHouse/XLRIT-optimization-2025/actions/workflows/docker-test.yml)

[![Running Tests](https://github.com/GiPHouse/XLRIT-optimization-2025/actions/workflows/test.yml/badge.svg?event=check_suite)](https://github.com/GiPHouse/XLRIT-optimization-2025/actions/workflows/test.yml)

## Development Environment

- **Java**: 21 (defined in `.java-version`)
- **Node.js**: 9.2.0 (defined in `.nvmrc`)

## Project Purpose

This application aims to optimize the packing of items into containers using various strategies (heuristic and solver-based). It supports configuration, simulation, and visualization of box packing outcomes, targeted at improving logistics efficiency in real-world scenarios.

## Project Structure – High-Level Overview

The project is divided into several modules and layers, reflecting a clean separation of concerns:

### Core Logic and APIs

- **`backend/box-optimizer-api/`**  
  This is the main Spring Boot application. It exposes REST APIs for optimizing box placement using multiple strategies. The core logic is structured around:
  - `algorithms/`: contains strategy implementations (e.g. Timefold, Skjolber)
  - `controller/`: exposes the HTTP endpoints
  - `model/`: defines key domain objects like `Container`, `Item`, `Warehouse`
  - `services/`: contains service classes used by the controller

### Solvers (Pluggable Engines)

- **`backend/skjolber/`**  
  Encapsulates the Skjolber 3D Bin Packing solver logic.
- **`backend/timefold/`**  
  Contains the Timefold-based solver. The module includes:
  - Custom domain definitions (e.g. `Box`, `Container`, `BoxPlan`)
  - Constraint configurations and solver setup
  - Data files (`boxSets/`) for example problems
  - Unit and integration tests

### Frontend

- **`frontend/`**  
  A React + Three.js-based 3D visualization UI. It lets users upload configurations, trigger solving, and visualize packed containers in real-time. Includes:
  - `src/input-logic/`: manages user input and internal state
  - `mock/` and `__mocks__/`: used for unit testing
  - `audio/`: includes sound effects for interactive UI
  - Vite configuration for fast builds and hot reloading

### Dockerization

- **`Dockerfile`, `docker-compose.yml`, `build-n-run-docker.sh`**  
  Provides an easy way to containerize and run the entire stack (frontend + backend). Allows for reproducible deployments and testing across environments.

---

Each major component has its own `README.md` for module-specific documentation:

- `box-optimizer-api/README.md` → API usage and controller-level logic
- `timefold/README.md` → Optimization with Timefold
- `skjolber/README.md` → Integration of the Skjolber solver

This modular setup ensures that solvers can be swapped or extended independently, and that the UI remains decoupled from backend logic. /

## **Tech Stack Summary**

| Layer            | Technology                        |
| ---------------- | --------------------------------- |
| Frontend         | React, Three.js, Vite             |
| Backend          | Spring Boot, Maven, Java 21       |
| Solvers          | Skjolber 3D Bin Packing, Timefold |
| Containerization | Docker, Docker Compose            |
| Testing          | Jest (frontend), JUnit (backend)  |

## Manual Build Instructions

> **Note:** Assumes Java, Maven, React, Vite, and npm are installed.

### Building Timefold JAR File

```bash
cd backend/timefold
mvn package
```

### Building the Backend

```bash
cd ~/box-optimizer-api
mvn spring-boot:run
```

### Building the Frontend

```bash
cd ~/frontend
# Delete node_modules if it exists
rm -rf node_modules
```

Now install the necessary dependencies (avoiding version conflicts):

```bash
npm install react@18.3.1 react-dom@18.3.1 three@0.150.1 @react-three/fiber@8.12.1 @react-three/drei@9.96.5 @react-three/cannon@6.6.0 @react-spring/three@9.7.5 use-asset react-merge-refs scheduler --legacy-peer-deps
```

Install testing dependencies:

```bash
npm install --save-dev @react-three/test-renderer@8.2.4 jest babel-jest @testing-library/react @testing-library/jest-dom jest-environment-jsdom --legacy-peer-deps
```

Build and start development:

```bash
npm run build
npm run dev
```

> After setup, you can simply run `npm run dev` to start the frontend in a new session.

## Launch the App with Docker

Build Docker images for the frontend and backend:

### Backend

```bash
cd backend
sudo docker build -t backend .
```

### Frontend

```bash
cd frontend
sudo docker build -t frontend .
```

### Compose the Containers

From the root of the project directory (`/XLRIT-optimization-2025`):

```bash
sudo docker compose up -d
```

The web application will be accessible at:
**http://localhost:5173**

## Stop or Clean Docker Containers

### Stop a Running Container

List running containers:

```bash
sudo docker ps -a
```

Stop a specific container:

```bash
sudo docker stop <CONTAINER_ID>
```

### Remove All Containers

```bash
sudo docker rm $(sudo docker ps -a -q)
```
