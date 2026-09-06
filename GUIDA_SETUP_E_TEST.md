# Anime Explorer — Guida Setup, Avvio e Test (v2)

Dall'installazione dei prerequisiti fino al test di ogni endpoint e pagina.

---

## 1. Prerequisiti

| Software | Versione minima | Verifica |
|---|---|---|
| Node.js | 18+ | `node -v` |
| Java JDK | 17+ | `java -version` |
| Gradle | 8+ (o wrapper) | `gradle -v` |
| PostgreSQL | 14+ | `psql --version` |
| MongoDB | 6+ | `mongod --version` |

> **Alternativa Docker**: `docker compose up -d` dalla root avvia PostgreSQL 16 e MongoDB 7 già configurati (db `animedb`, utente `postgres`/`postgres`). Se usi Docker salta la sezione 3.

> **Windows**: usare `gradlew.bat` al posto di `./gradlew`.

---

## 2. Dataset

Estrai i CSV di `datasets.zip` in `data/` alla root. Mapping file→database in `data/README.md`.

---

## 3. Setup database (senza Docker)

```bash
# PostgreSQL
psql -U postgres -c "CREATE DATABASE animedb;"

# MongoDB: nessuna creazione manuale, il db nasce al primo import
```

Se le credenziali PostgreSQL differiscono da `postgres`/`postgres`, modifica
`solution/spring-boot-server/src/main/resources/application.properties`.

---

## 4. Import dati

### 4.1 PostgreSQL (statici)

```bash
cd solution/spring-boot-server
./gradlew bootRun --args='--import-data'
```

Log atteso: `[details.csv] done: 28955 rows imported, 0 skipped` ecc. Al termine chiudi con `Ctrl+C`.
L'import è **idempotente** (TRUNCATE prima di ogni file): puoi rilanciarlo quando vuoi.

Verifica:

```bash
psql -U postgres -d animedb -c "SELECT COUNT(*) FROM anime_details;"
psql -U postgres -d animedb -c "SELECT COUNT(*) FROM characters;"
psql -U postgres -d animedb -c "SELECT title, score FROM anime_details ORDER BY score DESC NULLS LAST LIMIT 5;"
```

### 4.2 MongoDB (dinamici)

```bash
cd solution/express-mongo-server
npm install
npm run import                      # tutti i file
node scripts/import-data.js --only stats,profiles,favs,recommendations   # selettivo
```

`ratings.csv` (~124M righe: `username,anime_id,status,score,is_rewatching,num_watched_episodes`) richiede parecchi minuti. Nota: `score` 0 = "in lista ma non votato" (convenzione MyAnimeList): le righe vengono importate tutte, ma medie e distribuzioni considerano solo i voti 1-10. Inoltre: il log mostra il progresso ogni 1M righe e gli **indici vengono creati a fine import** (più veloce). Con `--only` puoi importare prima i file piccoli e lanciare ratings per ultimo.

Verifica:

```bash
mongosh animedb --eval "db.ratings.countDocuments()"
mongosh animedb --eval "db.stats.countDocuments()"
mongosh animedb --eval "db.ratings.findOne()"
```

---

## 5. Avvio (3 terminali)

```bash
# T1 — Spring Boot (8080)
cd solution/spring-boot-server && ./gradlew bootRun

# T2 — Express MongoDB (3002)
cd solution/express-mongo-server && npm start

# T3 — Gateway + frontend (3000)
cd solution/central-server && npm install && npm start
```

Apri **http://localhost:3000** → homepage con top anime e statistiche.

| Swagger | URL |
|---|---|
| Gateway | http://localhost:3000/api-docs |
| MongoDB server | http://localhost:3002/api-docs |
| Spring Boot | http://localhost:8080/swagger-ui.html |

---

## 6. Test API — Spring Boot (8080)

```bash
# Anime
curl "http://localhost:8080/api/anime/search?q=Naruto&page=0&size=5"
curl "http://localhost:8080/api/anime/search?type=TV&genre=Action"
curl "http://localhost:8080/api/anime/top?limit=10"
curl "http://localhost:8080/api/anime/1"
curl "http://localhost:8080/api/anime/1/characters"          # <- con nomi e immagini
curl "http://localhost:8080/api/anime/batch?ids=1,5,20"      # <- summaries per enrichment
curl "http://localhost:8080/api/anime/genres"
curl "http://localhost:8080/api/anime/types"
curl "http://localhost:8080/api/anime/genre-stats"
curl "http://localhost:8080/api/anime/type-stats"

# Personaggi
curl "http://localhost:8080/api/characters/search?q=Luffy"
curl "http://localhost:8080/api/characters/search"           # blank -> più popolari
curl "http://localhost:8080/api/characters/1"
curl "http://localhost:8080/api/characters/1/nicknames"
curl "http://localhost:8080/api/characters/1/appearances"    # <- con titoli anime

# Persone
curl "http://localhost:8080/api/people/search?q=Miyazaki"
curl "http://localhost:8080/api/people/1"
curl "http://localhost:8080/api/people/1/alternate-names"
curl "http://localhost:8080/api/people/1/anime-works"        # <- con titoli
curl "http://localhost:8080/api/people/1/voice-works"        # <- con titoli + nomi personaggi
```

Casi limite: `curl -i http://localhost:8080/api/anime/999999999` → **404**; `curl -i "http://localhost:8080/api/anime/abc"` → **400** JSON.

## 7. Test API — Express MongoDB (3002)

```bash
curl "http://localhost:3002/api/ratings/anime/1"             # media + distribuzione
curl "http://localhost:3002/api/ratings/user/ishikawas?page=0&size=10"   # per username
curl "http://localhost:3002/api/ratings/distribution"
curl "http://localhost:3002/api/recommendations/anime/1"
curl "http://localhost:3002/api/recommendations/most-recommended?limit=10"
curl "http://localhost:3002/api/favs/top?type=anime&limit=10"
curl "http://localhost:3002/api/favs/top?type=character&limit=10"
curl "http://localhost:3002/api/favs/user/ishikawas"
curl "http://localhost:3002/api/favs/anime/1"
curl "http://localhost:3002/api/stats/overview"
curl "http://localhost:3002/api/stats/anime/1"
curl "http://localhost:3002/api/profiles/ishikawas"   # per username (il dataset non ha id numerici)
```

Caso limite: `curl -i http://localhost:3002/api/stats/anime/999999999` → **404**.

## 8. Test Gateway (3000)

Stessi path del backend, via proxy:

```bash
curl "http://localhost:3000/api/anime/top?limit=3"
curl "http://localhost:3000/api/anime/1/characters"
curl "http://localhost:3000/api/ratings/anime/1"
curl "http://localhost:3000/api/stats/overview"
curl "http://localhost:3000/api/recommendations/most-recommended?limit=5"
```

Test resilienza: spegni il server Spring Boot e ricarica la home → la pagina si carica comunque con alert "No data available" (degradazione controllata), niente crash.

---

## 9. Test pagine frontend

| URL | Verifica |
|---|---|
| `/` | hero, 4 card statistiche, griglia top 12, lista most favourited **con titoli** |
| `/search?q=Naruto` | risultati, badge tipo/score |
| `/search?type=Movie&genre=Action&page=1` | filtri + paginazione |
| `/anime/1` | info, badge MAL score + rating utenti + favourites, synopsis, **grafico distribuzione rating**, watch status, **personaggi con nomi/foto**, **raccomandazioni con titoli** |
| `/characters` e `/characters?q=Lelouch` | griglia popolarità / ricerca |
| `/character/1` | kanji, nickname, about, tabella **apparizioni con titoli** |
| `/people` e `/people?q=Miyazaki` | griglia / ricerca |
| `/person/1` | alt names, **ruoli doppiaggio con titoli e personaggi**, production works |
| `/statistics` | 4 card + grafico generi (barre orizzontali), formati (doughnut), distribuzione rating, top 10 |
| `/recommendations` | classifica "most recommended" **con titoli** |
| `/recommendations?animeId=1` | lista consigliati per Cowboy Bebop **con titoli** |
| `/pagina-inesistente` | pagina 404 con link home |

Responsive: restringi la finestra → navbar collassa in hamburger, griglie passano a 2 colonne.

---

## 10. Checklist finale

- [ ] Import PostgreSQL senza errori, conteggi coerenti coi CSV
- [ ] Import MongoDB completato, indici creati
- [ ] 3 server attivi, 3 Swagger raggiungibili
- [ ] Tutti i curl delle sezioni 6-8 rispondono
- [ ] 404/400 gestiti con JSON (API) e pagina errore (frontend)
- [ ] Tutte le pagine della sezione 9 renderizzano con dati reali
- [ ] Home degrada senza crash a backend spenti
- [ ] Test unitari Spring: `./gradlew test`

---

## 11. Troubleshooting

**`ECONNREFUSED :5432` / Spring non parte** → PostgreSQL spento o credenziali errate in `application.properties`.

**`MongoServerError: connect ECONNREFUSED :27017`** → `sudo systemctl start mongod` (Linux) / `brew services start mongodb-community` (macOS) / `net start MongoDB` (Windows), oppure `docker compose up -d`.

**Import: molte righe `skipped`** → header CSV diversi dagli attesi. Controlla `head -1 data/details.csv` e aggiungi il nome colonna ai candidati in `CsvImportRunner.java` (Spring) o `scripts/import-data.js` (Mongo): i parser accettano più nomi per campo.

**`EADDRINUSE`** → porta occupata: `lsof -i :3000` e `kill <PID>` (Windows: `netstat -ano | findstr :3000`).

**Frontend senza dati ma server attivi** → console browser (F12) + log del gateway; testa i backend direttamente (sezioni 6-7).

**`gradlew` mancante o jar wrapper assente** → con Gradle installato: `gradle wrapper` dentro `solution/spring-boot-server`.

---

## 12. Reset rapido

```bash
# PostgreSQL: l'import fa già TRUNCATE, ma per ripartire da zero:
psql -U postgres -c "DROP DATABASE animedb;" && psql -U postgres -c "CREATE DATABASE animedb;"

# MongoDB
mongosh animedb --eval "db.dropDatabase()"
```
