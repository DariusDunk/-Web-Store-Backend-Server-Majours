import NodeCache from "node-cache";

const cache = new NodeCache({
    stdTTL: 8 * 60 * 60,
    checkperiod: 60
});

export default {
    get(sessionId) {
        return cache.get(sessionId);
    },

    set(sessionId, value, ttl) {
        cache.set(sessionId, value, ttl);
    },

    safeDelete(sessionId) {
        if (!cache.has(sessionId)) return;
        cache.del(sessionId);
    },

    ttl(sessionId, newTTL) {
        cache.ttl(sessionId, newTTL);
    },

    print() {
        console.log("cache data: \n" + JSON.stringify(cache.data));
    },

    has(sessionId) {
        return cache.has(sessionId);
    }
};