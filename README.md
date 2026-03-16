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

## Placeholder per LPC Chat Formatter

Se `PlaceholderAPI` e `LPC Chat Formatter` sono installati, questo plugin registra automaticamente i placeholder:

- `%battleforgeclans_clan_name%`
- `%battleforgeclans_clan_role%`
- `%battleforgeclans_in_clan%`

Valori di fallback:

- `clan_name`: stringa vuota se il player non e in un clan
- `clan_role`: stringa vuota se il player non e in un clan
- `in_clan`: `yes` oppure `no`

Esempio formato LPC:

```yaml
format: '%vault_prefix% %player_name% &8[&6%battleforgeclans_clan_name%&8] &7[&b%battleforgeclans_clan_role%&7] &8» &f%message%'
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
