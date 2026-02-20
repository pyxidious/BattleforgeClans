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