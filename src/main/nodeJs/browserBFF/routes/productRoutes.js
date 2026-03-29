const express = require('express');
const router = express.Router();
const {Backend_Url} = require('./config.js');
const {safeJson} = require("../services/safeJsonFunc.js");
const {fetchWithSessionTokens} = require("../services/requestTokenManager");
const axios = require('axios');


router.get('/featured/:page', async (req, res) => {//todo prodylju sys sesiite ot tuk

    const page = req.params.page;

    try {
        const response = await axios.get(`${Backend_Url}/product/findall?${new URLSearchParams({page: page || 0})}`, {});
        const data = await response.data;
        return res.status(response.status).json(data);

    } catch (error) {
        console.error('Search: Error fetching data:', error);
        return res.status(error.status).end();
    }
});

router.get('/manufacturer/:manufacturerName/p:page', async (req, res) => {
    const {manufacturerName, page} = req.params;
    const sort = req.query.sort;

    try {
        const response = await axios.get(`${Backend_Url}/product/manufacturer/${manufacturerName}/p${page}?${new URLSearchParams({sort: sort || ''})}`, {});
        const responseData = await response.data;
        return res.status(response.status).json(responseData);

    } catch (error) {
        console.error('Manufacturer: Error fetching data:', error);
        return res.status(error.status).end();
    }
});

router.get('/category/:categoryName/p:page', async (req, res) => {
    const {categoryName, page} = req.params;
    const sort = req.query.sort;

    try {

        const response = await axios.get(`${Backend_Url}/product/category/${categoryName}/p${page}?${new URLSearchParams({sort: sort || ''})}`, {});

        const responseData = await response.data;
        return res.status(response.status).json(responseData);

    } catch (error) {
        console.error('Category: Error fetching data:', error);
        return res.status(error.status).end();
    }
});

router.get(`/review/overview/:productCode`, async (req, res) => {
    const {productCode} = req.params;

    if (!productCode) {
        return res.status(400).json({error: 'Missing required parameters'});
    }

    try {

        const response = await axios.get(`${Backend_Url}/product/${productCode}/review/overview`, {});
        const responseData = await response.data;
        return res.status(response.status).json(responseData);

    } catch (error) {
        console.error('Error fetching product review overview from backend:', error);
        return res.status(error.status).end();
    }
})

router.get('/detail/:productCode', async (req, res) => {
    const {productCode} = req.params;
    const sessionId = req.cookies.session_id;

    if (!productCode || !sessionId) {
        return res.status(400).json({error: 'Missing required parameters'});
    }
    try {

        const [productDetailResponse, ratingOverviewResponse] =
            await fetchWithSessionTokens(sessionId, async (tokens) =>
                Promise.all([
                    axios.get(`${Backend_Url}/product/${productCode}`, {
                        headers: {
                            'Content-Type': 'application/json',
                            'Authorization': 'Bearer ' + tokens.access_token
                        }
                    }),
                    axios.get(`${Backend_Url}/product/${productCode}/review/overview`, {
                        headers: {
                            'Content-Type': 'application/json',
                            'Authorization': 'Bearer ' + tokens.access_token
                        }
                    })
                ])
            );

        const productDetails = productDetailResponse.data;
        const ratingOverview = ratingOverviewResponse.data;

        return res.status(200).json({productDetails, ratingOverview});

    } catch (error) {
        console.error('Error fetching data from backend:', error);
        return res.status(error.status).end();
    }
});

router.get('/suggest/:name', async (req, res) => {
    try {

        const name = req.params.name;
        const response = await axios.get(`${Backend_Url}/product/suggest?${new URLSearchParams({name: name || ''})}`);
        const data = await response.data;

        return res.status(response.status).json(data);

    } catch (error) {
        console.error('Suggest: Error fetching suggestions from backend:', error);
        return res.status(500).end();
    }
});

router.get(`/search`, async (req, res) => {

    const {searchText, page, sort} = req.query;

    try {

        const url = new URL('/product/search', Backend_Url);

        url.searchParams.set('name', searchText);
        url.searchParams.set('page', page);
        url.searchParams.set('sort', sort || '');

        const response = await axios.get(url.toString());

        const data = await response.data;
        return res.status(response.status).json(data);

    } catch (error) {
        console.error('Search: Error fetching data:', error);
        return res.status(500).end();
    }
});

router.get('/category-filter/:category/pg:page', async (req, res) => {

    // console.log('filter search');

    // Parse page (it's the number after 'p', e.g., '0' for first page)
    const page = parseInt(req.params.page, 10);
    if (isNaN(page)) {
        return res.status(400).json({error: 'Invalid page parameter'});
    }

    const category = decodeURIComponent(req.params.category);
    const {filters = {}, sort} = req.query;
    let minPrice = 0;
    let maxPrice = Infinity;  // Or some default max

    if (filters.pr) {

        const priceRange = filters.pr.split('-');
        minPrice = parseInt(priceRange[0], 10) || 0;
        maxPrice = parseInt(priceRange[1], 10) || Infinity;
    }

    let manufacturers = [];

    if (filters.m) {
        if (Array.isArray(filters.m)) {
            manufacturers = filters.m.map(decodeURIComponent);
        } else if (filters.m.includes(',')) {
            manufacturers = filters.m.split(',').map(decodeURIComponent);
        } else {
            manufacturers = [decodeURIComponent(filters.m)];
        }
    }

    const rating = filters.r ? filters.r : null;  // Assuming ratings are numbers

    const attributes = {};
    Object.keys(filters).forEach(key => {
        if (key.startsWith('a')) {
            const nameId = key.slice(1);  // e.g., '1' for 'a1'
            attributes[nameId] = filters[key].split(',').map(decodeURIComponent);
        }
    });

    const requestBody = {
        filter_attributes: attributes,
        product_category: category,
        price_lowest: minPrice,
        price_highest: maxPrice,
        manufacturer_names: manufacturers,
        rating: rating,
        sort: sort
    };

    try {

        const response = await axios.post(`${Backend_Url}/product/filter/${page}`, requestBody, {});

        const responseData = await response.data;
        return res.status(response.status).json(responseData);

    } catch (error) {
        console.error('Error fetching products with filters:', error);
        return res.status(500).end();
    }
});

router.post('/getPagedReviews', async (req, res) => {

    const productCode = req.body.productCode;
    const page = req.body.page;
    const sort = req.body.sortOrder;
    const verifiedOnly = req.body.verifiedOnly;
    const ratingValue = req.body.ratingValue
    const sessionId = req.cookies.session_id;

    try {
        const response = await fetchWithSessionTokens(sessionId, async (tokens) => {
            return await axios.post(`${Backend_Url}/product/reviews/paged`, {
                product_code: productCode,
                page,
                sort_order: sort,
                verified_only: verifiedOnly,
                rating_value: ratingValue
            }, {
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': 'Bearer ' + tokens.access_token
                }
            })
        })

        const responseData = await response.data;

        return res.status(response.status).json(responseData);

    } catch (error) {
        console.error('Reviews: Error fetching data: ', error);
        return res.status(500).json({error: 'Internal server error'});
    }
})

router.get(`/getReview/:productCode`, async (req, res) => {
    const {productCode} = req.params;
    const sessionId = req.cookies.session_id;

    try {

        const response = await fetchWithSessionTokens(sessionId, async (tokens) => {
            return await axios.get(`${Backend_Url}/product/review/specific?${new URLSearchParams({productCode: productCode || ''})}`, {
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': 'Bearer ' + tokens.access_token
                }
            })
        })

        const responseData = await response.data;
        return res.status(response.status).json(responseData);

    } catch (error) {

        if (error.response) {
            console.warn('Handled backend error for fetching specific review data');
            return res.status(error.response.status||500).json(error.response.data);
        }

        console.error('-------------------Unexpected error fetching specific review data-------------------\n', error);
        return res.status(500).json({error: 'Internal server error'});
    }
})

router.post(`/addReview`, async (req, res) => {
    const productCode = req.body.productCode;
    const rating = req.body.rating;
    const reviewText = req.body.reviewText;
    const sessionId = req.cookies.session_id;

    try {
        const response = await fetchWithSessionTokens(sessionId, async (tokens) => {
            return await axios.post(`${Backend_Url}/product/review/add`, {
                product_code: productCode,
                rating: rating,
                review_text: reviewText
            }, {
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': 'Bearer ' + tokens.access_token
                }
            })
        })

        const responseData = await response.data;
        return res.status(response.status).json(responseData);
    } catch (error) {

        if (error.response) {
            console.warn('Handled backend error for creating review');
            return res.status(error.response.status||500).json(error.response.data);
        }

        console.error('-------------------Unexpected error creating review-------------------\n', error);
        return res.status(500).json({error: 'Internal server error'});
    }
})

router.post(`/updateReview`, async (req, res) => {
    const sessionId = req.cookies.session_id;
    const productCode = req.body.productCode;
    const rating = req.body.rating;
    const reviewText = req.body.reviewText;

    try {

        const response = await fetchWithSessionTokens(sessionId, async (tokens) => {
            return await axios.patch(`${Backend_Url}/product/review/update`, {
                product_code: productCode,
                rating: rating,
                review_text: reviewText
            }, {
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': 'Bearer ' + tokens.access_token
                }
            })
        })

        const responseData = await response.data;
        return res.status(response.status).json(responseData);

    } catch (error) {

        if (error.response) {
            console.warn('Handled backend error for updating the review');
            return res.status(error.response.status||500).json(error.response.data);
        }

        console.error('-------------------Error updating the review-------------------\n', error);
        return res.status(500).end();
    }
})

router.post(`/deleteReview`, async (req, res) => {
    const {productCode} = req.body;
    const accessToken = req.cookies['access_token'];

    // console.log( "INSIDE DELETE REVIEW: " + "Product: " + productCode + " Customer: " + customerId)


    try {
        const response = await fetch(`${Backend_Url}/product/review/delete?product_code=${productCode}`,
            {
                method: 'DELETE',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': 'Bearer ' + accessToken
                }
            })

        if (!response.ok) {
            return res.status(response.status).end();
        }

        return res.status(response.status).json(response.statusText);
    } catch (error) {
        console.error('Error deleting the review ', error);
        return res.status(500).json({error: 'Internal server error'});
    }
})


module.exports = router
