/**
 * @fileoverview Central configuration of the gateway: ports, backend
 * URLs and frontend paths. Values can be overridden via environment
 * variables to ease deployment on different machines.
 */

const path = require('path');

module.exports = {
    /** HTTP port of the gateway. */
    PORT: process.env.PORT || 3000,

    /** Base URL of the Spring Boot API (static data, PostgreSQL). */
    SPRING_URL: process.env.SPRING_URL || 'http://localhost:8080/api',

    /** Base URL of the Express MongoDB API (dynamic data). */
    MONGO_URL: process.env.MONGO_URL || 'http://localhost:3002/api',

    /** Timeout applied to every proxied request (ms). */
    PROXY_TIMEOUT: 15000,

    /** Frontend directories (views + static assets). */
    FRONTEND_DIR: path.join(__dirname, '..', '..', 'frontend'),
    VIEWS_DIR: path.join(__dirname, '..', '..', 'frontend', 'views'),
    PUBLIC_DIR: path.join(__dirname, '..', '..', 'frontend', 'public')
};
