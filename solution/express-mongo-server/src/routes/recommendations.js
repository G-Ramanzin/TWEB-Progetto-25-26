/**
 * @fileoverview Routes for the recommendations collection.
 * The dataset stores directed pairs (mal_id -> recommendation_mal_id)
 * with no vote counts, so "popularity" is derived by aggregation:
 * an anime recommended from many different anime ranks higher.
 */

const express = require('express');
const router = express.Router();
const { Recommendation } = require('../models');

/**
 * @swagger
 * /api/recommendations/anime/{animeId}:
 *   get:
 *     summary: Anime recommended to who liked the given one
 *     description: Outgoing pairs (anime_id_from = animeId) from the dataset.
 *     tags: [Recommendations]
 *     parameters:
 *       - in: path
 *         name: animeId
 *         required: true
 *         schema: { type: integer }
 *       - in: query
 *         name: limit
 *         schema: { type: integer, default: 20, maximum: 50 }
 */
router.get('/anime/:animeId', async (req, res, next) => {
    try {
        const animeId = parseInt(req.params.animeId, 10);
        const limit = Math.min(parseInt(req.query.limit, 10) || 20, 50);
        const recs = await Recommendation.find({ anime_id_from: animeId })
            .limit(limit)
            .lean();
        res.json(recs);
    } catch (err) {
        next(err);
    }
});

/**
 * @swagger
 * /api/recommendations/most-recommended:
 *   get:
 *     summary: Most recommended anime
 *     description: >
 *       Ranks anime by how many other anime recommend them
 *       (aggregation on anime_id_to).
 *     tags: [Recommendations]
 *     parameters:
 *       - in: query
 *         name: limit
 *         schema: { type: integer, default: 20, maximum: 50 }
 */
router.get('/most-recommended', async (req, res, next) => {
    try {
        const limit = Math.min(parseInt(req.query.limit, 10) || 20, 50);
        const ranking = await Recommendation.aggregate([
            { $group: { _id: '$anime_id_to', count: { $sum: 1 } } },
            { $sort: { count: -1 } },
            { $limit: limit },
            { $project: { anime_id: '$_id', recommendedBy: '$count', _id: 0 } }
        ]);
        res.json(ranking);
    } catch (err) {
        next(err);
    }
});

module.exports = router;
