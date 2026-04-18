import sessionCache from "./sessionCache.js";
import {Backend_Url, WEB_CLIENT_NAME} from '../routes/config.js';
import axios from 'axios';

const refreshInProgress = new Map();
export async function fetchTokensOfSession(sessionID) {
    if (!sessionID) return null;

    try
    {
        const {data} = await axios.get(`${Backend_Url}/auth/tokens`,
            {
                headers: {
                    'Content-Type': 'application/json',
                    'x-session-id': sessionID,
                    // 'x-client_type': WEB_CLIENT_NAME,
                }
            });
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

    // 3. Create the actual work promise
    const fetchWork = async () => {
        try {
            const newSessionData = await fetchTokensOfSession(sessionId);

            if (!newSessionData) {
                throw new Error('Backend returned empty session data');
            }

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
        console.error("Error creating guest session: ", error);
    }
}

export async function fetchWithSessionTokens(sessionId, requestFn, options = {}) {
    const {isMe= false, req, res } = options;
    req.headers['x-client_type'] = WEB_CLIENT_NAME;
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

        // if (res) {
        //     res.setHeader('x-session-user', sessionData.is_guest ? 'guest' : 'authenticated');
        //
        //     // console.log("Setting x-session-user header to " + (sessionData.is_guest ? 'guest' : 'authenticated'));
        //     res.setHeader('Access-Control-Expose-Headers', 'x-session-user');
        // }

        processSessionData(sessionData, res, isMe, sessionId);


        // --- 3. Execute Original Request ---

        const axiosResponse = await requestFn(sessionData);
        const responseBody = makeResponse(axiosResponse);

        processResponseHeaders(responseBody.headers ?? {}, res, sessionId);

        return responseBody;

    } catch (err) {

        // console.error("Error in fetchWithSessionTokens:", err);

        const normalizedError = new Error(err?.message);

        if (err.isAxiosError && err.response) {

            // console.log("Axios Error Response:", err.response.data);

            if (err.response.headers) {
                if (err.response?.headers) {
                    processResponseHeaders(err.response.headers, res, sessionId);
                }
            }

            normalizedError.response = {
                status: err.response.status,
                data: err.response.data
            };
        } else {
            // console.log("Non-Axios Error:", err);
            normalizedError.response = {
                status: err?.status || 500,
                data: { error: err?.message || 'Internal BFF Error' }
            };
        }

        // console.error("Normalized Error:", normalizedError);

        throw normalizedError;
    }

    function processSessionData(newSessionData, res, isMe = false, oldSessionId = null) {

        const {
            session_id: newSessionId,
            session_expires_in,
            is_guest,
        } = newSessionData;

        if (newSessionId !== oldSessionId) {
            sessionCache.safeDelete(oldSessionId);
            sessionCache.set(newSessionId, newSessionData);

            res.cookie('session_id', sessionId,
                {
                    maxAge: session_expires_in * 1000,
                    secure: false,
                    path: '/',
                    sameSite: 'lax',
                    httpOnly: true
                });
        }
        else {
            sessionCache.set(oldSessionId, newSessionData);
            res.cookie('session_id', oldSessionId,
                {
                    maxAge: session_expires_in * 1000,
                    secure: false,
                    path: '/',
                    sameSite: 'lax',
                    httpOnly: true
                });
        }

        if (is_guest && isMe) {

            const guestErr = new Error("isMe guest error");
            guestErr.response = {
                data:{
                    guestError: true
                },
                status: 401}
            guestErr.isAxiosError = true;

            throw guestErr;
        }

    }

    function processResponseHeaders(headers, res, oldSessionId) {

        if (headers) {
            const {'x-session-info': sessionData} = headers;

            if (!sessionData) return;

            const {session_id, session_ttl, is_guest, is_rememberMe, is_replaced} = sessionData;

            if (session_id) {
                if (is_replaced)
                {
                    res.cookie('session_id', session_id,
                        {
                            maxAge: (session_ttl ?? 3600) * 1000,
                            secure: false,
                            path: '/',
                            sameSite: 'lax',
                            httpOnly: true
                        });

                    sessionCache.safeDelete(oldSessionId);
                }

                sessionCache.set(session_id, {
                    session_id,
                    is_guest: is_guest,
                    remember_me: is_rememberMe
                });
            }
        }

    }
}
