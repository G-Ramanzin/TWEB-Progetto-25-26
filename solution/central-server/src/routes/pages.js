/**
 * @fileoverview Frontend page routes: each route gathers the data it
 * needs from the two backends (in parallel where possible) and renders
 * the corresponding Handlebars view.
 *
 * The gateway only forwards requests and merges the JSON answers into
 * the view model: no computation on the data itself is performed here,
 * in compliance with the "no heavy duties" requirement.
 */

const express = require('express');
const axios = require('axios');
const router = express.Router();
const { SPRING_URL, MONGO_URL, PROXY_TIMEOUT } = require('../config');

/**
 * GET helper returning the response data, or a fallback value when the
 * backend call fails: pages degrade gracefully instead of breaking.
 *
 * @param {string} url - full backend URL
 * @param {Object} [params] - query parameters
 * @param {*} [fallback=null] - value returned on error
 * @returns {Promise<*>} response data or fallback
 */
async function fetchData(url, params = {}, fallback = null) {
    try {
        const response = await axios.get(url, { params, timeout: PROXY_TIMEOUT });
        return response.data;
    } catch (err) {
        console.error(`[PAGES] ${url} failed: ${err.message}`);
        return fallback;
    }
}

/**
 * @swagger
 * /:
 *   get:
 *     summary: Home page
 *     description: Top rated anime, most favourited anime and platform overview.
 *     tags: [Pages]
 */
router.get('/', async (req, res) => {
    const [topAnime, overview, favsData] = await Promise.all([
        fetchData(`${SPRING_URL}/anime/top`, { limit: 12 }, []),
        fetchData(`${MONGO_URL}/stats/overview`, {}, null),
        fetchData(`${MONGO_URL}/favs/top`, { type: 'anime', limit: 5 }, null)
    ]);

    // Enrich favourite ids with titles through the batch endpoint (light merge).
    const topFavs = favsData ? favsData.top : [];
    let favsEnriched = [];
    if (topFavs.length) {
        const ids = topFavs.map(f => f.item_id).join(',');
        const summaries = await fetchData(`${SPRING_URL}/anime/batch`, { ids }, []);
        const byId = Object.fromEntries(summaries.map(s => [s.animeId, s]));
        favsEnriched = topFavs.map(f => ({ ...f, anime: byId[f.item_id] || null }));
    }

    res.render('home', {
        title: 'Anime Explorer',
        topAnime,
        overview,
        topFavs: favsEnriched
    });
});

/**
 * @swagger
 * /search:
 *   get:
 *     summary: Anime search page
 *     description: Filterable, paginated anime search backed by PostgreSQL.
 *     tags: [Pages]
 */
router.get('/search', async (req, res) => {
    const { q = '', genre = '', type = '', status = '' } = req.query;
    const page = Math.max(parseInt(req.query.page, 10) || 0, 0);

    const [results, genres, types] = await Promise.all([
        fetchData(`${SPRING_URL}/anime/search`, { q, genre, type, status, page, size: 24 },
            { content: [], totalPages: 0, totalElements: 0 }),
        fetchData(`${SPRING_URL}/anime/genres`, {}, []),
        fetchData(`${SPRING_URL}/anime/types`, {}, [])
    ]);

    res.render('search', {
        title: 'Cerca Anime',
        results: results.content,
        totalElements: results.totalElements,
        totalPages: results.totalPages,
        page,
        hasPrev: page > 0,
        hasNext: page < results.totalPages - 1,
        query: { q, genre, type, status },
        genres,
        types
    });
});

/**
 * @swagger
 * /anime/{id}:
 *   get:
 *     summary: Anime detail page
 *     description: >
 *       Static detail (PostgreSQL) merged with ratings, watch stats,
 *       favourites and recommendations (MongoDB). Recommendation ids
 *       are enriched with titles via the batch endpoint.
 *     tags: [Pages]
 */
router.get('/anime/:id', async (req, res) => {
    const id = req.params.id;

    const anime = await fetchData(`${SPRING_URL}/anime/${id}`);
    if (!anime) {
        return res.status(404).render('error', {
            title: 'Non trovato', message: `Anime #${id} non trovato.`
        });
    }

    const [characters, ratings, stats, favs, recommendations] = await Promise.all([
        fetchData(`${SPRING_URL}/anime/${id}/characters`, {}, []),
        fetchData(`${MONGO_URL}/ratings/anime/${id}`, {}, null),
        fetchData(`${MONGO_URL}/stats/anime/${id}`, {}, null),
        fetchData(`${MONGO_URL}/favs/anime/${id}`, {}, null),
        fetchData(`${MONGO_URL}/recommendations/anime/${id}`, { limit: 12 }, [])
    ]);

    // Enrich recommendation ids with titles (light merge of two API answers).
    let recsEnriched = [];
    if (recommendations.length) {
        const numericId = parseInt(id, 10);
        const otherIds = [...new Set(recommendations.map(r =>
            r.anime_id_from === numericId ? r.anime_id_to : r.anime_id_from))];
        const summaries = await fetchData(`${SPRING_URL}/anime/batch`,
            { ids: otherIds.join(',') }, []);
        const byId = Object.fromEntries(summaries.map(s => [s.animeId, s]));
        recsEnriched = recommendations.map(r => {
            const otherId = r.anime_id_from === numericId ? r.anime_id_to : r.anime_id_from;
            return { ...r, other: byId[otherId] || { animeId: otherId, title: `Anime #${otherId}` } };
        });
    }

    res.render('anime-detail', {
        title: anime.title,
        anime,
        characters: characters.slice(0, 12),
        ratings,
        stats,
        favs,
        recommendations: recsEnriched
    });
});

/**
 * @swagger
 * /characters:
 *   get:
 *     summary: Character browsing page
 *     tags: [Pages]
 */
router.get('/characters', async (req, res) => {
    const q = req.query.q || '';
    const page = Math.max(parseInt(req.query.page, 10) || 0, 0);

    const results = await fetchData(`${SPRING_URL}/characters/search`, { q, page, size: 24 },
        { content: [], totalPages: 0, totalElements: 0 });

    res.render('characters', {
        title: 'Personaggi',
        characters: results.content,
        totalElements: results.totalElements,
        totalPages: results.totalPages,
        page,
        hasPrev: page > 0,
        hasNext: page < results.totalPages - 1,
        q
    });
});

/**
 * @swagger
 * /character/{id}:
 *   get:
 *     summary: Character detail page
 *     description: Character identity, nicknames and anime appearances with titles.
 *     tags: [Pages]
 */
router.get('/character/:id', async (req, res) => {
    const id = req.params.id;

    const character = await fetchData(`${SPRING_URL}/characters/${id}`);
    if (!character) {
        return res.status(404).render('error', {
            title: 'Non trovato', message: `Personaggio #${id} non trovato.`
        });
    }

    const [nicknames, appearances] = await Promise.all([
        fetchData(`${SPRING_URL}/characters/${id}/nicknames`, {}, []),
        fetchData(`${SPRING_URL}/characters/${id}/appearances`, {}, [])
    ]);

    res.render('character-detail', {
        title: character.name,
        character,
        nicknames,
        appearances
    });
});

/**
 * @swagger
 * /people:
 *   get:
 *     summary: People browsing page
 *     tags: [Pages]
 */
router.get('/people', async (req, res) => {
    const q = req.query.q || '';
    const page = Math.max(parseInt(req.query.page, 10) || 0, 0);

    const results = await fetchData(`${SPRING_URL}/people/search`, { q, page, size: 24 },
        { content: [], totalPages: 0, totalElements: 0 });

    res.render('people', {
        title: 'Persone',
        people: results.content,
        totalElements: results.totalElements,
        totalPages: results.totalPages,
        page,
        hasPrev: page > 0,
        hasNext: page < results.totalPages - 1,
        q
    });
});

/**
 * @swagger
 * /person/{id}:
 *   get:
 *     summary: Person detail page
 *     description: Person identity, alternate names, voice roles and production works.
 *     tags: [Pages]
 */
router.get('/person/:id', async (req, res) => {
    const id = req.params.id;

    const person = await fetchData(`${SPRING_URL}/people/${id}`);
    if (!person) {
        return res.status(404).render('error', {
            title: 'Non trovato', message: `Persona #${id} non trovata.`
        });
    }

    const [alternateNames, voiceWorks, animeWorks] = await Promise.all([
        fetchData(`${SPRING_URL}/people/${id}/alternate-names`, {}, []),
        fetchData(`${SPRING_URL}/people/${id}/voice-works`, {}, []),
        fetchData(`${SPRING_URL}/people/${id}/anime-works`, {}, [])
    ]);

    res.render('person-detail', {
        title: person.name,
        person,
        alternateNames,
        voiceWorks: voiceWorks.slice(0, 30),
        animeWorks: animeWorks.slice(0, 30)
    });
});

/**
 * @swagger
 * /statistics:
 *   get:
 *     summary: Statistics dashboard page
 *     description: Genre/format distributions (PostgreSQL) and rating distribution (MongoDB) rendered with Chart.js.
 *     tags: [Pages]
 */
router.get('/statistics', async (req, res) => {
    const [overview, genreStats, typeStats, ratingDistribution, topAnime] = await Promise.all([
        fetchData(`${MONGO_URL}/stats/overview`, {}, null),
        fetchData(`${SPRING_URL}/anime/genre-stats`, {}, []),
        fetchData(`${SPRING_URL}/anime/type-stats`, {}, []),
        fetchData(`${MONGO_URL}/ratings/distribution`, {}, []),
        fetchData(`${SPRING_URL}/anime/top`, { limit: 10 }, [])
    ]);

    res.render('statistics', {
        title: 'Statistiche',
        overview,
        genreStats: genreStats.slice(0, 15),
        typeStats,
        ratingDistribution,
        topAnime
    });
});

/**
 * @swagger
 * /recommendations:
 *   get:
 *     summary: Recommendations page
 *     description: Strongest recommendation pairs, or the pairs of a specific anime, with titles from the batch endpoint.
 *     tags: [Pages]
 */
router.get('/recommendations', async (req, res) => {
    const animeId = req.query.animeId ? parseInt(req.query.animeId, 10) : null;

    // With a filter: the anime recommended to who liked the given one.
    if (animeId) {
        const [sourceList, pairs] = await Promise.all([
            fetchData(`${SPRING_URL}/anime/batch`, { ids: animeId }, []),
            fetchData(`${MONGO_URL}/recommendations/anime/${animeId}`, { limit: 30 }, [])
        ]);

        const ids = [...new Set(pairs.map(r => r.anime_id_to))];
        const summaries = ids.length
            ? await fetchData(`${SPRING_URL}/anime/batch`, { ids: ids.join(',') }, [])
            : [];
        const byId = Object.fromEntries(summaries.map(s => [s.animeId, s]));

        return res.render('recommendations', {
            title: 'Consigli',
            animeId,
            sourceAnime: sourceList.length ? sourceList[0] : null,
            recommended: ids.map(i => byId[i] || { animeId: i, title: `Anime #${i}` })
        });
    }

    // Without filter: "most recommended anime" ranking from MongoDB,
    // enriched with titles via the batch endpoint (light merge).
    const ranking = await fetchData(`${MONGO_URL}/recommendations/most-recommended`, { limit: 30 }, []);
    const ids = ranking.map(r => r.anime_id);
    const summaries = ids.length
        ? await fetchData(`${SPRING_URL}/anime/batch`, { ids: ids.join(',') }, [])
        : [];
    const byId = Object.fromEntries(summaries.map(s => [s.animeId, s]));

    res.render('recommendations', {
        title: 'Consigli',
        animeId: null,
        ranking: ranking.map(r => ({
            anime: byId[r.anime_id] || { animeId: r.anime_id, title: `Anime #${r.anime_id}` },
            recommendedBy: r.recommendedBy
        }))
    });
});

module.exports = router;
