/**
 * @fileoverview Swagger (OpenAPI) configuration. The route files are
 * scanned for @swagger JSDoc blocks and the UI is served at /api-docs.
 */

const swaggerJsdoc = require('swagger-jsdoc');
const path = require('path');

const swaggerSpec = swaggerJsdoc({
    definition: {
        openapi: '3.0.0',
        info: {
            title: 'Anime Explorer - MongoDB API',
            version: '1.0.0',
            description: 'REST API for the dynamic anime data subset ' +
                '(ratings, recommendations, favourites, stats, profiles) stored in MongoDB. ' +
                'Consumed by the central Express gateway.'
        },
        servers: [{ url: 'http://localhost:3002' }]
    },
    apis: [path.join(__dirname, 'routes', '*.js')]
});

module.exports = swaggerSpec;
