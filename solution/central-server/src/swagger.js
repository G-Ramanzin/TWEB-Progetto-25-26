/**
 * @fileoverview Swagger configuration of the gateway. The route files
 * are scanned for @swagger JSDoc blocks; UI served at /api-docs.
 */

const swaggerJsdoc = require('swagger-jsdoc');
const path = require('path');
const { PORT } = require('./config');

const swaggerSpec = swaggerJsdoc({
    definition: {
        openapi: '3.0.0',
        info: {
            title: 'Anime Explorer - Central Gateway API',
            version: '1.0.0',
            description: 'Lightweight gateway: serves the Handlebars frontend and ' +
                'forwards API calls to the Spring Boot server (static data) and to the ' +
                'Express MongoDB server (dynamic data). No heavy processing happens here.'
        },
        servers: [{ url: `http://localhost:${PORT}` }]
    },
    apis: [path.join(__dirname, 'routes', '*.js')]
});

module.exports = swaggerSpec;
