import express from 'express';
const router = express.Router();
import {Backend_Url} from './config.js';
const AuthURL = `${Backend_Url}/auth`;
import sessionCache from '../services/sessionCache.js';
import {fetchWithSessionTokens} from "../services/requestTokenManager.js";
import axiosBackendClient from '../axiosBackendClient.js';
import axios from 'axios';

async function getCartSummary(req, res, sessionId) {
    return await fetchWithSessionTokens(sessionId, async (sessionData) => {
        return await axiosBackendClient.get(`${Backend_Url}/cart/summary`,
            {
                headers: {
                    'Content-Type': 'application/json',
                    ...(!sessionData?.is_guest && {'Authorization': 'Bearer ' + sessionData.access_token}),
                    ...(sessionData.session_id && {'X-Session-Id': sessionData.session_id})
                },
                bffContext: {req, res}
            }
        );
    },
        {req, res});
}

router.post(`/register`, async (req, res) => {

    const {name, familyName, email, password} = req.body;
    
    try {
        const response = await axios.post(`${AuthURL}/register`, {
            first_name: name,
            last_name: familyName,
            email: email,
            password: password
        });

        const responseData = response.data;

        if (responseData != null) {
            return res.status(response.status).json(responseData);
        }

        return res.status(response.status).end();
    } catch (error) {
        if (error.response) {
            const responseData = error.response.data;
            if (responseData != null) {
                return res.status(error.response.status).json(responseData);
            }
            return res.status(error.response.status).end();
        }
        console.error('Error with registration: ', error);
        return res.status(500).json({error: 'Internal server error'});
    }
})


router.post(`/login`, async (req, res) => {
    const {email, password, rememberMe = false} = req.body;

    const guestSessionId = req.cookies.session_id;

    // console.log("Node login:" + email + " " + password)
    let authResponse = null;
    const trimmedEmail = email.trim();
    try {

        const response = await axios.post(`${AuthURL}/login`, {
            identifier: trimmedEmail,
            password: password,
            remember_me: rememberMe,
            client_type: "Web"
        }, {
            headers: {
                'Content-Type': 'application/json',
            ...(guestSessionId && {'X-Session-Id': guestSessionId})
        }
    })


        const responseData = await response.data;

        const {
            access_token, refresh_token, expires_in, refresh_expires_in
            , session_id, session_expires_in
        } = responseData;

        authResponse = responseData;

        sessionCache.set(session_id, {
            session_id,
            access_token,
            expires_in,
            refresh_token,
            refresh_expires_in,
            is_guest: false,
            remember_me: rememberMe
        });

        res.cookie('session_id', authResponse.session_id,
            {
                maxAge: session_expires_in * 1000,
                secure: false,
                path: '/',
                sameSite: 'lax',
                httpOnly: true
            });
    }
    catch (error) {
        console.error('-------------Error with login-------------\n', error);
        return res.status(500).end();
    }

    if (!authResponse.session_id) {
        return res.status(400).end();
    }

    try {
        const userDataResponse = await fetchWithSessionTokens(authResponse.session_id, async (sessionData) => {
            const [userResponse, cartSummary] = await Promise.all([
                axiosBackendClient.get(`${Backend_Url}/customer/me`, {
                    headers: {
                        'Content-Type': 'application/json',
                        ...(sessionData?.access_token && {'Authorization': 'Bearer ' + sessionData.access_token}),
                        ...(sessionData.session_id && {'X-Session-Id': sessionData.session_id})
                    },
                    bffContext: {
                        req, res
                    }
                },
                    ),
                getCartSummary(req, res, authResponse.session_id)

            ]);

            return {
                data: {user: userResponse?.data, cartSummary: cartSummary?.data}
            }
        },{req, res});

        const userData = await userDataResponse.data;
        // console.log("UserData.data: "+ JSON.stringify(userData))
        return res.status(userDataResponse.status).json(userData);
    } catch (error) {
        console.error('Error fetching user data: ', error);
        return res.status(error.response?.status || 500).end();
    }
});


router.post('/logout', async (req, res) => {
    const sessionId = req.cookies.session_id;

        try {
          const response =  await fetchWithSessionTokens(sessionId, async (sessionData) => await axiosBackendClient.get(
                `${AuthURL}/invalidate/${encodeURIComponent(sessionData.refresh_token)}`,
                  {
                      headers: {
                          'Content-Type': 'application/json',
                          ...(!sessionData?.is_guest && {'Authorization': 'Bearer ' + sessionData.access_token}),
                          ...(sessionData.session_id && {'X-Session-Id': sessionData.session_id}),
                      },
                      bffContext: {
                          req, res
                      }
                  }
            ),
              {req, res});

          const responseData = await response.data;

            if (responseData) {
                const {session_id, session_ttl} = responseData;

                if (session_id && session_ttl)
                {
                    const summaryResponse = await getCartSummary(req, res, session_id);
                    const cartSummary = await summaryResponse?.data;
                    sessionCache.set(session_id, {
                            session_id,
                            is_guest: true,
                            remember_me: false
                        });

                    res.cookie('session_id', session_id,
                        {
                            maxAge: session_ttl * 1000,
                            secure: false,
                            path: '/',
                            sameSite: 'lax',
                            httpOnly: true
                        });

                    return res.status(200).json({authenticated: false, cartSummary: cartSummary});
                }
            }

        } catch (error) {
            console.error("Error invalidating token and session, cookies and sessionCache will still be erased: ", error);
            // clearSessionCookies(res, sessionId);
            sessionCache.safeDelete(sessionId);
            return res.status(200).end();
        }

});
export default router;
