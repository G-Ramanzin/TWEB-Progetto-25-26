/**
 * @fileoverview Entry point of the central Express gateway (port 3000).
 *
 * Responsibilities:
 *  - render the Handlebars frontend (views in ../frontend/views);
 *  - proxy /api requests to the two backend servers;
 *  - serve static assets and the Swagger UI.
 *
 * The gateway deliberately performs no data processing: all queries run
 * on the Spring Boot server (PostgreSQL) or on the Express MongoDB
 * server, keeping this process fast under load.
 */

const express = require('express');
const { engine } = require('express-handlebars');
const cors = require('cors');
const morgan = require('morgan');
const swaggerUi = require('swagger-ui-express');
const path = require('path');

const { PORT, VIEWS_DIR, PUBLIC_DIR } = require('./config');
const helpers = require('./hbs-helpers');
const swaggerSpec = require('./swagger');

const app = express();

// --- View engine: Handlebars with custom helpers ---
app.engine('hbs', engine({
    extname: '.hbs',
    defaultLayout: 'main',
    layoutsDir: path.join(VIEWS_DIR, 'layouts'),
    partialsDir: path.join(VIEWS_DIR, 'partials'),
    helpers
}));
app.set('view engine', 'hbs');
app.set('views', VIEWS_DIR);

// --- Middleware ---
app.use(cors());
app.use(morgan('dev'));
app.use(express.json());
app.use(express.static(PUBLIC_DIR));

// --- Routes ---
app.use('/api-docs', swaggerUi.serve, swaggerUi.setup(swaggerSpec));
app.use('/api', require('./routes/api-proxy'));
app.use('/', require('./routes/pages'));

// --- 404 page for anything else ---
app.use((req, res) => {
    res.status(404).render('error', {
        title: 'Pagina non trovata',
        message: `La pagina ${req.originalUrl} non esiste.`
    });
});

// --- Last-resort error handler ---
app.use((err, req, res, next) => {
    console.error('[ERROR]', err.message);
    res.status(500).render('error', {
        title: 'Errore del server',
        message: 'Si è verificato un errore imprevisto. Riprova.'
    });
});

app.listen(PORT, () => {
    console.log(`🚀 Anime Explorer gateway on http://localhost:${PORT}`);
    console.log(`📖 Swagger docs on http://localhost:${PORT}/api-docs`);
});
