const NodeCache = require("node-cache");

const cache = new NodeCache({
    stdTTL: 8 * 60 * 60,
    checkperiod: 60
});

module.exports = {
    get(sessionId) {
        return cache.get(sessionId);
    },

    set(sessionId, value, ttl) {
        cache.set(sessionId, value, ttl);
    },

    delete(sessionId) {
        cache.del(sessionId);
    },

    ttl(sessionId, newTTL) {
        cache.ttl(sessionId, newTTL);
    },

    has(sessionId) {
        return cache.has(sessionId);
    }
};