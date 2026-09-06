/**
 * @fileoverview Express server for the dynamic anime data subset
 * (MongoDB): ratings, recommendations, favourites, stats, profiles.
 * Listens on port 3002 and is consumed by the central gateway.
 */

const express = require('express');
const cors = require('cors');
const morgan = require('morgan');
const swaggerUi = require('swagger-ui-express');
const { connect } = require('./db');
const swaggerSpec = require('./swagger');

const app = express();
const PORT = process.env.PORT || 3002;

// --- Middleware ---
app.use(cors());
app.use(morgan('dev'));
app.use(express.json());

// --- API routes, one module per collection ---
app.use('/api/ratings', require('./routes/ratings'));
app.use('/api/recommendations', require('./routes/recommendations'));
app.use('/api/favs', require('./routes/favs'));
app.use('/api/stats', require('./routes/stats'));
app.use('/api/profiles', require('./routes/profiles'));

// --- Swagger UI ---
app.use('/api-docs', swaggerUi.serve, swaggerUi.setup(swaggerSpec));

// --- 404 for unknown API paths ---
app.use((req, res) => res.status(404).json({ error: 'Not found' }));

// --- Centralised error handler (routes call next(err)) ---
app.use((err, req, res, next) => {
    console.error('[ERROR]', err.message);
    res.status(500).json({ error: 'Internal server error' });
});

/**
 * Connects to MongoDB and starts the HTTP listener.
 * Exits the process when the database is unreachable, so a
 * misconfiguration is visible immediately.
 */
async function start() {
    try {
        await connect();
        app.listen(PORT, () => {
            console.log(`🍃 Express MongoDB server on http://localhost:${PORT}`);
            console.log(`📖 Swagger docs on http://localhost:${PORT}/api-docs`);
        });
    } catch (err) {
        console.error('Cannot connect to MongoDB:', err.message);
        process.exit(1);
    }
}

start();
