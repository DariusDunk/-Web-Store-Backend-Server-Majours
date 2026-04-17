import sessionCache from "./sessionCache.js";
import {Backend_Url} from '../routes/config.js';
import axios from 'axios';

const refreshInProgress = new Map();
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

async function createGuestSession() {
    try {
        const {data} = await axios.get(`${Backend_Url}/auth/session/guest/create/Web`);

        // console.log("Guest session data: ", data);

        return data;
    } catch (error) {
        console.error("Error creating guest session: ", error);
    }
}

async function getSessionData(sessionId) {
    // 1. Check Cache
    const cachedData = sessionCache.get(sessionId);
    if (cachedData) {
        // console.log("✅ Cache Hit:", sessionId);
        return cachedData;
    }

    // 2. Check Lock
    if (refreshInProgress.has(sessionId)) {
        // console.log("⏳ Waiting for existing refresh:", sessionId);
        await refreshInProgress.get(sessionId);
        return sessionCache.get(sessionId);
    }

    // console.log("🚀 Starting new fetch for:", sessionId);

    // 3. Create the actual work promise
    const fetchWork = async () => {
        try {
            const newSessionData = await fetchTokensOfSession(sessionId);

            if (!newSessionData) {
                throw new Error('Backend returned empty session data');
            }

            // const ttl = newSessionData.session_expires_in || 3600;

            // newSessionData.session_id = sessionId;

            sessionCache.set(sessionId, newSessionData);
            console.log("💾 Cache Updated for:", sessionId);

            return newSessionData;
        } catch (error) {
            console.error("❌ Fetch internal error:", error.message);
            throw error; // Re-throw so the lock-waiters and caller know it failed
        } finally {
            refreshInProgress.delete(sessionId);
            // console.log("🔓 Lock released for:", sessionId);
        }
    };

    // 4. Store the promise and execute
    const p = fetchWork();
    refreshInProgress.set(sessionId, p);

    return await p;
}

async function setUpGuestSession(res) {
    try
    {
        const guestData = await createGuestSession();
        const {session_id, session_ttl} = guestData;

        sessionCache.set(session_id, {session_id, is_guest: true, remember_me: false});
        // sessionId = session_id;

        res.cookie('session_id', session_id, {
            maxAge: session_ttl * 1000,
            secure: false,
            path: '/',
            sameSite: 'lax',
            httpOnly: true
        });

        console.log("Guest session id for return: ", session_id);
        return session_id;
    }
    catch (error)
    {

        // if (error.response) {
        //
        //     const errorResponse = error.response.data;
        //
        //     if (errorResponse?.guestError) {
        //
        //         console.log("Guest se");
        //
        //         const cartSummaryResponse = await getCartSummary(req, res, sessionId);
        //         const summaryData = cartSummaryResponse?.data;
        //
        //         return res.status(200).json({authenticated: false, cartSummary: summaryData});
        //     }
        // }
        console.error("Error creating guest session: ", error);
    }
}

export async function fetchWithSessionTokens(sessionId, requestFn, options = {}) {
    const {isMe= false, req, res } = options;

    // Helper to safely format responses
    const makeResponse = (axiosResponse) => ({
        status: axiosResponse?.status || 200,
        data: axiosResponse?.data ?? {},
        headers: axiosResponse?.headers ?? {}
    });

    try {
        // --- 1. Session Setup ---
        if (!sessionId) {
            sessionId = await setUpGuestSession(res);

        }

        // --- 2. Safe Token Retrieval ---
        let sessionData;
        try {
            sessionData = await getSessionData(sessionId);

        } catch (err) {

            // console.error("Error fetching session data:", err);

            return { status: 401, data: { error: 'Session expired or unable to fetch tokens' }, headers: {} };
        }

        // console.log("SessionData.is_guest = "+ sessionData.is_guest + " isMe = " + isMe)

        if (res) {
            res.setHeader('x-session-user', sessionData.is_guest ? 'guest' : 'authenticated');

            // console.log("Setting x-session-user header to " + (sessionData.is_guest ? 'guest' : 'authenticated'));
            res.setHeader('Access-Control-Expose-Headers', 'x-session-user');
        }

        if (sessionData.is_guest && isMe) {

            // console.log("Throwing isMe guest error");

            const guestErr = new Error("isMe guest error");
            guestErr.response = {
            data:{
                guestError: true
            },
                status: 401}
            guestErr.isAxiosError = true;

            throw guestErr;
        }

        // --- 3. Execute Original Request ---
        const axiosResponse = await requestFn(sessionData);
        return makeResponse(axiosResponse);

    } catch (err) {

        const normalizedError = new Error(err.message);

        if (err.isAxiosError && err.response) {
            normalizedError.response = {
                status: err.response.status,
                data: err.response.data
            };
        } else {
            // Handle network timeouts/internal BFF crashes
            normalizedError.response = {
                status: err.status || 500,
                data: { error: err.message || 'Internal BFF Error' }
            };
        }

        // 2. THROW it. Do not return it.
        throw normalizedError;


        // if (err.message === "isMe guest error") {
        //     return { status: 401, data: { guestError: true }, headers: {} };
        // }
        // // --- 4. Global Error Normalization ---
        // if (err.isAxiosError) {
        //     // Fallback to 502 if err.response is undefined (e.g., Network Timeout / ECONNREFUSED)
        //     const status = err.response?.status || 502;
        //     const data = err.response?.data ?? { error: err.message || 'Downstream request failed' };
        //     return { status, data, headers: err.response?.headers ?? {} };
        // }
        //
        // // Catch-all for code errors
        // return { status: 500, data: { error: err.message || 'Internal BFF Error' }, headers: {} };
    }
}
