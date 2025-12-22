const express =require( 'express');
const router = express.Router();
const { Backend_Url } = require('./config.js');

router.post('/logout', async (req, res) => {
    const refreshToken = req.cookies.refresh_token;

    if (refreshToken)
    {
        const response = await fetch(`${Backend_Url}/customer/invalidate`, {
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
        path: '/refresh',
        sameSite: 'lax',
        maxAge: 0
    });



    res.status(200).end();
});

router.post('/token', async (req, res) => {
    const refreshToken = req.cookies.refresh_token;

    console.log("token: " + refreshToken);

    if (refreshToken)
    {
        const response = await fetch(`${Backend_Url}/auth/refresh`,
            {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({refresh_token: refreshToken})
            });

        console.log("response status: " + response.status)

        if (response.status >= 200 && response.status <= 300) {
            const responseData = await response.json();
            const {access_token,  refresh_token, expires_in: access_token_lifetime, refresh_expires_in: refresh_token_lifeTime} = responseData;

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
                path: '/refresh',
                sameSite: `lax`,
                maxAge: refresh_token_lifeTime * 1000
            })
            res.status(200).end();
        }
        else
            res.status(response.status).end();

    }
})

module.exports = router;
