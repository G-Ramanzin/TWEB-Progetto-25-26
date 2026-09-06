# Report — Anime Explorer (TWEB 2025-2026)

> **Max 5 pagine** (indice escluso). Struttura conforme all'Appendix A della consegna.
> Convertire in PDF prima della consegna. Le sezioni [no marks] vanno comunque compilate.

## Index Table *(opzionale, fuori dal conteggio pagine)*

## Introduction *(no marks)*
- Cosa aspettarsi dal report, panoramica onesta del lavoro svolto.

---

## 1. Central Server (Express gateway)
### Solution — design e motivazioni
- Gateway leggero: rendering Handlebars + proxy axios, nessuna elaborazione dati.
- Perché: requisito "no heavy duties"; separazione presentazione/dati; scalabilità.
- Enrichment leggero (merge id→titoli via endpoint batch) e perché non è "heavy duty".
### Issues
- Gestione fallimenti dei backend (fallback + pagine degradate).
### Requirements
- Conformità: Express, axios obbligatorio, Handlebars, Swagger.
### Limitations
- Niente cache; possibile aggiunta di un layer Redis; timeout fisso.

## 2. Spring Boot Server (PostgreSQL, dati statici)
### Solution
- Entità JPA senza FK (import orfano-tollerante), indici su colonne di ricerca.
- DTO + JPQL `JOIN ... ON` per esporre nomi/titoli in una sola chiamata.
- Import: opencsv + JdbcTemplate batch (motivare vs JPA saveAll con chiavi IDENTITY).
- Scelte di naming: tabella `characters` (parola riservata), colonna `anime_rank`.
### Issues
- Header CSV variabili → parser tollerante con nomi candidati; campi TEXT.
### Requirements
- Spring Boot + PostgreSQL per dati statici; Swagger (springdoc); Javadoc.
### Limitations
- Ricerca LIKE non full-text; possibile estensione con indici trigram.

## 3. Express MongoDB Server (dati dinamici)
### Solution
- Un modulo route per collection; aggregation pipeline per distribuzioni e top.
- Import streaming (readline) + insertMany ordered:false; indici creati a fine import.
### Issues
- 124M righe di ratings: memoria piatta, batch, estimatedDocumentCount per i totali.
### Requirements
- Express + MongoDB per dati dinamici; Swagger (swagger-jsdoc); JSDoc.
### Limitations
- Nessuna scrittura (API read-only); aggregazioni globali costose se non indicizzate.

## 4. Frontend (Handlebars + Bootstrap + Chart.js)
### Solution
- Layout unico + partials; grafici via attributi data-* letti da app.js.
- Pagine: home, search con filtri, dettagli anime/personaggio/persona, statistics, recommendations.
### Issues
- Dati mancanti/orfani → empty states e fallback immagini.
### Requirements
- HTML+JS+CSS+Bootstrap+Handlebars (nessun altro framework).
### Limitations
- Rendering server-side puro; niente SPA routing.

---

## Conclusions *(no marks)*
- Lezioni apprese (batching, naming SQL, divisione statico/dinamico).

## Division of Work *(MANDATORY)*
- Chi ha fatto cosa, con precisione (commit come evidenza).

## Extra Information *(MANDATORY)*
- Config extra necessaria (credenziali PostgreSQL, path data/, docker-compose opzionale)
  oppure dichiarare esplicitamente che non serve nulla.
- **Uso della Generative AI**: spiegare come è stata usata (richiesto dalla consegna).

## Bibliography *(no marks)*
- Documentazione ufficiale citata nel testo (Spring, Express, Mongoose, Chart.js...).
