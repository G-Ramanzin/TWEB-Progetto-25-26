/**
 * @fileoverview API proxy routes of the gateway.
 *
 * Every route simply forwards the request (path + query string) to the
 * proper backend with axios and streams the JSON back. The gateway
 * performs no data processing, in compliance with the requirement that
 * the central server "must not perform any heavy duties".
 */

const express = require('express');
const axios = require('axios');
const router = express.Router();
const { SPRING_URL, MONGO_URL, PROXY_TIMEOUT } = require('../config');

/**
 * Forwards a GET request to a backend endpoint and relays the answer.
 * Backend errors are propagated with their original status code.
 *
 * @param {string} url - full backend URL
 * @param {import('express').Request} req - incoming request (query is forwarded)
 * @param {import('express').Response} res - response to write to
 * @returns {Promise<void>}
 */
async function forward(url, req, res) {
    try {
        const response = await axios.get(url, { params: req.query, timeout: PROXY_TIMEOUT });
        res.json(response.data);
    } catch (err) {
        // A timeout (ECONNABORTED) is not the same failure as a dead
        // backend: report 504 vs 502 so the cause is visible.
        let status;
        let body;
        if (err.response) {
            status = err.response.status;
            body = err.response.data;
        } else if (err.code === 'ECONNABORTED') {
            status = 504;
            body = { error: 'Backend timeout' };
        } else {
            status = 502;
            body = { error: 'Backend unreachable' };
        }
        console.error(`[PROXY] ${url} -> ${status} (${err.code || 'HTTP'})`);
        res.status(status).json(body);
    }
}

// ==================== Spring Boot (static data) ====================

/**
 * @swagger
 * /api/anime/search:
 *   get:
 *     summary: Search anime (proxied to Spring Boot)
 *     tags: [Anime]
 *     parameters:
 *       - { in: query, name: q, schema: { type: string } }
 *       - { in: query, name: genre, schema: { type: string } }
 *       - { in: query, name: type, schema: { type: string } }
 *       - { in: query, name: status, schema: { type: string } }
 *       - { in: query, name: page, schema: { type: integer } }
 *       - { in: query, name: size, schema: { type: integer } }
 */
router.get('/anime/search', (req, res) => forward(`${SPRING_URL}/anime/search`, req, res));

/**
 * @swagger
 * /api/anime/top:
 *   get:
 *     summary: Top rated anime (proxied to Spring Boot)
 *     tags: [Anime]
 */
router.get('/anime/top', (req, res) => forward(`${SPRING_URL}/anime/top`, req, res));

/**
 * @swagger
 * /api/anime/batch:
 *   get:
 *     summary: Batch anime summaries by ids (proxied to Spring Boot)
 *     tags: [Anime]
 */
router.get('/anime/batch', (req, res) => forward(`${SPRING_URL}/anime/batch`, req, res));

/**
 * @swagger
 * /api/anime/genres:
 *   get:
 *     summary: All genres (proxied to Spring Boot)
 *     tags: [Anime]
 */
router.get('/anime/genres', (req, res) => forward(`${SPRING_URL}/anime/genres`, req, res));

/**
 * @swagger
 * /api/anime/types:
 *   get:
 *     summary: All formats (proxied to Spring Boot)
 *     tags: [Anime]
 */
router.get('/anime/types', (req, res) => forward(`${SPRING_URL}/anime/types`, req, res));

/**
 * @swagger
 * /api/anime/genre-stats:
 *   get:
 *     summary: Anime per genre (proxied to Spring Boot)
 *     tags: [Anime]
 */
router.get('/anime/genre-stats', (req, res) => forward(`${SPRING_URL}/anime/genre-stats`, req, res));

/**
 * @swagger
 * /api/anime/type-stats:
 *   get:
 *     summary: Anime per format (proxied to Spring Boot)
 *     tags: [Anime]
 */
router.get('/anime/type-stats', (req, res) => forward(`${SPRING_URL}/anime/type-stats`, req, res));

/**
 * @swagger
 * /api/anime/{id}:
 *   get:
 *     summary: Anime detail (proxied to Spring Boot)
 *     tags: [Anime]
 *     parameters:
 *       - { in: path, name: id, required: true, schema: { type: integer } }
 */
router.get('/anime/:id', (req, res) => forward(`${SPRING_URL}/anime/${req.params.id}`, req, res));

/**
 * @swagger
 * /api/anime/{id}/characters:
 *   get:
 *     summary: Characters of an anime with names (proxied to Spring Boot)
 *     tags: [Anime]
 */
router.get('/anime/:id/characters', (req, res) => forward(`${SPRING_URL}/anime/${req.params.id}/characters`, req, res));

/**
 * @swagger
 * /api/characters/search:
 *   get:
 *     summary: Search characters (proxied to Spring Boot)
 *     tags: [Characters]
 */
router.get('/characters/search', (req, res) => forward(`${SPRING_URL}/characters/search`, req, res));

/**
 * @swagger
 * /api/characters/{id}:
 *   get:
 *     summary: Character detail (proxied to Spring Boot)
 *     tags: [Characters]
 */
router.get('/characters/:id', (req, res) => forward(`${SPRING_URL}/characters/${req.params.id}`, req, res));

/**
 * @swagger
 * /api/characters/{id}/nicknames:
 *   get:
 *     summary: Character nicknames (proxied to Spring Boot)
 *     tags: [Characters]
 */
router.get('/characters/:id/nicknames', (req, res) => forward(`${SPRING_URL}/characters/${req.params.id}/nicknames`, req, res));

/**
 * @swagger
 * /api/characters/{id}/appearances:
 *   get:
 *     summary: Anime appearances of a character with titles (proxied to Spring Boot)
 *     tags: [Characters]
 */
router.get('/characters/:id/appearances', (req, res) => forward(`${SPRING_URL}/characters/${req.params.id}/appearances`, req, res));

/**
 * @swagger
 * /api/people/search:
 *   get:
 *     summary: Search people (proxied to Spring Boot)
 *     tags: [People]
 */
router.get('/people/search', (req, res) => forward(`${SPRING_URL}/people/search`, req, res));

/**
 * @swagger
 * /api/people/{id}:
 *   get:
 *     summary: Person detail (proxied to Spring Boot)
 *     tags: [People]
 */
router.get('/people/:id', (req, res) => forward(`${SPRING_URL}/people/${req.params.id}`, req, res));

/**
 * @swagger
 * /api/people/{id}/alternate-names:
 *   get:
 *     summary: Alternate names of a person (proxied to Spring Boot)
 *     tags: [People]
 */
router.get('/people/:id/alternate-names', (req, res) => forward(`${SPRING_URL}/people/${req.params.id}/alternate-names`, req, res));

/**
 * @swagger
 * /api/people/{id}/anime-works:
 *   get:
 *     summary: Production works of a person with titles (proxied to Spring Boot)
 *     tags: [People]
 */
router.get('/people/:id/anime-works', (req, res) => forward(`${SPRING_URL}/people/${req.params.id}/anime-works`, req, res));

/**
 * @swagger
 * /api/people/{id}/voice-works:
 *   get:
 *     summary: Voice roles of a person with titles and character names (proxied to Spring Boot)
 *     tags: [People]
 */
router.get('/people/:id/voice-works', (req, res) => forward(`${SPRING_URL}/people/${req.params.id}/voice-works`, req, res));

// ==================== Express MongoDB (dynamic data) ====================

/**
 * @swagger
 * /api/ratings/anime/{id}:
 *   get:
 *     summary: Rating summary of an anime (proxied to MongoDB server)
 *     tags: [Ratings]
 */
router.get('/ratings/anime/:id', (req, res) => forward(`${MONGO_URL}/ratings/anime/${req.params.id}`, req, res));

/**
 * @swagger
 * /api/ratings/user/{id}:
 *   get:
 *     summary: Ratings of a user (proxied to MongoDB server)
 *     tags: [Ratings]
 */
router.get('/ratings/user/:id', (req, res) => forward(`${MONGO_URL}/ratings/user/${req.params.id}`, req, res));

/**
 * @swagger
 * /api/ratings/distribution:
 *   get:
 *     summary: Global rating distribution (proxied to MongoDB server)
 *     tags: [Ratings]
 */
router.get('/ratings/distribution', (req, res) => forward(`${MONGO_URL}/ratings/distribution`, req, res));

/**
 * @swagger
 * /api/recommendations/anime/{id}:
 *   get:
 *     summary: Recommendations of an anime (proxied to MongoDB server)
 *     tags: [Recommendations]
 */
router.get('/recommendations/anime/:id', (req, res) => forward(`${MONGO_URL}/recommendations/anime/${req.params.id}`, req, res));

/**
 * @swagger
 * /api/recommendations/most-recommended:
 *   get:
 *     summary: Most recommended anime ranking (proxied to MongoDB server)
 *     tags: [Recommendations]
 */
router.get('/recommendations/most-recommended', (req, res) => forward(`${MONGO_URL}/recommendations/most-recommended`, req, res));

/**
 * @swagger
 * /api/favs/top:
 *   get:
 *     summary: Most favourited anime (proxied to MongoDB server)
 *     tags: [Favourites]
 */
router.get('/favs/top', (req, res) => forward(`${MONGO_URL}/favs/top`, req, res));

/**
 * @swagger
 * /api/favs/anime/{id}:
 *   get:
 *     summary: Favourite count of an anime (proxied to MongoDB server)
 *     tags: [Favourites]
 */
router.get('/favs/anime/:id', (req, res) => forward(`${MONGO_URL}/favs/anime/${req.params.id}`, req, res));

/**
 * @swagger
 * /api/stats/overview:
 *   get:
 *     summary: Platform overview (proxied to MongoDB server)
 *     tags: [Stats]
 */
router.get('/stats/overview', (req, res) => forward(`${MONGO_URL}/stats/overview`, req, res));

/**
 * @swagger
 * /api/stats/anime/{id}:
 *   get:
 *     summary: Watch statistics of an anime (proxied to MongoDB server)
 *     tags: [Stats]
 */
router.get('/stats/anime/:id', (req, res) => forward(`${MONGO_URL}/stats/anime/${req.params.id}`, req, res));

/**
 * @swagger
 * /api/favs/user/{username}:
 *   get:
 *     summary: Favourites of a user grouped by type (proxied to MongoDB server)
 *     tags: [Favourites]
 */
router.get('/favs/user/:username', (req, res) => forward(`${MONGO_URL}/favs/user/${req.params.username}`, req, res));

/**
 * @swagger
 * /api/profiles/{username}:
 *   get:
 *     summary: User profile by username (proxied to MongoDB server)
 *     tags: [Profiles]
 */
router.get('/profiles/:username', (req, res) => forward(`${MONGO_URL}/profiles/${req.params.username}`, req, res));

module.exports = router;
