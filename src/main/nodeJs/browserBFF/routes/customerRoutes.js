import express from 'express';
import {Backend_Url} from './config.js';
// const sessionCache = require('../services/sessionCache.js');
import {fetchWithSessionTokens} from "../services/requestTokenManager.js";
import axiosBackendClient from '../axiosBackendClient.js';
import axios from "axios";
import sessionCache from "../services/sessionCache.js";

const router = express.Router();

const timestamp = () => {
    const now = new Date();
    return `[${now.getFullYear()}-${String(now.getMonth()+1).padStart(2,'0')}-${String(now.getDate()).padStart(2,'0')} ${String(now.getHours()).padStart(2,'0')}-${String(now.getMinutes()).padStart(2,'0')}-${String(now.getSeconds()).padStart(2,'0')}]`;
};

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
    });
}

router.get('/getFavourites/:page', async (req, res) => {
    const page = req.params.page
    const sessionId = req.cookies.session_id;

    try {

        const response = await fetchWithSessionTokens(sessionId, async (sessionData) => {
                return await axiosBackendClient.get(`${Backend_Url}/customer/favourites/p/${page}`, {
                headers: {
                    'Content-Type': 'application/json',
                   ...(sessionData?.access_token && {'Authorization': 'Bearer ' + sessionData.access_token}),
                    ...(sessionData.session_id && { 'X-Session-Id': sessionData.session_id })
                },
                bffContext: {
                    req,res
                }
            });
            }, {req, res})

        const responseData = await response.data;
        return res.status(response.status).json(responseData);
    } catch (error) {

        if (error.response) {
            console.warn(`${timestamp()} Handled backend error for fetching favourites`);

            const status = error.response.status;

            // console.log("error statis: ", status );

            return res.status(status||500).end();
        }

        console.error('-------------------Unexpected error fetching favourites-------------------\n', error);
        return res.status(500).end();
    }
})

router.post(`/addFavourite/:productCode`, async (req, res) => {

    const sessionId = req.cookies.session_id;
    const productCode = req.params.productCode;

    try {

        const response = await fetchWithSessionTokens(sessionId, async (sessionData) => {
                return await axiosBackendClient.post(`${Backend_Url}/customer/favorite/add/${productCode}`, {}, {
                headers: {
                    'Content-Type': 'application/json',
                   ...(sessionData?.access_token && {'Authorization': 'Bearer ' + sessionData.access_token}),
                    ...(sessionData.session_id && { 'X-Session-Id': sessionId })
                },
                bffContext: {
                    req, res
                }
            });
            }, {req, res});

        const responseData = await response.data;

        if (responseData)
            return res.status(response.status).json(responseData);
        else
            return res.status(response.status).end();


    } catch (error) {

        if (error.response) {
            console.warn(`${timestamp()} Handled backend error for adding product to favourites`);
            return res.status(error.response.status||500).json(error.response.data);
        }

        console.error('-------------------Unexpected error adding product to favourites-------------------\n', error);
        return res.status(500).json(error.response.data);
    }
});

router.post(`/removeFav/single`, async (req, res) => {

    const sessionId = req.cookies.session_id;

    const {productCode, currentPage} = req.body;

    const requestBody = {product_code: productCode, current_page: currentPage};

    try {

        const response = await fetchWithSessionTokens(sessionId, async (sessionData) =>{
                return await axiosBackendClient.delete(`${Backend_Url}/customer/favorite/remove/single`, {
                headers: {
                    'Content-Type': 'application/json',
                   ...(!sessionData?.is_guest && {'Authorization': 'Bearer ' + sessionData.access_token}),
                    ...(sessionData.session_id && {'X-Session-Id': sessionData.session_id}),
                },
                data: JSON.stringify(requestBody),
                bffContext: {
                    req, res
                }
            });
            }, {req, res})

        const responseData = await response.data;

        return res.status(response.status).json(responseData);

    } catch (error) {

        if (error.response) {
            console.warn(`${timestamp()} Handled backend error for removing product through the favourites page`);
            return res.status(error.response.status||500).end();
        }

        console.error('-------------------Unexpected error removing product through the favourites page-------------------\n', error);
        return res.status(500).end();
    }
})

router.post(`/removeFav/detProd/:productCode`, async (req, res) => {

    const sessionId = req.cookies.session_id;
    const productCode = req.params.productCode;

    try {

        const response = await fetchWithSessionTokens(sessionId, async (sessionData) => {
                return await axiosBackendClient.delete(`${Backend_Url}/customer/favourites/remove/${productCode}`, {
                headers: {
                    'Content-Type': 'application/json',
                   ...(!sessionData?.is_guest && {'Authorization': 'Bearer ' + sessionData.access_token}),
                    ...(sessionData.session_id && {'X-Session-Id': sessionData.session_id}),
                },
                bffContext: {
                    req, res
                }
            });
            }, {req, res});

        return res.status(response.status).end();

    } catch (error) {

        if (error.response) {
            console.warn(`${timestamp()} Handled backend error for removing product from favourites through the product page`);
            return res.status(error.response.status||500).end();
        }

        console.error('-------------------Unexpected error removing product from favourites through the product page-------------------\n', error);
        return res.status(500).end();
    }
})

router.post(`/removeFav/batch`, async (req, res) => {
    try {
        const sessionId = req.cookies.session_id;
        const {currentPage, productCodes} = req.body;

        const response = await fetchWithSessionTokens(sessionId, async (sessionData) => {
                return await axiosBackendClient.delete(`${Backend_Url}/customer/favorite/remove/batch`, {
                headers: {
                    'Content-Type': 'application/json',
                   ...(!sessionData?.is_guest && {'Authorization': 'Bearer ' + sessionData.access_token}),
                    ...(sessionData.session_id && {'X-Session-Id': sessionData.session_id}),
                },
                data: JSON.stringify({current_page: currentPage, product_codes: productCodes}),
                bffContext: {
                    req, res
                }
            });
            }, {req, res})

        const responseData = await response.data;

        return res.status(response.status).json(responseData);

    } catch (error) {

        if (error.response) {
            console.warn(`${timestamp()} Handled backend error for batch deleting products from favourites`);
            return res.status(error.response.status||500).end();
        }
        console.error('-------------------Unexpected error batch deleting products from favourites-------------------\n', error);
        return res.status(500).end();
    }
})

router.get('/me', async (req, res) => {

    const sessionId = req.cookies.session_id;

    if (!sessionId)
    {
        try
        {
            const guestResponse = await axios.get(`${Backend_Url}/auth/session/guest/create/Web`);
            const guestData = await guestResponse.data;
            const {session_id, session_ttl} = guestData;

            // console.log("Response from guest session creation: ", JSON.stringify(guestData));

            if (session_id && session_ttl)
            {
                sessionCache.set(session_id, {
                        session_id,
                        is_guest: true,
                        remember_me: false
                    },
                    session_ttl);

                res.cookie('session_id', session_id,
                    {
                        maxAge: session_ttl * 1000,
                        secure: false,
                        path: '/',
                        sameSite: 'lax',
                        httpOnly: true
                    });




                return res.status(200).json({authenticated: false});
            }
        }
        catch (error)
        {
            console.error('Error creating guest session:', error);
            return res.status(500).end();
        }
    }

    const isGuest = sessionCache.get(sessionId)?.is_guest;
    //
    // sessionCache.print();
    //
    // console.log("isGuest: ", isGuest);

    if (!isGuest)
    {
        try {
            const response = await fetchWithSessionTokens(sessionId, async (sessionData) => {
                    return await axiosBackendClient.get(`${Backend_Url}/customer/me`, {
                        headers: {
                            'Content-Type': 'application/json',
                            ...(!sessionData?.is_guest && {'Authorization': 'Bearer ' + sessionData.access_token}),
                            ...(sessionData.session_id && {'X-Session-Id': sessionData.session_id}),
                        },
                        bffContext: {
                            req, res
                        }
                    });
                },
                {isMe: true, req, res});

            const responseData = await response.data;

            if (responseData)
                responseData.authenticated = !responseData.is_guest||false;

            const cartSummaryResponse = await getCartSummary(req, res, sessionId);
            responseData.cartSummary = await cartSummaryResponse?.data;

            // console.log("responseData: ", JSON.stringify(responseData));

            return res.status(response.status).json(responseData);

        } catch (error) {

            if (error.response &&error.response.status === 401) {

                const errorResponse = error.response.data;

                if (errorResponse?.guestError) {

                    console.log("Guest error detected in '/me' request, creating guest session");

                    const cartSummaryResponse = await getCartSummary(req, res, sessionId);
                    const summaryData = cartSummaryResponse?.data;

                    return res.status(200).json({authenticated: false, cartSummary: summaryData});
                }
            }

            console.error('------------------------Error fetching user info------------------------\n', error);
            return res.status(500).end();
        }
    }
    else {
        const cartSummaryResponse = await getCartSummary(req, res, sessionId);
        const summaryData = cartSummaryResponse?.data;
        return res.status(200).json({authenticated: false, cartSummary: summaryData});
    }
})

export default router;
