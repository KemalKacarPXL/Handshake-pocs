# AON Research Project

## Vereisten

- Java 21
- Node.js 20.19+ of 22.12+
- Docker (voor PostgreSQL)

## Applicatie starten

### 1. Database starten

```bash
docker compose up -d
```

Dit start een PostgreSQL-container op poort **5432** (database: `aon_db`).

### 2. Backend starten (Spring Boot)

```bash
./gradlew bootRun
```

De API draait op **http://localhost:8080**.

### 3. Frontend starten (Vue + Vite)

```bash
cd frontend
npm install
npm run dev
```

De frontend draait op **http://localhost:5173**.

### 4. Ngrok om frontend op gsm te raadplegen (werkt met 4g)

```bash
ngrok http 5173
```

## Test accounts

Bij het opstarten worden automatisch de volgende accounts aangemaakt via de DataSeeder. Alle accounts gebruiken wachtwoord **`password123`**.

### Coordinator

| Email | Wachtwoord |
|-------|------------|
| coordinator@voorbeeld.be | password123 |

### Studenten

| Voornaam | Achternaam | Email | Opleiding |
|----------|------------|-------|-----------|
| Arda | Güler | arda.guler@student.pxl.be | Toegepaste Informatica |
| Emma | Janssen | emma.janssen@student.pxl.be | Toegepaste Informatica |
| Noah | Peeters | noah.peeters@student.pxl.be | Elektronica-ICT |
| Lotte | Mertens | lotte.mertens@student.pxl.be | Toegepaste Informatica |
| Lucas | Willems | lucas.willems@student.pxl.be | Elektronica-ICT |
| Sara | Claes | sara.claes@student.pxl.be | Toegepaste Informatica |
| Daan | Wouters | daan.wouters@student.pxl.be | Systemen en Netwerken |
| Julie | Maes | julie.maes@student.pxl.be | Toegepaste Informatica |

### Bedrijven

| Voornaam | Achternaam | Email | Locatie |
|----------|------------|-------|---------|
| Jan | De Smedt | jan.desmedt@cegeka.be | Hasselt |
| Sophie | Van Damme | sophie.vandamme@mobly.be | Antwerpen |
| Pieter | Hendrickx | pieter.hendrickx@cronos.be | Kontich |
| Lisa | Jacobs | lisa.jacobs@ordina.be | Mechelen |
| Tom | Lenaerts | tom.lenaerts@inetum-realdolmen.be | Brussel |
