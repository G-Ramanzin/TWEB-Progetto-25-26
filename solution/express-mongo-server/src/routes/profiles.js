/**
 * @fileoverview Routes for the user profiles collection.
 * Profiles are keyed by username (the dataset has no numeric user id).
 */

const express = require('express');
const router = express.Router();
const { Profile } = require('../models');

/**
 * @swagger
 * /api/profiles/{username}:
 *   get:
 *     summary: User profile by username
 *     tags: [Profiles]
 *     parameters:
 *       - in: path
 *         name: username
 *         required: true
 *         schema: { type: string }
 *     responses:
 *       200: { description: Profile document }
 *       404: { description: Profile not found }
 */
router.get('/:username', async (req, res, next) => {
    try {
        const profile = await Profile.findOne({ username: req.params.username }).lean();
        if (!profile) return res.status(404).json({ error: 'Profile not found' });
        res.json(profile);
    } catch (err) {
        next(err);
    }
});

module.exports = router;
