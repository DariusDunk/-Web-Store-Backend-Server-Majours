import sessionCache from "./sessionCache";

const {backendClient} = require('../axiosBackendClient');

export async function fetchTokensOfSession(sessionID) {
    if (!sessionID) return null;

    const { data } = await backendClient.get(`/auth/tokens/${sessionID}`);
    return data;
}

export async function withSessionTokens(sessionId, requestFn) {
    let tokens = sessionCache.get(sessionId);

    if (!tokens) {
        tokens = await fetchTokensOfSession(sessionId);
        if (!tokens) throw new Error("Session expired or missing");

        sessionCache.set(sessionId, tokens, tokens.ttl);
    }

    // Now tokens exist, call the request function
    return await requestFn(tokens);
}