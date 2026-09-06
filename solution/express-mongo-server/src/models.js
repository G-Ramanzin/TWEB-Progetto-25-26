/**
 * @fileoverview Mongoose models for the dynamic data subset, aligned
 * with the real CSV headers of the dataset.
 * Collections: ratings, recommendations, favs, stats, profiles.
 *
 * Indexes are declared here but created by the import script AFTER the
 * bulk load (building an index on a full collection is much faster
 * than maintaining it during 124M inserts).
 */

const mongoose = require('mongoose');

/**
 * One entry of a user's anime list (ratings.csv, real headers:
 * username, anime_id, status, score, is_rewatching,
 * num_watched_episodes). The score follows the MyAnimeList
 * convention: 0 means "in the list but not scored", 1-10 is a real
 * rating — analytics endpoints therefore filter rating >= 1.
 * Status and episode counters are not stored: per-anime watch
 * statistics already come pre-aggregated from stats.csv.
 * @typedef {Object} Rating
 * @property {string} [username] - owner of the list entry
 * @property {number} [user_id]  - numeric user id (when present)
 * @property {number} anime_id   - anime the entry refers to
 * @property {number} rating     - 0 (unscored) or 1-10
 */
const ratingSchema = new mongoose.Schema({
    username: { type: String },
    user_id: { type: Number },
    anime_id: { type: Number, required: true },
    rating: { type: Number, required: true }
}, { collection: 'ratings', versionKey: false });

ratingSchema.index({ anime_id: 1 });
ratingSchema.index({ username: 1 });

/**
 * A directed recommendation pair (recommendations.csv:
 * mal_id,recommendation_mal_id). Each pair appears once; there are no
 * vote counts in the dataset, popularity is derived by aggregation.
 * @typedef {Object} Recommendation
 * @property {number} anime_id_from - "if you liked this..."
 * @property {number} anime_id_to   - "...users recommend this"
 */
const recommendationSchema = new mongoose.Schema({
    anime_id_from: { type: Number, required: true },
    anime_id_to: { type: Number, required: true }
}, { collection: 'recommendations', versionKey: false });

recommendationSchema.index({ anime_id_from: 1 });
recommendationSchema.index({ anime_id_to: 1 });

/**
 * A user favourite (favs.csv: username,fav_type,id).
 * fav_type is one of: anime, character, people, company.
 * @typedef {Object} Fav
 * @property {string} username - user
 * @property {string} fav_type - favourite type
 * @property {number} item_id  - id of the favourited item (CSV column "id")
 */
const favSchema = new mongoose.Schema({
    username: { type: String, required: true },
    fav_type: { type: String, required: true },
    item_id: { type: Number, required: true }
}, { collection: 'favs', versionKey: false });

favSchema.index({ fav_type: 1, item_id: 1 });
favSchema.index({ username: 1 });

/**
 * Watch statistics and score distribution of an anime (stats.csv:
 * mal_id, watching, completed, on_hold, dropped, plan_to_watch, total,
 * score_1_votes..score_10_votes with percentages).
 * mean_score and scored_total are computed at import time from the
 * per-score vote columns, since the CSV has no mean column.
 */
const statsSchema = new mongoose.Schema({
    anime_id: { type: Number, required: true },
    watching: Number,
    completed: Number,
    on_hold: Number,
    dropped: Number,
    plan_to_watch: Number,
    total: Number,
    mean_score: Number,
    scored_total: Number
}, { collection: 'stats', versionKey: false, strict: false });

statsSchema.index({ anime_id: 1 });

/**
 * A user profile (profiles.csv: username,gender,birthday,location,
 * joined,watching,completed,on_hold,dropped,plan_to_watch).
 * Profiles are keyed by username: the dataset has no numeric user id.
 */
const profileSchema = new mongoose.Schema({
    username: { type: String, required: true },
    gender: String,
    birthday: String,
    location: String,
    joined: String,
    watching: Number,
    completed: Number,
    on_hold: Number,
    dropped: Number,
    plan_to_watch: Number
}, { collection: 'profiles', versionKey: false, strict: false });

profileSchema.index({ username: 1 });

module.exports = {
    Rating: mongoose.model('Rating', ratingSchema),
    Recommendation: mongoose.model('Recommendation', recommendationSchema),
    Fav: mongoose.model('Fav', favSchema),
    Stats: mongoose.model('Stats', statsSchema),
    Profile: mongoose.model('Profile', profileSchema)
};
