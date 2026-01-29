[![Java CI with Maven in Linux](https://github.com/syedali430/inventorymanager/actions/workflows/maven.yml/badge.svg)](https://github.com/syedali430/inventorymanager/actions/workflows/maven.yml) [![Java CI with Maven, Docker and SonarCloud in Linux](https://github.com/syedali430/inventorymanager/actions/workflows/build.yml/badge.svg)](https://github.com/syedali430/inventorymanager/actions/workflows/build.yml) [![Java CI with Maven with Windows and Mac](https://github.com/syedali430/inventorymanager/actions/workflows/mac_win_maven.yml/badge.svg)](https://github.com/syedali430/inventorymanager/actions/workflows/mac_win_maven.yml)
[![Coverage Status](https://coveralls.io/repos/github/syedali430/inventorymanager/badge.svg?branch=main)](https://coveralls.io/github/syedali430/inventorymanager?branch=main)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=syedali430_inventorymanager2&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=syedali430_inventorymanager2) [![Bugs](https://sonarcloud.io/api/project_badges/measure?project=syedali430_inventorymanager2&metric=bugs)](https://sonarcloud.io/summary/new_code?id=syedali430_inventorymanager2) [![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=syedali430_inventorymanager2&metric=code_smells)](https://sonarcloud.io/summary/new_code?id=syedali430_inventorymanager2) [![Coverage](https://sonarcloud.io/api/project_badges/measure?project=syedali430_inventorymanager2&metric=coverage)](https://sonarcloud.io/summary/new_code?id=syedali430_inventorymanager2) [![Technical Debt](https://sonarcloud.io/api/project_badges/measure?project=syedali430_inventorymanager2&metric=sqale_index)](https://sonarcloud.io/summary/new_code?id=syedali430_inventorymanager2) [![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=syedali430_inventorymanager2&metric=vulnerabilities)](https://sonarcloud.io/summary/new_code?id=syedali430_inventorymanager2) [![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=syedali430_inventorymanager2&metric=sqale_rating)](https://sonarcloud.io/summary/new_code?id=syedali430_inventorymanager2) [![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=syedali430_inventorymanager2&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=syedali430_inventorymanager2)

# **Inventory Manager**

An inventory management system built in Java with MongoDB integration, providing CRUD operations for managing items. This project follows a **Test-Driven Development (TDD)** approach, with a focus on achieving **100% code coverage**.

---

## **About the Author**
- Maintained by the repository owner with community contributions.
- Focused on practical Java testing workflows and repeatable builds.

---

## **Introduction**
- Inventory Manager is a small Java Swing app backed by MongoDB.
- The codebase demonstrates TDD, CI, and automated testing practices.

---

## **Book Structure**
- Sections map to real tooling in this repo: tests, build, CI, and quality gates.
- Each topic points to the relevant configuration or test folder.

---

## **Features**
- **CRUD Operations** for inventory items
- **MongoDB repository integration**
- **Test-Driven Development (TDD)** for code reliability and quality
- **100% Code Coverage** using **SonarCloud** and **Coveralls**
- **Continuous Integration** with **GitHub Actions**, testing on Java in multiple environments

---

## **Testing**
- Focus on fast feedback with unit, integration, UI, and end-to-end layers.
- AssertJ Swing covers GUI flows; embedded Mongo keeps tests hermetic.
- Awaitility guards async UI updates to avoid flaky checks.
- CI runs headless on Linux and desktop runners to validate cross-platform behavior.

---

## **Eclipse**
- Import the repo as an Existing Maven Project to pick up classpath settings.
- Use JDK 8 for source/target compatibility.
- `.project` and `.classpath` are checked in for convenience.

---

## **JUnit & TDD**
- JUnit 4.13 drives the red-green-refactor loop for controllers, repos, and UI.
- Tests mock controllers where needed and hit embedded Mongo for integration confidence.
- Each feature starts from a failing test to keep scope small and design focused.

---

## **Code Coverage & Mutation Testing**
- JaCoCo enforces minimum line coverage via Maven build rules.
- SonarCloud reports coverage on CI; Coveralls can be enabled for external badges.
- PIT mutation testing runs in the Maven lifecycle to catch weak assertions and missing branches.

---

## **Maven**
- `mvn test` runs unit tests; `mvn verify` runs integration and E2E suites.
- Use profiles like `with-docker` or `skip-testcontainers` when needed.
- `-DskipITs` and `-Ddocker.skip` help speed up local runs.

---

## **Mocking**
- Mockito verifies controller interactions and isolates UI behavior.
- Keep mocks at boundaries; rely on real components for integration tests.

---

## **Git**
- Keep commits small, descriptive, and tied to a single topic.
- Push frequently so CI validates each step.

---

## **Integration Tests**
- Integration tests live under `inventorymanager/src/it/java` and run via Failsafe.
- Embedded Mongo keeps DB state deterministic for repository and controller flows.
- Use `mvn verify` to include them, or `-DskipITs` to skip.

---

## **UI Tests**
- UI tests use AssertJ Swing fixtures to exercise view behavior.
- They validate button states, list selection, and field synchronization.

---

## **End-to-End Tests**
- AssertJ Swing drives the full UI workflow against the running application.
- E2E tests live under `inventorymanager/src/e2e/java` and run in the integration phase.
- Linux CI runs headless via `xvfb-run` to support GUI tests.

---

## **Continuous Integration**
- GitHub Actions builds on Linux, Windows, and macOS for cross-platform confidence.
- CI runs `mvn verify` on Linux and `mvn test` on desktop runners.
- Sonar analysis runs only when `SONAR_TOKEN` is present.

---

## **Docker**
- Docker can be used to provide external services when needed (Mongo/Sonar).
- Local `docker-compose.yml` is available for running SonarQube + Postgres.
- CI skips Docker when no daemon is available.

---

## **Code Quality**
- SonarCloud tracks bugs, code smells, and coverage trends.
- CI keeps quality checks consistent across platforms.

---

## **Learning Tests**
- Small experiments validate library behavior and assumptions.
- Keep them isolated and focused on one question at a time.

---

## **Bibliography**
- JUnit 4: https://junit.org/junit4/
- AssertJ Swing: https://joel-costigliola.github.io/assertj/assertj-swing.html
- Mockito: https://site.mockito.org/
- Awaitility: https://github.com/awaitility/awaitility
- PIT Mutation Testing: https://pitest.org/
- Maven: https://maven.apache.org/
- SonarCloud: https://sonarcloud.io/

---

## **Acknowledgments**
Thanks to **SonarCloud** and **Coveralls** for providing code quality and coverage analysis tools.

