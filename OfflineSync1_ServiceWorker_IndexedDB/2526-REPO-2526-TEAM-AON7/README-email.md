# Uitnodigingsmail versturen (Handshake / bedrijven)

Deze pagina beschrijft hoe je lokaal **echte SMTP naar Mailpit** gebruikt om uitnodigingsmails te testen, en wat het verschil is met de standaardstart.

## Wat er bij is gekomen

In **`docker-compose.yml`** draait naast **PostgreSQL** nu ook **Mailpit**:

| Service    | Container       | Poorten   | Doel |
|-----------|-----------------|-----------|------|
| `postgres` | `aon_postgres` | `5432`    | Database |
| **`mailpit`** | **`aon_mailpit`** | **`8025`** (web), **`1025`** (SMTP) | Vangt alle uitgaande testmail op |

Mailpit is **geen echte mailbox op internet**: alles wat je app via SMTP naar `localhost:1025` stuurt, verschijnt in de **Mailpit-webinterface**. Zo kun je veilig testen zonder Gmail.

## Stappen: containers + backend + frontend

### 1. Docker (database + Mailpit)

In de **root van het project** (waar `docker-compose.yml` staat):

```bash
docker compose up -d
```

Je start daarmee **twee** containers: PostgreSQL én Mailpit.  
Controleer met `docker compose ps` of beide **running** zijn.

### 2. Backend met mailprofiel

Voor uitnodigingsmail via **Mailpit** moet Spring het profiel **`mailpit`** actief hebben. Dat schakelt SMTP in naar `127.0.0.1:1025` (zie `application-mailpit.properties`).

```bash
./gradlew bootRun --args='--spring.profiles.active=mailpit'
```

**Zonder** dit profiel (alleen `./gradlew bootRun`):

- wordt er **geen** verbinding met Mailpit gemaakt;
- zonder extra `.env` / `MAIL_SMTP_*` gaat mail naar de **console-mailer** (je ziet de inhoud in de terminal, geen Mailpit).

### 3. Frontend

Zoals gewoonlijk, bijvoorbeeld:

```bash
cd frontend
npm run dev
```

De UI draait typisch op **http://localhost:5173**. Log in als **coördinator**, ga naar een event → **Bedrijven** → uitnodiging aanmaken → **Mail versturen**.

## Mail bekijken (Mailpit)

Open in je browser:

**http://localhost:8025**

Daar zie je de “inbox” van Mailpit: onderwerp, afzanger, ontvanger en inhoud van elke mail die de backend naar poort **1025** heeft gestuurd.  
Dat is de manier om te controleren of **Mail versturen** in de app goed is gelopen.

## Kort overzicht

1. `docker compose up -d` → Postgres + **nieuwe Mailpit-container**  
2. `./gradlew bootRun --args='--spring.profiles.active=mailpit'` → backend stuurt mail naar Mailpit  
3. **http://localhost:8025** → mails bekijken en testen  



---

