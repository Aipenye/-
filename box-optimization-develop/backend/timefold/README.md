# Timefold Optimization Backend

This README covers everything related to the **Timefold** component of this project. This module is based on the `bedallocation` quickstart example from [Timefold’s GitHub repository](https://github.com/TimefoldAI).

## Project Overview

This module forms the optimization engine for a larger backend system, using Timefold to solve constraint satisfaction problems related to box packing (BoxSolver).

## Prerequisites

Ensure the following are installed on your system:

- Java (JDK 21 or higher recommended)
- Maven (`mvn`)

## Project Structure

```
timefold/
├── boxSets/                 # Example input/output sets for testing
├── src/                     # Main codebase (Java)
├── test/                    # Duplicate of boxSets for isolated testing
├── pom.xml                  # Maven build file
└── README.md                # You are here
```

### High-Level Explanation

- The `timefold/` folder is the **root** directory of this optimization module.
- The `mvn package` command builds the project and creates a `.jar` file in the `target/` directory.
- This `.jar` is then consumed as a **library** by another backend module: `box-optimizer-api`.

## Building the Project

To build the optimization engine:

```bash
mvn package
```

This compiles the source code, runs all tests, and produces a `.jar` in the `target/` folder.

## Input and Test Sets

- The `boxSets/` folder contains input data used during development and testing.
- These are referenced by Java test files in `src/test/`.

## Source Code Breakdown

### `src/main/`

This is where the main development happens.

#### ➤ `resources/`

- **`application.properties`**  
  Contains basic configuration, but **only applies during test execution** (`mvn package`). It does **not** influence runtime behavior when running the backend via the web interface.

- **`solverConfig.xml`**  
  Controls Timefold's behavior, such as the solving strategy (e.g., greedy construction). This file is referenced both during tests and in actual runs.

- **`META-INF/`**  
  Legacy frontend files from the original Timefold demo. These are **not used** in the current production setup but may still be needed for test cases.

#### ➤ `java/org/acme/timefold/`

Core domain logic, REST interfaces, and solver configurations live here.

For details, refer to the [README.md](src/main/java/org/acme/timefold/README.md) inside this directory.

### `src/test/`

Test suite directory.

- Unit and integration tests for the optimization engine.
- Includes files like `BoxTest.java` and `BedSchedulingResourceIT.java`.
- Also contains a `test_cases_json/` folder with JSON test input/output.

> Tests are automatically run when executing `mvn package`.

## FAQ

### Can I change how long Timefold runs?

Yes — but not via `application.properties`. Modify the `solverConfig.xml` instead, particularly the `<termination>` settings.

### Where can I add more test cases?

Add input files to `boxSets/` and corresponding assertions in `src/test/`.

---
