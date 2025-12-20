const express =require( 'express');
const router = express.Router();
const { Backend_Url } = require('./config.js');

router.post('/logout', async (req, res) => {//TODO testvai
    // Clear access token
    const refreshToken = req.cookies.refresh_token;

    // console.log("REFRESH TOKEN: " + refreshToken);
    // if (!refreshToken) {
    //     return res.status(400).send('No refresh token');
    // }

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
        console.log("response status: " + response.status)
        console.log("response: " + JSON.stringify(response));
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

module.exports = router;
