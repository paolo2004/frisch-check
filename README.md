# 🥬 FrischCheck

**FrischCheck** ist eine interne Anwendung für die Restaurantküche. Sie erfasst Lebensmittel mit Ablaufdatum, warnt rechtzeitig per
E-Mail und/oder Telegram und zeigt eine FEFO-sortierte Übersicht (First Expired, First Out).
Produkte werden nie gelöscht, sondern als **VERBRAUCHT** oder **WEGGEWORFEN** markiert. So bleiben Verluste messbar.

**Stack:** Java 21 · Spring Boot 3.5 · Maven · JPA/Hibernate · H2 (Entwicklung) / PostgreSQL (Profil `prod`) · Thymeleaf

## Warum diese Schichten?

| Package | Aufgabe | Warum getrennt? |
|---|---|---|
| `controller` | Nimmt HTTP-Requests an, validiert Eingaben (`@Valid`), gibt DTOs zurück. **Keine Logik.** | `api` (JSON) und `web` (HTML) teilen sich dieselben Services. Eine Oberfläche kommt ohne doppelte Logik dazu. |
| `service` | Geschäftsregeln: Ablauf-Stufen, Statuswechsel, Benachrichtigung. | Ohne Web und DB mit Mockito testbar. Hier steckt der eigentliche Wert der App. |
| `repository` | Datenbankzugriff über Spring Data (Methodennamen werden zu SQL). | H2 → PostgreSQL ohne Code-Änderung austauschbar. |
| `model` | JPA-Entities (Tabellen) und Enums. | Datenbankstruktur an einer Stelle. |
| `dto` | Was die API rein- und rauslässt. | Entities bleiben intern: keine ungewollten Felder in der API, keine Lazy-Loading-Fehler, und die API bleibt stabil, auch wenn die DB sich ändert. |
| `config` | Beans, typsichere Properties, Initialdaten. | Konfiguration getrennt von Logik. Werte stehen in der `application.yml` statt im Code. |
| `exception` | Eigene Exceptions und `GlobalExceptionHandler`. | Ein einheitliches Fehlerformat, ohne try/catch in jedem Controller. |

**Ablauf-Stufen** (pro Kategorie konfigurierbar):

Tage bis Ablauf  < 0              -> ABGELAUFEN
Tage bis Ablauf <= critical-days  -> KRITISCH
Tage bis Ablauf <= warning-days   -> WARNUNG
sonst                             -> OK


## Starten

Voraussetzung: **JDK 21 oder neuer**. Maven muss nicht installiert sein, das erledigt der Wrapper.

```bash
# Windows (PowerShell)
.\mvnw.cmd spring-boot:run

# macOS / Linux / Git Bash
./mvnw spring-boot:run
```

Danach:

- **Oberfläche:** http://localhost:8080 (auch am Handy im selben WLAN: `http://<IP-deines-PCs>:8080`)
- **H2-Konsole:** http://localhost:8080/h2-console (JDBC-URL `jdbc:h2:file:./data/restaurant-db`, User `sa`, ohne Passwort)
- **Tests:** `.\mvnw.cmd test`

> **Fehler „PKIX path building failed“ beim ersten Build?** Ein Virenscanner oder Firmen-Proxy prüft
> HTTPS-Verbindungen, und Java kennt dessen Zertifikat nicht. Lösung: Java den Windows-Zertifikatsspeicher nutzen lassen:
> ```powershell
> $env:MAVEN_OPTS='-Djavax.net.ssl.trustStoreType=Windows-ROOT'
> .\mvnw.cmd spring-boot:run
> ```

### Mit PostgreSQL (Profil `prod`)

```bash
docker run -d --name frischcheck-db -e POSTGRES_DB=frischcheck -e POSTGRES_USER=frischcheck \
  -e POSTGRES_PASSWORD=frischcheck -p 5432:5432 postgres:17

./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
```

Zugangsdaten per Umgebungsvariablen: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`.

---

## Benachrichtigungen einrichten

Beide Kanäle sind standardmäßig **aus**. Ohne aktiven Kanal landet die Warnung nur im Log.
Mehrere Kanäle gleichzeitig sind möglich. Fällt einer aus, werden die anderen trotzdem bedient.

### Telegram

1. In Telegram **@BotFather** öffnen → `/newbot` → Token kopieren.
2. Dem neuen Bot eine Nachricht schreiben (oder ihn in die Küchen-Gruppe aufnehmen).
3. Chat-ID holen: `https://api.telegram.org/bot<TOKEN>/getUpdates` im Browser öffnen → `"chat":{"id": ...}`.
4. Starten:
   ```powershell
   $env:TELEGRAM_BOT_TOKEN='123456:ABC...'
   $env:TELEGRAM_CHAT_ID='-1001234567890'
   .\mvnw.cmd spring-boot:run "-Dspring-boot.run.arguments=--app.notification.telegram.enabled=true"
   ```

### E-Mail

In `application.yml` den auskommentierten Block `spring.mail` aktivieren (z. B. Gmail mit
[App-Passwort](https://myaccount.google.com/apppasswords)), dann:

```powershell
$env:MAIL_USERNAME='dein.name@gmail.com'; $env:MAIL_PASSWORD='app-passwort'
$env:MAIL_FROM='dein.name@gmail.com';     $env:MAIL_TO='kueche@restaurant.de'
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.arguments=--app.notification.email.enabled=true"
```

> Tokens und Passwörter gehören **nie** in die `application.yml` im Git-Repo, sondern nur in Umgebungsvariablen.

### Uhrzeit & Schwellen ändern

Alles in `src/main/resources/application.yml`:

```yaml
app:
  expiry:
    check-cron: "0 0 8 * * *"      # täglich 08:00 – z. B. "0 30 7 * * MON-SAT" = Mo–Sa 07:30
    categories:
      - name: Frischware
        warning-days: 5
        critical-days: 2
```

Kategorien aus der YAML werden nur beim **ersten** Start angelegt. Spätere Änderungen an bestehenden
Kategorien laufen über die API (`PUT /api/categories/{id}`).

---

## REST-API – Beispiel-Requests

Basis-URL: `http://localhost:8080`. (Unter Windows PowerShell `curl.exe` statt `curl` verwenden, oder Git Bash.)

### Kategorien

```bash
# Alle Kategorien (mit IDs – brauchst du beim Anlegen von Produkten)
curl http://localhost:8080/api/categories

# Neue Kategorie
curl -X POST http://localhost:8080/api/categories \
  -H "Content-Type: application/json" \
  -d '{"name":"Obst & Gemüse","warningDays":4,"criticalDays":1}'

# Schwellen ändern
curl -X PUT http://localhost:8080/api/categories/1 \
  -H "Content-Type: application/json" \
  -d '{"name":"Frischware","warningDays":4,"criticalDays":2}'
```

### Produkte

```bash
# Produkt erfassen (receivedDate optional -> heute)
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{
        "name": "Schlagsahne",
        "categoryId": 3,
        "storageLocation": "KUEHLSCHRANK",
        "quantity": 3,
        "unit": "LITER",
        "expiryDate": "2026-10-02",
        "receivedDate": "2026-09-29"
      }'

# Bald ablaufende Produkte (FEFO)
curl http://localhost:8080/api/products/expiring

# Alle Produkte, optional gefiltert
curl "http://localhost:8080/api/products?status=WEGGEWORFEN"

# Einzelnes Produkt / ändern
curl http://localhost:8080/api/products/1
curl -X PUT http://localhost:8080/api/products/1 -H "Content-Type: application/json" -d '{ ...wie POST... }'

# Als verbraucht oder weggeworfen markieren
curl -X PATCH http://localhost:8080/api/products/1/status \
  -H "Content-Type: application/json" \
  -d '{"status":"WEGGEWORFEN"}'

# Ablaufprüfung sofort auslösen (statt bis 08:00 zu warten)
curl -X POST http://localhost:8080/api/expiry-check
```

Erlaubte Werte:
- `storageLocation`: `KUEHLSCHRANK`, `KUEHLHAUS`, `TIEFKUEHLER`, `TROCKENLAGER`, `GETRAENKELAGER`, `BAR`
- `unit`: `STUECK`, `KG`, `GRAMM`, `LITER`, `MILLILITER`, `PACKUNG`, `FLASCHE`, `KISTE`
- `status`: `AUF_LAGER`, `VERBRAUCHT`, `WEGGEWORFEN`

### Beispiel-Antwort

```json
{
  "id": 1,
  "name": "Schlagsahne",
  "categoryId": 3,
  "categoryName": "Milchprodukte",
  "storageLocation": "KUEHLSCHRANK",
  "quantity": 3,
  "unit": "LITER",
  "expiryDate": "2026-09-30",
  "receivedDate": "2026-09-29",
  "status": "AUF_LAGER",
  "daysUntilExpiry": 1,
  "expiryLevel": "KRITISCH",
  "expiryText": "läuft morgen ab"
}
```

### Fehlerformat

```json
{
  "timestamp": "2026-09-29T12:31:21",
  "status": 400,
  "error": "Bad Request",
  "message": "Validierung fehlgeschlagen",
  "path": "/api/products",
  "fieldErrors": { "name": "darf nicht leer sein", "quantity": "muss größer als 0 sein" }
}
```

| Status | Wann |
|---|---|
| 400 | Validierung fehlgeschlagen, kaputtes JSON, unbekannter Enum-Wert |
| 404 | Produkt/Kategorie existiert nicht |
| 409 | Fachregel verletzt (z. B. weggeworfenes Produkt erneut ändern, Kategorie doppelt) |
| 500 | Unerwarteter Fehler (Details nur im Server-Log) |

### Beispiel-Benachrichtigung

```
Ablaufwarnung: 2 Produkt(e) brauchen Aufmerksamkeit

Stand: 29.09.2026

ABGELAUFEN (1)
- Hackfleisch (Fleisch & Fisch) – 2.5 kg – Kühlhaus – seit 2 Tagen abgelaufen (27.09.2026)

KRITISCH (1)
- Schlagsahne (Milchprodukte) – 3 l – Kühlschrank – läuft morgen ab (30.09.2026)
```
=======
# frisch-check
