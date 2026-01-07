[![Java CI with Maven in Linux](https://github.com/syedali430/inventorymanager/actions/workflows/maven.yml/badge.svg)](https://github.com/syedali430/inventorymanager/actions/workflows/maven.yml) [![Java CI with Maven, Docker and SonarCloud in Linux](https://github.com/syedali430/inventorymanager/actions/workflows/sonar.yml/badge.svg)](https://github.com/syedali430/inventorymanager/actions/workflows/sonar.yml) [![Java CI with Maven with Windows and Mac](https://github.com/syedali430/inventorymanager/actions/workflows/mac_win_maven.yml/badge.svg)](https://github.com/syedali430/inventorymanager/actions/workflows/mac_win_maven.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=syedali430_inventorymanager&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=syedali430_inventorymanager) [![Bugs](https://sonarcloud.io/api/project_badges/measure?project=syedali430_inventorymanager&metric=bugs)](https://sonarcloud.io/summary/new_code?id=syedali430_inventorymanager) [![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=syedali430_inventorymanager&metric=code_smells)](https://sonarcloud.io/summary/new_code?id=syedali430_inventorymanager) [![Coverage](https://sonarcloud.io/api/project_badges/measure?project=syedali430_inventorymanager&metric=coverage)](https://sonarcloud.io/summary/new_code?id=syedali430_inventorymanager) [![Technical Debt](https://sonarcloud.io/api/project_badges/measure?project=syedali430_inventorymanager&metric=sqale_index)](https://sonarcloud.io/summary/new_code?id=syedali430_inventorymanager) [![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=syedali430_inventorymanager&metric=vulnerabilities)](https://sonarcloud.io/summary/new_code?id=syedali430_inventorymanager) [![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=syedali430_inventorymanager&metric=sqale_rating)](https://sonarcloud.io/summary/new_code?id=syedali430_inventorymanager) [![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=syedali430_inventorymanager&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=syedali430_inventorymanager)

# **Inventory Manager**

An inventory management system built in Java with MongoDB integration, providing CRUD operations for managing items. This project follows a **Test-Driven Development (TDD)** approach, with a focus on achieving **100% code coverage**.

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

## **Acknowledgments**
Thanks to **SonarCloud** and **Coveralls** for providing code quality and coverage analysis tools.
