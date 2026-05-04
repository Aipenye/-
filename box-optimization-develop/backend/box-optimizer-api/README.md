# Box Optimizer API

This is the Spring Boot backend service for solving box optimization problems using multiple strategies. It exposes an API to receive box and item input and returns optimized packing results.

## Overview

This service supports solving packing problems using:

- **SkjolberStrategy** — A heuristic-based approach.
- **TimefoldStrategy** — A solver-based optimization using [Timefold](https://timefold.ai/).
- For future algorithms/strategies, use the `SolvingStrategy` interface.

## Project Structure

```bash
src/
├── main/
│ ├── java/com/xlrit/boxoptimization/
│ │ ├── algorithms/ # Strategy pattern implementations
│ │ ├── controller/ # REST controller for /optimize
│ │ ├── dto/ # Request/response data structures
│ │ ├── mappers/ # Strategy-specific data mappers
│ │ ├── model/ # Core domain models (Item, Container, etc.)
│ │ ├── services/ # Business logic (item optimization)
│ │ └── BoxOptimizationApiApplication.java # Main class
│ └── resources/
│ └── application.properties
├── test/
│ └── ... # Unit tests for model & mappers
```

## Running the box-optimizer-api

First you would have to make sure that the dependencies `timefold` and `skjolber` folders are built.

Then, run:

```
mvn clean install
mvn spring-boot:run
mvn test
```
