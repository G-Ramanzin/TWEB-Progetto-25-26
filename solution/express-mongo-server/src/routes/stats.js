/**
 * @fileoverview Routes for the stats collection and the aggregated
 * platform overview used by the home and statistics pages.
 */

const express = require('express');
const router = express.Router();
const { Stats, Rating, Fav, Profile } = require('../models');

/**
 * @swagger
 * /api/stats/overview:
 *   get:
 *     summary: Platform-wide overview
 *     description: >
 *       Total users, ratings and favourites plus the mean score.
 *       Counts use estimatedDocumentCount (collection metadata) to stay
 *       fast on the 124M-document ratings collection.
 *     tags: [Stats]
 */
router.get('/overview', async (req, res, next) => {
    try {
        const [totalUsers, totalRatings, totalFavorites, meanAgg] = await Promise.all([
            Profile.estimatedDocumentCount(),
            Rating.estimatedDocumentCount(),
            Fav.estimatedDocumentCount(),
            Stats.aggregate([
                { $group: { _id: null, avgScore: { $avg: '$mean_score' } } }
            ])
        ]);

        res.json({
            totalUsers,
            totalRatings,
            totalFavorites,
            averageScore: meanAgg[0] ? Math.round(meanAgg[0].avgScore * 100) / 100 : 0
        });
    } catch (err) {
        next(err);
    }
});

/**
 * @swagger
 * /api/stats/anime/{animeId}:
 *   get:
 *     summary: Watch statistics of an anime
 *     tags: [Stats]
 *     parameters:
 *       - in: path
 *         name: animeId
 *         required: true
 *         schema: { type: integer }
 *     responses:
 *       200: { description: Stats document }
 *       404: { description: No stats for the given anime }
 */
router.get('/anime/:animeId', async (req, res, next) => {
    try {
        const stats = await Stats.findOne({ anime_id: parseInt(req.params.animeId, 10) }).lean();
        if (!stats) return res.status(404).json({ error: 'Stats not found' });
        res.json(stats);
    } catch (err) {
        next(err);
    }
});

module.exports = router;
