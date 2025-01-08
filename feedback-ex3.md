# Bewertung des 3. Arbeitsblatts

> Das Arbeitsblatt besteht aus 30 Punkten, von denen 15 zum Bestehen notwendig sind.

## Übersicht der erreichten Punkte

| Kriterium                     | erreichte Punkte / maximale Punkte |
| ----------------------------- |------------------------------|
| Frontend-Funktionalität       | 22 / 22                      |
| Usability                     | 3 / 3                        |
| Repository, Config und README | 5 / 5                        |
| **Summe**:                    | 30 / 30                      |
| **Bestanden**:                | ja                           |

## Funktionalität des Frontends (22 / 22)
> Anforderungen für maximale Punktzahl: Frontend hat alle geforderten Views / View-Elemente mit korrekter Funktionalität, Kommunikation mit der API, Browser-Console (F12) enthält keine Fehler

### Bewertung

- [x] (8 / 8 Punkte) View zur Verwaltung von `Assignees`
  - [x] (1 Punkt) View-Elemente für `GET /assignees`
  - [x] (1 Punkt) View-Elemente für `GET /assignees/$id`
  - [x] (2 Punkte) View-Elemente für `POST /assignees`
  - [x] (3 Punkte) View-Elemente für `PUT /assignees/$id`
  - [x] (1 Punkt) View-Elemente für `DELETE /assignees/$id`
- [x] (11 / 11 Punkte) View zur Verwaltung von `ToDos`
  - [x] (1 Punkt) View-Elemente für `GET /todos`
  - [x] (1 Punkt) View-Elemente für `GET /todos/$id`
  - [x] (2 Punkte) View-Elemente für `POST /todos`
  - [x] (2 Punkte) View-Elemente für `PUT /todos/$id`
  - [x] (1 Punkt) View-Elemente für `DELETE /todos/$id`
  - [x] (1 Punkt) **Einfaches** Abhaken von `ToDos` möglich
  - [x] (1 Punkt) Abgehakte `ToDos` sind **getrennt** von offenen
  - [x] (1 Punkt) `ToDos` sind **filterbar** nach `title`
  - [x] (1 Punkt) `ToDos` sind **sortierbar** nach `title` und `dueDate`
- [x] (2 Punkte) HTTP-Kommunikation mit dem gestarteten Backend (`api`) funktioniert für einen Teil der Ressourcen (3-4 Ressourcen: 1 Punkt, ab 5 Ressourcen: 2 Punkte)
- [x] (1 Punkt) Browser-Console (F12) zeigt keine JS Errors an

## Usability (3 / 3)

- [x] (1 Punkt) Gängige UI Components werden passend eingesetzt (z.B. Tables, Menus/Selects, Inputs, Switches, Toasts/Dialogs/Alerts etc.)
- [x] (1 Punkt) UI ist verständlich und stellt notwendige Informationen effizient bereit (z.B. wenige Klicks notwendig)
- [x] (1 Punkt) Terminologie, Farben und Layout sind konsistent

## Repository, Config und README (5 / 5)

- [x] (1 Punkt) Repository-Struktur folgt dem Beispielprojekt (`api` und `frontend` auf höchster Ebene)
- [x] (1 Punkt) Keine IDE-spezifischen Dateien (z.B. `.idea`, `.vscode`, etc.) oder Binaries / Libraries (z.B. `target`, `node_modules`, etc.) im Repository
- [x] (1 Punkt) Git tag `v0.3` vorhanden
- [x] (1 Punkt) DB- und API-Konfiguration entspricht dem Beispielprojekt (keine Änderungen für Korrektur nötig)
- [x] (0,5 Punkte) Keine Dummy-Dateien oder irrelevanter Text aus dem Beispiel-Projekt mehr in `frontend`
- [x] (0,5 Punkte) README in `frontend` enthält klare, IDE-unabhängige Anweisungen zur Ausführung der Applikation
