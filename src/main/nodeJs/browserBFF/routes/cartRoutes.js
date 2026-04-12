import express from 'express';
const router = express.Router();
import {Backend_Url} from './config.js';
const AuthURL = `${Backend_Url}/auth`;
import sessionCache from '../services/sessionCache.js';
import {fetchWithSessionTokens} from "../services/requestTokenManager.js";
import axiosBackendClient from '../axiosBackendClient.js';
import axios from 'axios';

const timestamp = () => {
    const now = new Date();
    return `[${now.getFullYear()}-${String(now.getMonth()+1).padStart(2,'0')}-${String(now.getDate()).padStart(2,'0')} ${String(now.getHours()).padStart(2,'0')}-${String(now.getMinutes()).padStart(2,'0')}-${String(now.getSeconds()).padStart(2,'0')}]`;
};

router.get('/getCart', async (req, res) => {
    const sessionId = req.cookies.session_id;

    try {
        const response = await fetchWithSessionTokens(sessionId, async (sessionData) => {
            return await axiosBackendClient.get(`${Backend_Url}/cart/get`, {
                headers: {
                    'Content-Type': 'application/json',
                    ...(!sessionData?.is_guest && {'Authorization': 'Bearer ' + sessionData.access_token}),
                    ...(sessionData.session_id && {'X-Session-Id': sessionData.session_id}),
                },
                bffContext: {
                    req, res
                }
            });
        }, {req, res})

        const cartResponseData = await response.data;


        const cartSummaryResponse = await fetchWithSessionTokens(sessionId, async (sessionData) => {
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
        });

        const cartSummaryData = await cartSummaryResponse?.data;

        const responseData = {cart: cartResponseData, cartSummary: cartSummaryData};

        return res.status(response.status).json(responseData);

    } catch (error) {

        if (error.response) {
            console.warn(`${timestamp()} Handled backend error for fetching the cart`);
            return res.status(error.response.status||500).end();
        }

        console.error('-------------------Unexpected error for fetching the cart-------------------\n', error);
        return res.status(500).end();
    }
});

router.get(`/summary`, async (req, res) => {

    const sessionId = req.cookies.session_id;

    try{
        const response = await fetchWithSessionTokens(sessionId, async (sessionData) => {
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
        })

        const responseData = await response.data;
        return res.status(response.status).json(responseData);
    }catch (error) {

        if (error.response) {
            console.warn(`${timestamp()} Handled backend error for fetching the cart summary`);
            return res.status(error.response.status||500).end();
        }
        console.error('-------------------Unexpected error for fetching the cart summary-------------------\n', error);
        return res.status(500).end();
    }
})

export default router;