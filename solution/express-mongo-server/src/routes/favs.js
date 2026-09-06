/**
 * @fileoverview Routes for the favourites collection (~4.2M documents,
 * real headers: username, fav_type, id). fav_type is one of:
 * anime, character, people, company.
 */

const express = require('express');
const router = express.Router();
const { Fav } = require('../models');

/** fav_type values accepted by the endpoints. */
const FAV_TYPES = ['anime', 'character', 'people', 'company'];

/**
 * @swagger
 * /api/favs/top:
 *   get:
 *     summary: Most favourited items
 *     tags: [Favourites]
 *     parameters:
 *       - in: query
 *         name: type
 *         schema: { type: string, default: anime, enum: [anime, character, people, company] }
 *       - in: query
 *         name: limit
 *         schema: { type: integer, default: 10, maximum: 50 }
 */
router.get('/top', async (req, res, next) => {
    try {
        const type = FAV_TYPES.includes(req.query.type) ? req.query.type : 'anime';
        const limit = Math.min(parseInt(req.query.limit, 10) || 10, 50);
        const top = await Fav.aggregate([
            { $match: { fav_type: type } },
            { $group: { _id: '$item_id', count: { $sum: 1 } } },
            { $sort: { count: -1 } },
            { $limit: limit },
            { $project: { item_id: '$_id', favoriteCount: '$count', _id: 0 } }
        ]).option({ allowDiskUse: true });
        res.json({ type, top });
    } catch (err) {
        next(err);
    }
});

/**
 * @swagger
 * /api/favs/anime/{animeId}:
 *   get:
 *     summary: Favourite count of an anime
 *     tags: [Favourites]
 *     parameters:
 *       - in: path
 *         name: animeId
 *         required: true
 *         schema: { type: integer }
 */
router.get('/anime/:animeId', async (req, res, next) => {
    try {
        const animeId = parseInt(req.params.animeId, 10);
        const favoriteCount = await Fav.countDocuments({ fav_type: 'anime', item_id: animeId });
        res.json({ animeId, favoriteCount });
    } catch (err) {
        next(err);
    }
});

/**
 * @swagger
 * /api/favs/user/{username}:
 *   get:
 *     summary: Favourites of a user, grouped by type
 *     tags: [Favourites]
 *     parameters:
 *       - in: path
 *         name: username
 *         required: true
 *         schema: { type: string }
 */
router.get('/user/:username', async (req, res, next) => {
    try {
        const favs = await Fav.find({ username: req.params.username }).limit(500).lean();
        const grouped = {};
        for (const f of favs) {
            (grouped[f.fav_type] = grouped[f.fav_type] || []).push(f.item_id);
        }
        res.json({ username: req.params.username, favourites: grouped });
    } catch (err) {
        next(err);
    }
});

module.exports = router;
