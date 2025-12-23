const express = require('express');
const router = express.Router();
const {Backend_Url} = require('./config.js');
const AuthURL = `${Backend_Url}/auth`;

router.post(`/login`, async (req, res) => {
    const {email, password} = req.body;

    // console.log("Node login: " + email + " " + password)

    const response = await fetch(`${AuthURL}/login`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({identifier: email, password: password})
    })

    // console.log("Node response: " + response.status)

    if (response.status !== 200) {
        return res.status(response.status).end();
    }

    const responseData = await response.json();
    const { access_token, refresh_token, expires_in, refresh_expires_in } = responseData;//TODO tuk zameni refresh_token sys session_id

    // console.log("Node response: " + JSON.stringify(responseData))

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
            httpOnly: true });

    // res.cookie('session_id', refresh_token,
    //     {
    //       maxAge: refresh_expires_in * 1000,
    //       secure: false,
    //       path: '/refresh',
    //       sameSite: 'lax',
    //       httpOnly: true });

    const userDataResponse = await fetch(`${Backend_Url}/customer/me`,
        {
            method: 'GET',
            headers: {
                Authorization: `Bearer ${access_token}`
            }
        });

    if (userDataResponse.status !== 200)
    {
        // console.log("Error fetching user data: " + userDataResponse.status);

        res.status(userDataResponse.status).end();
        return
    }

    const userData = await userDataResponse.json();

    // console.log("userData: " + JSON.stringify(userData));

    res.status(userDataResponse.status).json(userData);

});


router.post('/logout', async (req, res) => {
    const refreshToken = req.cookies.refresh_token;

    if (refreshToken) {
        const response = await fetch(`${AuthURL}/invalidate`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({refresh_token: refreshToken})
            }
        )
        // console.log("response status: " + response.status)
        // console.log("response: " + JSON.stringify(response));
    }


    res.cookie('access_token', '', {
        httpOnly: true,
        secure: false, // same as when you set it
        path: '/',
        sameSite: 'lax',
        maxAge: 0
    });

    // Clear refresh token
    res.cookie('refresh_token', '', {
        httpOnly: true,
        secure: false,
        path: '/auth',
        sameSite: 'lax',
        maxAge: 0
    });


    res.status(200).end();
});

router.post('/refresh', async (req, res) => {
    const refreshToken = req.cookies.refresh_token;

    // console.log("token: " + refreshToken);

    if (refreshToken) {
        const response = await fetch(`${AuthURL}/refresh`,
            {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({refresh_token: refreshToken})
            });

        // console.log("response status: " + response.status)

        if (response.status >= 200 && response.status <= 300) {
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
            res.status(200).end();
        } else
            res.status(response.status).end();

    }
    return res.status(401).end();
})

module.exports = router;
