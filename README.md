# Anime Explorer — Assignment TWEB 2025-2026

Applicazione web full-stack per esplorare e analizzare dati sugli anime, costruita
con l'architettura a microservizi richiesta dalla consegna.

## Architettura

```
                         ┌──────────────────────┐
   Browser ────────────▶ │   Server centrale    │  Express, porta 3000
   (Handlebars +         │   - rendering viste  │  NESSUN compito pesante:
    Bootstrap 5)         │   - proxy /api       │  solo rendering + chiamate axios
                         └───────┬──────┬───────┘
                                 │      │
                     axios       │      │       axios
                ┌────────────────┘      └───────────────┐
                ▼                                       ▼
   ┌─────────────────────────┐            ┌─────────────────────────┐
   │  Server Express MongoDB │            │  Server Spring Boot     │
   │  porta 3002             │            │  porta 8080             │
   │  Dati DINAMICI          │            │  Dati STATICI           │
   │  ratings, consigli,     │            │  anime, personaggi,     │
   │  preferiti, stats,      │            │  persone + relazioni    │
   │  profili                │            │                         │
   └───────────┬─────────────┘            └───────────┬─────────────┘
               ▼                                      ▼
           MongoDB                                PostgreSQL
```

## Distribuzione dei dati

| Sottoinsieme | Storage | File |
|---|---|---|
| **Statici** (cambiano raramente) | PostgreSQL via Spring Boot | details, characters, character_anime_works, character_nicknames, person_details, person_anime_works, person_voice_works, person_alternate_names |
| **Dinamici** (alta frequenza di cambiamento) | MongoDB via Express | ratings, recommendations, favs, stats, profiles |

## Avvio rapido

Le istruzioni complete (prerequisiti, import, test, troubleshooting) sono in
[`GUIDA_SETUP_E_TEST.md`](GUIDA_SETUP_E_TEST.md). In breve:

```bash
# 0. opzionale: avvia i database con Docker
docker compose up -d

# 1. metti i file CSV in data/

# 2. importa i dati statici in PostgreSQL
cd solution/spring-boot-server && ./gradlew bootRun --args='--import-data'

# 3. importa i dati dinamici in MongoDB
cd solution/express-mongo-server && npm install && npm run import

# 4. avvia i tre server (tre terminali)
cd solution/spring-boot-server   && ./gradlew bootRun     # :8080
cd solution/express-mongo-server && npm start             # :3002
cd solution/central-server       && npm install && npm start  # :3000
```

Poi apri **http://localhost:3000**.

## Documentazione Swagger

| Server | URL |
|---|---|
| Gateway centrale | http://localhost:3000/api-docs |
| Server Express MongoDB | http://localhost:3002/api-docs |
| Server Spring Boot | http://localhost:8080/swagger-ui.html |

## Struttura del progetto

```
anime-project/
├── data/                     # file CSV (non versionati)
├── solution/
│   ├── central-server/       # gateway Express + rendering viste (3000)
│   ├── express-mongo-server/ # Express + MongoDB, dati dinamici (3002)
│   ├── spring-boot-server/   # Spring Boot + PostgreSQL, dati statici (8080)
│   └── frontend/             # viste Handlebars + Bootstrap + Chart.js
├── report/                   # relazione di progetto (max 5 pagine) + self assessment
├── docker-compose.yml        # opzionale: database locali
└── GUIDA_SETUP_E_TEST.md     # guida completa di setup e test
```

## Membri del gruppo
- Gabriele Ramanzin — matricola 1074156
