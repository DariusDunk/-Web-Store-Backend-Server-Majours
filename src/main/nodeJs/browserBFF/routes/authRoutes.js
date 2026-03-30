const express = require('express');
const router = express.Router();
const {Backend_Url} = require('./config.js');
const AuthURL = `${Backend_Url}/auth`;
const sessionCache = require('../services/sessionCache.js');
const {fetchWithSessionTokens} = require("../services/requestTokenManager.js");
const axios = require("axios");

router.post(`/register`, async (req, res) => {

    const {name, familyName, email, password} = req.body;

    // console.log("Node register" +
    //   "\nName: " + name +
    //   "\nFamily name: " + familyName +
    //   "\nEmail: " + email +
    //   "\nPassword: " + password)


    try {
        const response = await fetch(`${AuthURL}/register`, {//todo smeni tova sys axios
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({first_name: name, last_name: familyName, email: email, password: password})
        });

        if (response.status !== 201) {
            const responseData = await response.json();

            if (responseData != null) {
                return res.status(response.status).json(responseData);
            }
        }

        return res.status(response.status).end();
    } catch (error) {
        console.error('Error with registration: ', error);
        return res.status(500).json({error: 'Internal server error'});
    }
})


router.post(`/login`, async (req, res) => {
    const {email, password, rememberMe = false} = req.body;

    // console.log("Node login: " + email + " " + password)
    let authResponse = null;

    try
    {

        const response = await axios.post(`${AuthURL}/login`, {
            identifier: email,
            password: password,
            remember_me: rememberMe,
            client_type: "Web"
        })

        const responseData = await response.data;

        const {
            access_token, refresh_token, expires_in, refresh_expires_in
            , session_id, session_expires_in
        } = responseData;

        authResponse = responseData;

        sessionCache.set(session_id, {
            access_token,
            expires_in,
            refresh_token,
            refresh_expires_in,
            remember_me: rememberMe
        }, session_expires_in);
    }
    catch (error) {
        console.error('-------------Error with login-------------\n', error);
        return res.status(500).end();
    }

    if (!authResponse.session_id) {
        return res.status(400).end();
    }

    try {
        const userDataResponse = await fetchWithSessionTokens(authResponse.session_id, async (tokens) => {
            return await axios.get(`${Backend_Url}/customer/me`, {
                headers: {
                    Authorization: `Bearer ${tokens.access_token}`
                }
            })
        });

        res.cookie('session_id', authResponse.session_id,
            {
                maxAge: authResponse.session_expires_in * 1000,
                secure: false,
                path: '/',
                sameSite: 'lax',
                httpOnly: true
            });

        // sessionCache.print();

        const userData = await userDataResponse.data;

        return res.status(userDataResponse.status).json(userData);
    } catch (error) {
        console.error('Error fetching user data: ', error);
        return res.status(error.response.status || 500).end();
    }
});


router.post('/logout', async (req, res) => {
    const sessionId = req.cookies.session_id;

    if (sessionId) {

        try {
            await fetchWithSessionTokens(sessionId, async (tokens) => await axios.get(
                `${AuthURL}/invalidate/${encodeURIComponent(tokens.refresh_token)}/${encodeURIComponent(sessionId)}`
            ));

        } catch (error) {
            console.error("Error invalidating token and session, cookies and sessionCache will still be erased: ", error);
        }

        clearSessionCookies(res, sessionId);

        return res.status(200).end();
    }
});

function clearSessionCookies(res, sessionId = null) {

    sessionCache.safeDelete(sessionId);

    res.cookie('session_id', '',
        {
            maxAge: 0,
            secure: false,
            path: '/',
            sameSite: 'lax',
            httpOnly: true
        });
}

router.post('/refresh', async (req, res) => {//todo tozi endpoint da se mahne kato priklu4i6 sys sesiite
    // const refreshToken = req.cookies.refresh_token;
    const sessionId = req.cookies.session_id;

    // console.log("token: " + refreshToken);

    // if (refreshToken) {
    //     const response = await fetch(`${AuthURL}/refresh/${encodeURIComponent(refreshToken)}`);
    //
    //     // console.log("response status: " + response.status)
    //
    //     if (response.ok) {
    //         const responseData = await response.json();
    //         const {
    //             access_token,
    //             refresh_token,
    //             expires_in: access_token_lifetime,
    //             refresh_expires_in: refresh_token_lifeTime
    //         } = responseData;
    //
    //         res.cookie('access_token', access_token, {
    //             httpOnly: true,
    //             secure: false,
    //             path: '/',
    //             sameSite: 'lax',
    //             maxAge: access_token_lifetime * 1000
    //         });
    //
    //         res.cookie('refresh_token', refresh_token, {
    //             httpOnly: true,
    //             secure: false,
    //             path: '/auth',
    //             sameSite: `lax`,
    //             maxAge: refresh_token_lifeTime * 1000
    //         })
    //         return res.status(200).end();
    //     } else
    //         return res.status(response.status).end();
    //
    // }


    if (sessionId) {
        //
        console.log("Refreshing tokens...");
        //
        // sessionCache.print();
        //
        // const sessionEntry = sessionCache.get(sessionId);
        //
        // console.log("sessionEntry for refresh: " + JSON.stringify(sessionEntry));
        //
        // const refreshToken2 = sessionEntry.refresh_token;
        //
        // console.log("refreshToken2: " + refreshToken2);

        // const response = await fetch(`${AuthURL}/refresh/${encodeURIComponent(refreshToken2)}/${encodeURIComponent(sessionId)}`);

        try {
            const response = await fetchWithSessionTokens(sessionId, async (tokens) => {
                return await axios.get(`${AuthURL}/refresh/${encodeURIComponent(tokens.refresh_token)}/${encodeURIComponent(sessionId)}`, {
                    headers: {
                        'Content-Type': 'application/json',
                    }
                });
            })

            // console.log("response status: " + response.status)

            const responseData = await response.json();
            const {
                access_token,
                refresh_token,
                expires_in: access_token_lifetime,
                refresh_expires_in: refresh_token_lifeTime,
                session_expires_in
            } = responseData;

            res.cookie('session_id', sessionId,
                {
                    maxAge: session_expires_in * 1000,
                    secure: false,
                    path: '/',
                    sameSite: 'lax',
                    httpOnly: true
                });

            res.cookie('access_token', access_token, {
                httpOnly: true,
                secure: false,
                path: '/',
                sameSite: 'lax',
                maxAge: access_token_lifetime * 1000
            });

            res.cookie('refresh_token', refresh_token, {
                httpOnly: true,
                secure: false,
                path: '/auth',
                sameSite: `lax`,
                maxAge: refresh_token_lifeTime * 1000
            })

            // const newTTL = session_expires_in ;

            sessionCache.safeDelete(sessionId);

            sessionCache.set(sessionId, {
                access_token,
                access_token_lifetime,
                refresh_token,
                refresh_token_lifeTime,
                remember_me: false
            }, session_expires_in);


            // sessionCache.ttl(sessionId, newTTL);
            return res.status(200).end();

        } catch (error) {
            console.error('Error refreshing token: ', error);
            return res.status(error.response.status || 500).end();
        }
    }

    return res.status(401).end();
})

module.exports = router;
