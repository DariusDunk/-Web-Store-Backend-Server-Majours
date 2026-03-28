const express = require('express');
const router = express.Router();
const {Backend_Url} = require('./config.js');
const {safeJson} = require('../services/safeJsonFunc.js');
// const sessionCache = require('../services/sessionCache.js');
const {fetchWithSessionTokens} = require("../services/requestTokenManager.js");
const axios = require("axios");

router.get('/getFavourites/:page', async (req, res) => {
    const page = req.params.page
    const sessionId = req.cookies.session_id;

    try {

        const response = await fetchWithSessionTokens(sessionId, async (tokens) => {
            return await axios.get(`${Backend_Url}/customer/favourites/p/${page}`, {
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': 'Bearer ' + tokens.access_token,
                }
            });
        })

        const responseData = await response.data;
        return res.status(response.status).json(responseData);
    } catch (error) {

        if (error.response) {
            console.warn('Handled backend error for fetching favourites');
            return res.status(error.response.status||500).end();
        }

        console.error('-------------------Unexpected error fetching favourites-------------------\n', error);
        return res.status(500).end();
    }
})

router.post(`/addFavourite/:productCode`, async (req, res) => {

    const sessionId = req.cookies.session_id;
    const productCode = req.params.productCode;

    try {

        const response = await fetchWithSessionTokens(sessionId, async (tokens) => {
            return await axios.post(`${Backend_Url}/customer/favorite/add/${productCode}`, {}, {
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': 'Bearer ' + tokens.access_token,
                }
            });
        });

        const responseData = await response.data;

        if (responseData)
            return res.status(response.status).json(responseData);
        else
            return res.status(response.status).end();


    } catch (error) {

        if (error.response) {
            console.warn('Handled backend error for adding product to favourites');
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

        const response = await fetchWithSessionTokens(sessionId, async (tokens) =>{
            return await axios.delete(`${Backend_Url}/customer/favorite/remove/single`, {
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': 'Bearer ' + tokens.access_token,
                },
                data: JSON.stringify(requestBody)
            });
        })

        const responseData = await response.data;

        return res.status(response.status).json(responseData);

    } catch (error) {

        if (error.response) {
            console.warn('Handled backend error for removing product through the favourites page');
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

        const response = await fetchWithSessionTokens(sessionId, async (tokens) => {
            return await axios.delete(`${Backend_Url}/customer/favourites/remove/${productCode}`, {
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': 'Bearer ' + tokens.access_token,
                }
            });
        });

        return res.status(response.status).end();

    } catch (error) {

        if (error.response) {
            console.warn('Handled backend error for removing product from favourites through the product page');
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

        const response = await fetchWithSessionTokens(sessionId, async (tokens) => {
            return await axios.delete(`${Backend_Url}/customer/favorite/remove/batch`, {
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': 'Bearer ' + tokens.access_token,
                },
                data: JSON.stringify({current_page: currentPage, product_codes: productCodes})
            });
        })

        const responseData = await response.data;

        return res.status(response.status).json(responseData);

    } catch (error) {

        if (error.response) {
            console.warn('Handled backend error for batch deleting products from favourites');
            return res.status(error.response.status||500).end();
        }
        console.error('-------------------Unexpected error batch deleting products from favourites-------------------\n', error);
        return res.status(500).end();
    }
})

router.post('/addToCart', async (req, res) => {
    try {
        const {productCode, doIncrement} = req.body;
        const sessionId = req.cookies.session_id;

        const response = await fetchWithSessionTokens(sessionId, async (tokens) => {
            return await axios.post(`${Backend_Url}/customer/cart/manageQuant`, {product_code: productCode, do_increment: doIncrement}, {
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': 'Bearer ' + tokens.access_token,
                }
            });
        })

        const responseData = await response.data;

        return res.status(response.status).json({message: responseData});

    } catch (error) {

        if (error.response) {
            console.warn('Handled backend error for adding product to cart');
            return res.status(error.response.status||500).json(error.response.data);
        }

        console.error('-------------------Unexpected error adding product to cart-------------------\n', error);
        return res.status(500).end();
    }
});

router.post('/addToCart/batch', async (req, res) => {
    try {
        const productCodes = req.body;
        const sessionId = req.cookies.session_id;

        const response = await fetchWithSessionTokens(sessionId, async (tokens) => {
            return await axios.post(`${Backend_Url}/customer/cart/add/batch`, productCodes, {
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': 'Bearer ' + tokens.access_token,
                }
            });
        })

        const responseData = await response.data;

        if (responseData)
            return res.status(response.status).json(responseData);
        else
            return res.status(response.status).end();

    } catch (error) {

        if (error.response) {
            console.warn('Handled backend error for batch adding products to cart');
            return res.status(error.response.status||500).json(error.response.data);
        }

        console.error('-------------------Error batch adding products to cart-------------------\n', error);
        return res.status(500).end();
    }
})

router.post('/removeFromCart/:productCode', async (req, res) => {
    try {

        const productCode = req.params.productCode;
        const sessionId = req.cookies.session_id;

        const response = await fetchWithSessionTokens(sessionId, async (tokens) => {
            return await axios.delete(`${Backend_Url}/customer/cart/remove/${productCode}`, {
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': 'Bearer ' + tokens.access_token,
                }
            });
        })

        const responseData = await response.data;

        return res.status(response.status).json(responseData);

    } catch (error) {

        if (error.response) {
            console.warn('Handled backend error for removing product from cart');
            return res.status(error.response.status||500).end();
        }

        console.error('-------------------Error removing product from cart-------------------\n', error);
        return res.status(500).end();
    }
})

router.post(`/removeFromCart/batch/turbo`, async (req, res) => {
    try {

        const sessionId = req.cookies.session_id;
        const productCodes = req.body;

        const response = await fetchWithSessionTokens(sessionId, async (tokens) => {
            return await axios.delete(`${Backend_Url}/customer/cart/remove/batch`, {
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': 'Bearer ' + tokens.access_token,
                },
                data: JSON.stringify(productCodes)
            });
        })

        const responseData = await response.data;

        return res.status(response.status).json(responseData);

    } catch (error) {

        if (error.response) {
            console.warn('Handled backend error for batch removing product from cart');
            return res.status(error.response.status||500).end();
        }

        console.error('-------------------Error batch removing product from cart-------------------\n', error);
        return res.status(500).end();
    }
})

router.get('/getCart', async (req, res) => {
    const sessionId = req.cookies.session_id;

    try {
        const response = await fetchWithSessionTokens(sessionId, async (tokens) => {
            return await axios.get(`${Backend_Url}/customer/cart`, {
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': 'Bearer ' + tokens.access_token,
                }
            });
        })

        const responseData = await response.data;
        return res.status(response.status).json(responseData);

    } catch (error) {

        if (error.response) {
            console.warn('Handled backend error for fetching the cart');
            return res.status(error.response.status||500).end();
        }

        console.error('-------------------Unexpected error for fetching the cart-------------------\n', error);
        return res.status(500).end();
    }
});

router.get('/me', async (req, res) => {

    const sessionId = req.cookies.session_id;

    try {

        const response = await fetchWithSessionTokens(sessionId, async (tokens) => {
            return await axios.get(`${Backend_Url}/customer/me`, {
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': 'Bearer ' + tokens.access_token,
                }
            });
        })

        const responseData = await response.data;
        return res.status(response.status).json(responseData);

    } catch (error) {
        console.error('Error:', error);
        return res.status(500).end();
    }
})

module.exports = router
