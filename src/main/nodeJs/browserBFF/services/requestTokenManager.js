import sessionCache from "./sessionCache.js";
import {Backend_Url, WEB_CLIENT_NAME} from '../routes/config.js';
import axios from 'axios';

const refreshInProgress = new Map();

export async function fetchTokensOfSession(sessionID) {

    try {
        const {data} = await axios.get(`${Backend_Url}/auth/tokens`,
            {
                headers: {
                    'Content-Type': 'application/json',
                    'x-client_type': WEB_CLIENT_NAME,
                    ...(sessionID && {'x-session-id': sessionID})
                }
            });
        return data;
    } catch (error) {
        console.error("Error fetching tokens of session: ", error);
        throw error;
    }
}

//
// async function createGuestSession() {
//     try {
//         const {data} = await axios.get(`${Backend_Url}/auth/session/guest/create/Web`);
//
//         // console.log("Guest session data: ", data);
//
//         return data;
//     } catch (error) {
//         console.error("Error creating guest session: ", error);
//     }
// }

async function getSessionData(sessionId) {
    // 1. Check Cache
    const cachedData = sessionCache.get(sessionId);
    if (cachedData) {
        console.log("✅ Cache Hit:", sessionId);
        return {...cachedData, _isCacheHit: true};
    }

    // 2. Check Lock
    if (refreshInProgress.has(sessionId)) {
        // console.log("⏳ Waiting for existing refresh:", sessionId);
        return await refreshInProgress.get(sessionId);
        // return sessionCache.get(sessionId);
    }

    // 3. Create the actual work promise
    const fetchWork = async () => {
        try {
            // if (!newSessionData) {
            //     throw new Error('Backend returned empty session data');
            // }

            // console.log("💾 Cache Updated for:", sessionId);

            const freshData = await fetchTokensOfSession(sessionId);
            return {...freshData, _isCacheHit: false};
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

//
// async function setUpGuestSession(res) {
//     try
//     {
//         const guestData = await createGuestSession();
//         const {session_id, session_ttl} = guestData;
//
//         sessionCache.set(session_id, {session_id, is_guest: true, remember_me: false, session_ttl});
//         // sessionId = session_id;
//
//         res.cookie('session_id', session_id, {
//             maxAge: (session_ttl ?? 660) * 1000,
//             secure: false,
//             path: '/',
//             sameSite: 'lax',
//             httpOnly: true
//         });
//
//         console.log("Guest session id for return: ", session_id);
//         return session_id;
//     }
//     catch (error)
//     {
//         console.error("Error creating guest session: ", error);
//     }
// }

export async function fetchWithSessionTokens(sessionId, requestFn, options = {}) {
    const {isMe = false, req, res} = options;

    const makeResponse = (axiosResponse) => ({
        status: axiosResponse?.status || 200,
        data: axiosResponse?.data ?? {},
        headers: axiosResponse?.headers ?? {}
    });
    let sessionData;
    try {

        try {
            sessionData = await getSessionData(sessionId);
            processRefreshedOrCachedSessionData(sessionData, res, isMe, sessionId);
        } catch (err) {

            console.error("Error fetching session data:", err);

        }

        const axiosResponse = await requestFn(sessionData);

        // console.log("----------------------------------\nResponse Body for of original request: ", JSON.stringify(axiosResponse.data), "\n----------------------------------\n");

        const responseBody = makeResponse(axiosResponse);
        // console.log("----------------------------------\nResponse Body for header processing: ", responseBody, "\n----------------------------------\n");
        processResponseHeaders(responseBody, res, sessionId);
        handleGuestState(responseBody.headers, sessionData, res);

        // const {headers} = responseBody;
        // console.log("----------------------------------\nResponse headers: ", headers, "\n----------------------------------\n");
        return responseBody;

    } catch (err) {

        console.error("Error in fetchWithSessionTokens:", err);

        const normalizedError = new Error(err?.message);

        if (err.isAxiosError && err.response) {

            // console.log("Axios Error Response:", err.response.data);

            if (err.response.headers) {
                if (err.response?.headers) {
                    processResponseHeaders(err.response.headers, res, sessionId, sessionData);
                    handleGuestState(err.response.headers, sessionData, res);
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
                data: {error: err?.message || 'Internal BFF Error'}
            };
        }

        // console.error("Normalized Error:", normalizedError);

        throw normalizedError;
    }

    function handleGuestState(headers, sessionDataAfterCacheRefresh, res) {

        let {'x-session-info': sessionData} = headers;

        let responseSessionId = null;

        responseSessionId = sessionData?.session_id;
        const {is_guest, session_id: refreshSessionId} = sessionDataAfterCacheRefresh;

        if (responseSessionId) {

            res.setHeader('Access-Control-Expose-Headers', 'x-guest-state');

            res.setHeader('x-guest-state', is_guest
                ? "guest"
                : "authenticated");

        } else {
            const cacheData = sessionCache.get(refreshSessionId);

            if (cacheData) {

                res.setHeader('Access-Control-Expose-Headers', 'x-guest-state');

                res.setHeader('x-guest-state', cacheData?.is_guest
                    ? "guest"
                    : "authenticated");

            }
        }

        console.log("----------------------------------\nResponse headers: ", res.headers, "\n----------------------------------\n");

    }

    function processRefreshedOrCachedSessionData(newSessionData, res, isMe = false, oldSessionId = null) {


        if (newSessionData == null) {
            return;
        }

        const {
            session_id: newSessionId,
            access_token,
            refresh_token,
            access_expires_in,
            refresh_expires_in,
            session_expires_in,
            is_guest,
            is_remember_me,
            _isCacheHit,
        } = newSessionData;

        console.log("----------------------------------\nSession Data after refresh : ", newSessionData);


        console.log("Session TTL in refresh:", session_expires_in);
        console.log("Cookie maxAge in refresh:", (session_expires_in ?? 3600) * 1000);

        if (newSessionId && newSessionId !== oldSessionId) {

            console.log("----------------------------------\nReplacing session cookie from refresh request with new session:  \n" + newSessionId +
                "----------------------------------\n ");

            sessionCache.safeDelete(oldSessionId);
            // sessionCache.set(newSessionId, newSessionData);

            sessionCache.setSession(
                newSessionId,
                access_token,
                access_expires_in,
                refresh_token,
                refresh_expires_in,
                is_guest,
                is_remember_me,
                session_expires_in);

            res.cookie('session_id', newSessionId,
                {
                    maxAge: (session_expires_in ?? 660) * 1000,
                    secure: false,
                    path: '/',
                    sameSite: 'lax',
                    httpOnly: true
                });
        } else if (!_isCacheHit) {
            // sessionCache.set(oldSessionId, newSessionData);
            delete newSessionData._isCacheHit;

            sessionCache.setSession(
                oldSessionId,
                access_token,
                access_expires_in,
                refresh_token,
                refresh_expires_in,
                is_guest,
                is_remember_me,
                session_expires_in);
            res.cookie('session_id', oldSessionId,
                {
                    maxAge: (session_expires_in ?? 660) * 1000,
                    secure: false,
                    path: '/',
                    sameSite: 'lax',
                    httpOnly: true
                });
        }

        if (isMe && is_guest) {

            req.updatedSessionId = newSessionId;

            const guestErr = new Error("isMe guest error");
            guestErr.response = {
                data: {
                    guestError: true
                },
                status: 401
            }
            guestErr.isAxiosError = true;

            throw guestErr;
        }

    }

    function processResponseHeaders(responseObject, res, oldSessionId) {

        const {headers} = responseObject;

        console.log("----------------------------------\nResponse Headers: ", headers, "\n----------------------------------\n");

        if (headers) {
            let {'x-session-info': sessionData} = headers;

            if (!sessionData) return;

            if (typeof sessionData === 'string') {
                sessionData = JSON.parse(sessionData);
            }

            const {session_id, session_expires_in, is_guest, is_replaced, is_remember_me} = sessionData;

            console.log("Session TTL in headers:", session_expires_in);
            console.log("Cookie maxAge in headers:", (session_expires_in ?? 3600) * 1000);

            console.log("----------------------------------\nSession Data after request headers: ", sessionData);

            console.log("----------------------------------\nSession ID from headers: ", session_id);
            console.log("----------------------------------\nIs_replaced from headers: ", is_replaced);

            if (session_id) {
                responseObject.newSessionId = session_id;

                if (is_replaced) {
                    console.log("----------------------------------\nReplacing session cookie and cache from headers with new session:  \n" + session_id +
                        "----------------------------------\n ");
                    sessionCache.safeDelete(oldSessionId);
                }

                res.cookie('session_id', session_id,
                    {
                        maxAge: (session_expires_in ?? 660) * 1000,
                        secure: false,
                        path: '/',
                        sameSite: 'lax',
                        httpOnly: true
                    });

                sessionCache.setSession(
                    session_id,
                    null,
                    null,
                    null,
                    null,
                    is_guest,
                    is_remember_me,
                    session_expires_in
                );
            }
        }

    }
}
