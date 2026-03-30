import sessionCache from "./sessionCache.js";

import { Backend_Url } from '../routes/config.js';
import axios from 'axios';

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
    let tokens = sessionCache.get(sessionId);

    if (!tokens) {
        tokens = await fetchTokensOfSession(sessionId);
        if (!tokens) throw new Error("Session expired or missing");

        sessionCache.set(sessionId, tokens, tokens.ttl);
    }

    // Now tokens exist, call the request function
    return await requestFn(tokens);
}