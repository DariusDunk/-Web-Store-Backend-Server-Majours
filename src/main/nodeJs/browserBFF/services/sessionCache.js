import NodeCache from "node-cache";

const ONE_HOUR = 60 * 60;

const cache = new NodeCache({
    stdTTL: ONE_HOUR,
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
    //
    // has(sessionId) {
    //     return cache.has(sessionId);
    // }
};