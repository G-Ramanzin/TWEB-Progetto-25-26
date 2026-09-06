/**
 * @fileoverview Streaming CSV import into MongoDB, aligned with the
 * real dataset headers.
 *
 * Usage:  node scripts/import-data.js [--only ratings,favs]
 *
 * Design notes for the huge ratings.csv (~124M rows):
 *  - the file is read line by line (readline) so memory stays flat;
 *  - documents are inserted with insertMany({ordered:false}) in
 *    batches of BATCH_SIZE, which lets MongoDB parallelise writes and
 *    keep going on single bad documents;
 *  - collections are dropped first so the import is idempotent;
 *  - indexes are created AFTER the bulk load: building an index once
 *    at the end is much faster than maintaining it on every insert.
 *
 * stats.csv has no mean score column, only per-value vote counts
 * (score_1_votes .. score_10_votes): mean_score and scored_total are
 * computed here at import time so the API can serve them directly.
 */

const fs = require('fs');
const path = require('path');
const readline = require('readline');
const mongoose = require('mongoose');
const { connect } = require('../src/db');
const { Rating, Recommendation, Fav, Stats, Profile } = require('../src/models');

const DATA_DIR = process.env.DATA_DIR || path.join(__dirname, '..', '..', '..', 'data');
const BATCH_SIZE = 20000;

/**
 * Minimal CSV line splitter with quote support.
 * Handles quoted fields containing commas and doubled quotes.
 *
 * @param {string} line - raw CSV line
 * @returns {string[]} field values
 */
function splitCsvLine(line) {
    const out = [];
    let cur = '';
    let inQuotes = false;
    for (let i = 0; i < line.length; i++) {
        const ch = line[i];
        if (inQuotes) {
            if (ch === '"') {
                if (line[i + 1] === '"') { cur += '"'; i++; }
                else inQuotes = false;
            } else cur += ch;
        } else if (ch === '"') {
            inQuotes = true;
        } else if (ch === ',') {
            out.push(cur); cur = '';
        } else cur += ch;
    }
    out.push(cur);
    return out;
}

/**
 * Builds a header-name -> index map with normalised names.
 *
 * @param {string[]} headerCells - first CSV row
 * @returns {Object<string, number>} lookup map
 */
function headerMap(headerCells) {
    const map = {};
    headerCells.forEach((h, i) => {
        map[h.trim().toLowerCase().replace(/\s+/g, '_')] = i;
    });
    return map;
}

/**
 * Returns the first non-empty cell among the candidate columns.
 *
 * @param {string[]} cells - CSV row cells
 * @param {Object<string, number>} map - header map
 * @param {...string} keys - candidate column names
 * @returns {string|null} value or null when missing
 */
function val(cells, map, ...keys) {
    for (const k of keys) {
        const idx = map[k];
        if (idx !== undefined && idx < cells.length) {
            const v = (cells[idx] || '').trim();
            if (v && v.toLowerCase() !== 'unknown') return v;
        }
    }
    return null;
}

/** @param {string|null} s @returns {number|null} */
function num(s) {
    if (s === null || s === '') return null;
    const n = Number(s);
    return Number.isFinite(n) ? n : null;
}

/**
 * Generic streaming import of one CSV file into one collection.
 *
 * @param {string} fileName - CSV file inside the data directory
 * @param {import('mongoose').Model} Model - destination model
 * @param {(cells: string[], map: Object<string, number>) => (Object|null)} mapper
 *        row mapper returning the document or null to skip the row
 * @returns {Promise<void>}
 */
async function importFile(fileName, Model, mapper) {
    const filePath = path.join(DATA_DIR, fileName);
    if (!fs.existsSync(filePath)) {
        console.warn(`[${fileName}] not found, skipped`);
        return;
    }

    console.log(`[${fileName}] importing into '${Model.collection.name}'...`);
    await Model.collection.drop().catch(() => { /* collection may not exist */ });

    const rl = readline.createInterface({
        input: fs.createReadStream(filePath, { encoding: 'utf8' }),
        crlfDelay: Infinity
    });

    let map = null;
    let batch = [];
    let imported = 0;
    let skipped = 0;
    const t0 = Date.now();

    for await (const line of rl) {
        if (!line) continue;
        const cells = splitCsvLine(line);
        if (!map) { map = headerMap(cells); continue; }

        const doc = mapper(cells, map);
        if (!doc) { skipped++; continue; }

        batch.push(doc);
        if (batch.length >= BATCH_SIZE) {
            await Model.insertMany(batch, { ordered: false, lean: true }).catch(e => {
                console.warn(`  batch warning: ${e.message.substring(0, 120)}`);
            });
            imported += batch.length;
            batch = [];
            if (imported % 1000000 === 0) {
                const secs = Math.round((Date.now() - t0) / 1000);
                console.log(`  ...${(imported / 1000000).toFixed(0)}M rows in ${secs}s`);
            }
        }
    }
    if (batch.length) {
        await Model.insertMany(batch, { ordered: false, lean: true }).catch(() => {});
        imported += batch.length;
    }

    console.log(`[${fileName}] done: ~${imported} imported, ${skipped} skipped ` +
        `(${Math.round((Date.now() - t0) / 1000)}s)`);
}

/** Creates all declared indexes after the bulk load. */
async function buildIndexes() {
    console.log('Building indexes (this can take a while on ratings)...');
    for (const Model of [Rating, Recommendation, Fav, Stats, Profile]) {
        const t0 = Date.now();
        await Model.createIndexes();
        console.log(`  ${Model.collection.name}: indexes ready in ${Math.round((Date.now() - t0) / 1000)}s`);
    }
}

/** Entry point: connects, imports the selected files, builds indexes. */
async function main() {
    await connect();

    const onlyArg = process.argv.find(a => a.startsWith('--only'));
    const only = onlyArg ? (onlyArg.split('=')[1] || process.argv[process.argv.indexOf(onlyArg) + 1] || '')
        .split(',').map(s => s.trim()).filter(Boolean) : null;
    const wanted = name => !only || only.includes(name);

    // ratings.csv: exact headers unknown at design time (4GB file);
    // both username and numeric user id based dumps are supported.
    if (wanted('ratings')) {
        await importFile('ratings.csv', Rating, (c, m) => {
            const anime_id = num(val(c, m, 'anime_id', 'anime_mal_id', 'mal_id', 'animeid'));
            const rating = num(val(c, m, 'rating', 'score', 'my_score'));
            if (anime_id === null || rating === null) return null;
            const doc = { anime_id, rating };
            const username = val(c, m, 'username', 'user', 'profile');
            const user_id = num(val(c, m, 'user_id', 'userid'));
            if (username) doc.username = username;
            if (user_id !== null) doc.user_id = user_id;
            if (!doc.username && doc.user_id === undefined) return null;
            return doc;
        });
    }

    // recommendations.csv: mal_id,recommendation_mal_id
    if (wanted('recommendations')) {
        await importFile('recommendations.csv', Recommendation, (c, m) => {
            const from = num(val(c, m, 'mal_id', 'anime_id_from', 'anime_id'));
            const to = num(val(c, m, 'recommendation_mal_id', 'anime_id_to'));
            if (from === null || to === null) return null;
            return { anime_id_from: from, anime_id_to: to };
        });
    }

    // favs.csv: username,fav_type,id
    if (wanted('favs')) {
        await importFile('favs.csv', Fav, (c, m) => {
            const username = val(c, m, 'username', 'user');
            const fav_type = val(c, m, 'fav_type', 'type');
            const item_id = num(val(c, m, 'id', 'item_id', 'anime_id'));
            if (!username || !fav_type || item_id === null) return null;
            return { username, fav_type, item_id };
        });
    }

    // stats.csv: mal_id,watching,completed,on_hold,dropped,plan_to_watch,
    // total,score_1_votes,score_1_percentage,...,score_10_votes,...
    if (wanted('stats')) {
        await importFile('stats.csv', Stats, (c, m) => {
            const anime_id = num(val(c, m, 'mal_id', 'anime_id', 'id'));
            if (anime_id === null) return null;

            // Compute the mean score from the per-value vote counts.
            let votesSum = 0;
            let weighted = 0;
            const scoreVotes = {};
            for (let s = 1; s <= 10; s++) {
                const v = num(val(c, m, `score_${s}_votes`)) || 0;
                scoreVotes[`score_${s}_votes`] = v;
                votesSum += v;
                weighted += s * v;
            }

            return {
                anime_id,
                watching: num(val(c, m, 'watching')),
                completed: num(val(c, m, 'completed')),
                on_hold: num(val(c, m, 'on_hold')),
                dropped: num(val(c, m, 'dropped')),
                plan_to_watch: num(val(c, m, 'plan_to_watch')),
                total: num(val(c, m, 'total')),
                scored_total: votesSum,
                mean_score: votesSum ? Math.round((weighted / votesSum) * 100) / 100 : null,
                ...scoreVotes
            };
        });
    }

    // profiles.csv: username,gender,birthday,location,joined,watching,
    // completed,on_hold,dropped,plan_to_watch
    if (wanted('profiles')) {
        await importFile('profiles.csv', Profile, (c, m) => {
            const username = val(c, m, 'username', 'profile', 'user');
            if (!username) return null;
            return {
                username,
                gender: val(c, m, 'gender'),
                birthday: val(c, m, 'birthday', 'birth_date'),
                location: val(c, m, 'location'),
                joined: val(c, m, 'joined'),
                watching: num(val(c, m, 'watching')),
                completed: num(val(c, m, 'completed')),
                on_hold: num(val(c, m, 'on_hold')),
                dropped: num(val(c, m, 'dropped')),
                plan_to_watch: num(val(c, m, 'plan_to_watch'))
            };
        });
    }

    await buildIndexes();
    await mongoose.disconnect();
    console.log('✅ MongoDB import completed.');
}

main().catch(err => {
    console.error('Import failed:', err);
    process.exit(1);
});
