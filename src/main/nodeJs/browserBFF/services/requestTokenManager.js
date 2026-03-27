import sessionCache from "./sessionCache";

const express =require( 'express');
const { Backend_Url } = require('../routes/config.js');

export async function fetchTokensOfSession(sessionID) {
    if (sessionID == null) {
        return null;
    }

    const response = await fetch(`${Backend_Url}/auth/tokens/${sessionID}`);
    if (!response.ok) {
        return response.status;
    }

    return await response.json();
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