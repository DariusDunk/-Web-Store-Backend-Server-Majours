// import axiosBackendClient from '../axiosBackendClient.js';
import {fetchTokensOfSession} from "./requestTokenManager.js";
import sessionCache from "./sessionCache.js";

export async function refreshToken(sessionId, res) {

    if (sessionId && res) {
        console.log("Refreshing tokens...");

        try {
            const responseData = await fetchTokensOfSession(sessionId);
            // console.log('Fetched tokens:', responseData);
            if (!responseData) {
                return Promise.reject("No token response data received from backend.");
            }
            const {
                access_token,
                refresh_token,
                expires_in: access_token_lifetime,
                refresh_expires_in: refresh_token_lifeTime,
                session_expires_in,
                is_guest,
                is_remember_me
            } = responseData;

            res.cookie('session_id', sessionId,
                {
                    maxAge: session_expires_in * 1000,
                    secure: false,
                    path: '/',
                    sameSite: 'lax',
                    httpOnly: true
                });

            // sessionCache.safeDelete(sessionId);

            sessionCache.set(sessionId, {
                access_token,
                access_token_lifetime,
                refresh_token,
                refresh_token_lifeTime,
                is_guest: is_guest,
                remember_me: is_remember_me
            }, session_expires_in);

            return;

        } catch (error) {
            console.error('Error refreshing token: ', error);
            return Promise.reject(error);
        }
    }
}


