# Clans

Clans is a Paper 1.21.11 plugin that provides a modular foundation for a clan system.

## Requirements

- Java 21
- Paper 1.21.11

## Current Structure

The plugin is built with a simple and clean architecture.

The main class (`ClansPlugin`) is responsible only for initialization and command registration.

The `/clans` command acts as a root dispatcher. Each subcommand is implemented in its own class to keep the code organized and easy to extend.

All messages are handled through a dedicated message utility class to ensure consistent formatting and easier future changes.

The actual clan logic will be implemented inside a service layer, keeping command handling separated from business logic.

## Build

```bash
./gradlew build
```

## Automated Tests

### 1) Unit tests (fast)

- Scope: model + core service rules.
- Command (Gradle):

```bash
./gradlew testUnit
```

- Command (PowerShell):

```powershell
.\scripts\test-unit.ps1
```

### 2) Workflow tests (integration-level service flows)

- Scope: end-to-end service flows (create/invite/accept/roles/kick/delete).
- Command (Gradle):

```bash
./gradlew testWorkflow
```

- Command (PowerShell):

```powershell
.\scripts\test-workflow.ps1
```

### 3) Production gate

- Scope: clean workspace + full test suite + build artifact.
- Command (Gradle):

```bash
./gradlew testProductionGate
```

- Command (PowerShell):

```powershell
.\scripts\test-production-gate.ps1
```