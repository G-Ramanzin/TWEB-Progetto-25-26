/**
 * @fileoverview MongoDB connection helper shared by the server and the
 * import script. The URI can be overridden with the MONGO_URI
 * environment variable.
 */

const mongoose = require('mongoose');

const MONGO_URI = process.env.MONGO_URI || 'mongodb://localhost:27017/animedb';

/**
 * Opens the shared mongoose connection.
 *
 * @returns {Promise<typeof mongoose>} the connected mongoose instance
 */
async function connect() {
    mongoose.set('strictQuery', true);
    const conn = await mongoose.connect(MONGO_URI);
    console.log(`✅ Connected to MongoDB at ${MONGO_URI}`);
    return conn;
}

module.exports = { connect, MONGO_URI };
