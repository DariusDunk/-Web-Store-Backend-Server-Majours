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

export async function fetchWithSessionTokens(sessionId, requestFn, options) {

    const {isMe = false, res, req} = options;

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

            if (tokens.is_guest && isMe) {

                const isMeGuestError = new Error("Guest user cannot access this endpoint");

                isMeGuestError.response = {
                    status: 401,
                    data: {
                        guestError: true
                    }
                }
                throw isMeGuestError;
            }
        }

        return await requestFn(tokens);

    })();

    try {
        return await refreshInProgress[sessionId];
    } finally {
        delete refreshInProgress[sessionId];
    }
}