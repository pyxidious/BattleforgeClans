# Clans

Plugin Paper 1.21.11 per la gestione dei clan.

## Requisiti

- Java 21
- Paper 1.21.11

---

## Struttura attuale

it.battleforge.clans
├─ ClansPlugin.java # Main del plugin
├─ command/
│ ├─ ClansCommand.java # Comando principale (/clans)
│ ├─ SubCommand.java # Interfaccia per i sottocomandi
│ └─ sub/
│ └─ HelpSubCommand.java # /clans listacomandi
├─ message/
│ └─ Messages.java # Gestione centralizzata dei messaggi
└─ service/

## Build

```bash
./gradlew build