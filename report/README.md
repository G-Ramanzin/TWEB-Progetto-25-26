# Report — scaletta (max 5 pagine, Appendix A)

Struttura richiesta dalla consegna. Una sottosezione per OGNI task tecnico,
ciascuna con: **Solution (design + motivazioni) / Issues / Requirements / Limitations**.

1. **Introduction** (no marks) — cosa aspettarsi dal report, onestà sul lavoro svolto.
2. **Task: Central server (Express gateway)**
   - Solution: gateway leggero, rendering Handlebars, proxy axios, nessun accesso DB.
   - Issues: gestione errori dei backend, timeout, enrichment raccomandazioni.
   - Requirements: "must not perform any heavy duties" → solo merge leggeri di risposte.
   - Limitations: no cache, no bilanciamento.
3. **Task: Spring Boot server (PostgreSQL, dati statici)**
   - Solution: JPA per lo schema, JDBC batch per l'import, DTO con JOIN per nomi.
   - Issues: parola riservata `character`, campi TEXT, chiavi orfane, performance import.
   - Requirements: dati statici su Postgres, Swagger, Javadoc.
   - Limitations: ricerca LIKE non full-text.
4. **Task: Express MongoDB server (dati dinamici)**
   - Solution: mongoose, route modulari, aggregation pipeline, indici post-import.
   - Issues: 124M righe di ratings → streaming import, insertMany ordered:false.
   - Requirements: dati dinamici su MongoDB, Swagger, JSDoc.
   - Limitations: aggregazioni globali costose senza pre-calcolo.
5. **Task: Frontend (Handlebars + Bootstrap + Chart.js)**
   - Solution: server-side rendering, partials riusabili, grafici da data-attributes.
   - Issues: paginazione, dati mancanti (immagini/score nulli).
   - Requirements: HTML+JS+CSS+Bootstrap+Handlebars, nessun altro framework.
   - Limitations: no client-side routing.
6. **Conclusions** (no marks) — lezioni apprese.
7. **Division of Work** (MANDATORY) — chi ha fatto cosa, con precisione.
8. **Extra Information** (MANDATORY) — istruzioni extra per l'esecuzione o "nessuna
   configurazione extra necessaria". Dichiarare qui l'uso di GenAI come richiesto.
9. **Bibliography** — fonti citate nel testo.

> Ricorda: allegare anche il **self assessment form** compilato.
