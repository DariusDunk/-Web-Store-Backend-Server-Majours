const express = require('express');
const router = express.Router();
const {Backend_Url} = require('./config.js');
const AuthURL = `${Backend_Url}/auth`;
const sessionCache = require('../services/sessionCache.js');

router.post(`/register`, async (req, res) => {

    const {name, familyName, email, password} = req.body;

    // console.log("Node register" +
    //   "\nName: " + name +
    //   "\nFamily name: " + familyName +
    //   "\nEmail: " + email +
    //   "\nPassword: " + password)


    try {
        const response = await fetch(`${AuthURL}/register`, {
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

    const response = await fetch(`${AuthURL}/login`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({identifier: email, password: password})//todo tobavi rememberMe vv tqloto
    })

    // console.log("Node response: " + response.status)

    if (!response.ok) {
        return res.status(response.status).end();
    }

    const responseData = await response.json();
    const {
        access_token, refresh_token, expires_in, refresh_expires_in
        , session_id, session_expires_in
    } = responseData;//TODO tuk zameni refresh_token sys session_id

    // console.log("accessToken TTL from backend: " + expires_in + "\naccess token TTL for the frontend: " + expires_in * 1000);


    res.cookie('access_token', access_token,
        {
            maxAge: expires_in * 1000,
            secure: false,
            path: '/',
            sameSite: 'lax',
            httpOnly: true
        })

    res.cookie('refresh_token', refresh_token,
        {
            maxAge: refresh_expires_in * 1000,
            secure: false,
            path: '/auth',
            sameSite: 'lax',
            httpOnly: true
        });


    const userDataResponse = await fetch(`${Backend_Url}/customer/me`,
        {
            method: 'GET',
            headers: {
                Authorization: `Bearer ${access_token}`
            }
        });

    if (!userDataResponse.ok) {
        // console.log("Error fetching user data: " + userDataResponse.status);

        return res.status(userDataResponse.status).end();
    }
//todo tova e za kogato vsi4ko sys sesiite e setupnato

//     res.cookie('session_id', session_id,
//         {
//             maxAge: session_expires_in * 1000,
//             secure: false,
//             path: '/',
//             sameSite: 'lax',
//             httpOnly: true
//         });
//
//     sessionCache.set(session_id, {
//         access_token,
//         expires_in,
//         refresh_token,
//         refresh_expires_in,
//         remember_me:rememberMe
//     }, session_expires_in * 1000);

    const userData = await userDataResponse.json();

    // console.log("userData: " + JSON.stringify(userData));

    return res.status(userDataResponse.status).json(userData);

});


router.post('/logout', async (req, res) => {
    const refreshToken = req.cookies.refresh_token;
    const sessionId = req.cookies.session_id;

    if (refreshToken) {
        const response = await fetch(`${AuthURL}/invalidate/${encodeURIComponent(refreshToken)}`
        )
        if (!response.ok) console.log("Error invalidating token: " + response.statusText);
    }

    if (sessionId) {
        const response = await fetch(`${AuthURL}/invalidate/${encodeURIComponent(refreshToken) / encodeURIComponent(sessionId)}`
        )
        if (!response.ok) console.log("Error invalidating token and session: " + response.statusText);
    }

    res.cookie('access_token', '', {//TODO tuk zameni refresh_token/access_token sys session_id
        httpOnly: true,
        secure: false,
        path: '/',
        sameSite: 'lax',
        maxAge: 0
    });

    res.cookie('refresh_token', '', {
        httpOnly: true,
        secure: false,
        path: '/auth',
        sameSite: 'lax',
        maxAge: 0
    });

    //todo tova e za kogato vsi4ko sys sesiite e setupnato

    // res.cookie('session_id', sessionId,
    //     {
    //         maxAge: 0,
    //         secure: false,
    //         path: '/',
    //         sameSite: 'lax',
    //         httpOnly: true
    //     });
    //
    // sessionCache.delete(sessionId);

    return res.status(200).end();
});

router.post('/refresh', async (req, res) => {
    const refreshToken = req.cookies.refresh_token;
    const sessionId = req.cookies.session_id;

    // console.log("token: " + refreshToken);

    if (refreshToken) {
        const response = await fetch(`${AuthURL}/refresh/${encodeURIComponent(refreshToken)}`);

        // console.log("response status: " + response.status)

        if (response.ok) {
            const responseData = await response.json();
            const {
                access_token,
                refresh_token,
                expires_in: access_token_lifetime,
                refresh_expires_in: refresh_token_lifeTime
            } = responseData;

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
            return res.status(200).end();
        } else
            return res.status(response.status).end();

    }

    //todo tova e za kogato vsi4ko sys sesiite e setupnato

    // if (sessionId) {
    //
    //     const refreshToken2 = sessionCache.get(sessionId).refresh_token
    //
    //     const response = await fetch(`${AuthURL}/refresh/${encodeURIComponent(refreshToken2)}/ ${encodeURIComponent(sessionId)}`);
    //
    //     // console.log("response status: " + response.status)
    //
    //     if (response.ok) {
    //         const responseData = await response.json();
    //         const {
    //             access_token,
    //             refresh_token,
    //             expires_in: access_token_lifetime,
    //             refresh_expires_in: refresh_token_lifeTime,
    //             session_expires_in
    //         } = responseData;
    //
    //         res.cookie('session_id', sessionId,
    //             {
    //                 maxAge: session_expires_in * 1000,
    //                 secure: false,
    //                 path: '/',
    //                 sameSite: 'lax',
    //                 httpOnly: true
    //             });
    //
    //         const newTTL = session_expires_in * 1000;
    //
    //        sessionCache.ttl(sessionId, newTTL);
    //         return res.status(200).end();
    //     } else
    //         return res.status(response.status).end();
    //
    // }

    return res.status(401).end();
})

module.exports = router;
