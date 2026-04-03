import sessionCache from "./sessionCache.js";

import { Backend_Url } from '../routes/config.js';
import axios from 'axios';
const refreshInProgress = {};
export async function fetchTokensOfSession(sessionID) {
    if (!sessionID) return null;

    try
    {
        const {data} = await axios.get(`${Backend_Url}/auth/tokens/${sessionID}`);
        return data;
    }
    catch (error)
        {
        console.error("Error fetching tokens of session: ", error);
        throw error;
        }
}

export async function fetchWithSessionTokens(sessionId, requestFn) {

    if (refreshInProgress[sessionId]) {

        // console.log("Refresh call prevented, already in progress for session: " + sessionId + "");

        return refreshInProgress[sessionId];
    }

    refreshInProgress[sessionId] = (async () => {
        // console.log("fetchWithSessionTokens called");
        let tokens = sessionCache.get(sessionId);

        if (!tokens) {
            tokens = await fetchTokensOfSession(sessionId);

            if (!tokens) throw new Error("Session expired or missing");

            sessionCache.set(sessionId, tokens, tokens.ttl);
        }

        return await requestFn(tokens);

    })();

    try {
        return await refreshInProgress[sessionId];
    } finally {
        delete refreshInProgress[sessionId];
    }
}