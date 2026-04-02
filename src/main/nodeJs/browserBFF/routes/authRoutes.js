import express from 'express';
const router = express.Router();
import {Backend_Url} from './config.js';
const AuthURL = `${Backend_Url}/auth`;
import sessionCache from '../services/sessionCache.js';
import {fetchWithSessionTokens} from "../services/requestTokenManager.js";
import axiosBackendClient from '../axiosBackendClient.js';
import axios from 'axios';

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

    const guestSessionId = req.cookies('session_id');

    // console.log("Node login: " + email + " " + password)
    let authResponse = null;

    try {

        const response = await axios.post(`${AuthURL}/login`, {
            identifier: email,
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
            access_token,
            expires_in,
            refresh_token,
            refresh_expires_in,
            is_guest: false,
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
            return await axiosBackendClient.get(`${Backend_Url}/customer/me`, {
                headers: {
                    'Content-Type': 'application/json',
                    ...(tokens?.access_token && {'Authorization': 'Bearer ' + tokens.access_token}),
                    ...(authResponse.session_id && { 'X-Session-Id': authResponse.session_id })
                },
                bffContext: {
                    req, res
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
            await fetchWithSessionTokens(sessionId, async (tokens) => await axiosBackendClient.get(
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

export default router;
