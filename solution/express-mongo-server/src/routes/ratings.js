/**
 * @fileoverview Routes for the ratings collection (~124M documents).
 * Every aggregation touching the whole collection is either indexed
 * or capped, since full scans on this collection are expensive.
 */

const express = require('express');
const router = express.Router();
const { Rating } = require('../models');

/**
 * @swagger
 * /api/ratings/anime/{animeId}:
 *   get:
 *     summary: Rating summary of an anime
 *     description: >
 *       Average, count and per-value distribution of the real ratings
 *       (1-10) of one anime. Unscored list entries (score 0) are excluded.
 *     tags: [Ratings]
 *     parameters:
 *       - in: path
 *         name: animeId
 *         required: true
 *         schema: { type: integer }
 *     responses:
 *       200:
 *         description: Summary with distribution array
 */
router.get('/anime/:animeId', async (req, res, next) => {
    try {
        const animeId = parseInt(req.params.animeId, 10);
        // rating 0 = "in list but unscored" (MAL convention): excluded.
        const distribution = await Rating.aggregate([
            { $match: { anime_id: animeId, rating: { $gte: 1 } } },
            { $group: { _id: '$rating', count: { $sum: 1 } } },
            { $sort: { _id: 1 } }
        ]);

        const totalRatings = distribution.reduce((s, d) => s + d.count, 0);
        const weightedSum = distribution.reduce((s, d) => s + d._id * d.count, 0);

        res.json({
            animeId,
            totalRatings,
            averageRating: totalRatings ? Math.round((weightedSum / totalRatings) * 100) / 100 : 0,
            distribution: distribution.map(d => ({ rating: d._id, count: d.count }))
        });
    } catch (err) {
        next(err);
    }
});

/**
 * @swagger
 * /api/ratings/user/{username}:
 *   get:
 *     summary: Ratings given by a user (paginated)
 *     description: The user is matched by username (or numeric id when the dump has one).
 *     tags: [Ratings]
 *     parameters:
 *       - in: path
 *         name: username
 *         required: true
 *         schema: { type: string }
 *       - in: query
 *         name: page
 *         schema: { type: integer, default: 0 }
 *       - in: query
 *         name: size
 *         schema: { type: integer, default: 20, maximum: 100 }
 */
router.get('/user/:username', async (req, res, next) => {
    try {
        const raw = req.params.username;
        const asNumber = parseInt(raw, 10);
        const query = Number.isNaN(asNumber)
            ? { username: raw }
            : { $or: [{ username: raw }, { user_id: asNumber }] };

        const page = Math.max(parseInt(req.query.page, 10) || 0, 0);
        const size = Math.min(parseInt(req.query.size, 10) || 20, 100);

        const [content, totalElements] = await Promise.all([
            Rating.find(query)
                .sort({ rating: -1 })
                .skip(page * size)
                .limit(size)
                .lean(),
            Rating.countDocuments(query)
        ]);

        res.json({
            content,
            totalElements,
            totalPages: Math.ceil(totalElements / size),
            currentPage: page
        });
    } catch (err) {
        next(err);
    }
});

/**
 * @swagger
 * /api/ratings/distribution:
 *   get:
 *     summary: Global rating distribution
 *     description: Count of ratings per value (1-10) across the whole collection.
 *     tags: [Ratings]
 */
router.get('/distribution', async (req, res, next) => {
    try {
        // rating 0 = unscored list entries: excluded from the chart.
        const distribution = await Rating.aggregate([
            { $match: { rating: { $gte: 1 } } },
            { $group: { _id: '$rating', count: { $sum: 1 } } },
            { $sort: { _id: 1 } }
        ]).option({ allowDiskUse: true });
        res.json(distribution.map(d => ({ rating: d._id, count: d.count })));
    } catch (err) {
        next(err);
    }
});

module.exports = router;
