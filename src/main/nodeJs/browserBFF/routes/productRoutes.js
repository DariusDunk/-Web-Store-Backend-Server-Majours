import express, {response} from 'express';
const router = express.Router();
import {Backend_Url} from './config.js';
import {fetchWithSessionTokens} from "../services/requestTokenManager.js";
import axiosBackendClient from '../axiosBackendClient.js';

const timestamp = () => {
    const now = new Date();
    return `[${now.getFullYear()}-${String(now.getMonth()+1).padStart(2,'0')}-${String(now.getDate()).padStart(2,'0')} ${String(now.getHours()).padStart(2,'0')}-${String(now.getMinutes()).padStart(2,'0')}-${String(now.getSeconds()).padStart(2,'0')}]`;
};

router.get('/featured/:page', async (req, res) => {

    const page = req.params.page;
    const sessionId = req.cookies.session_id;

    try {
        const response = await axiosBackendClient.get(`${Backend_Url}/product/findall?${new URLSearchParams({page: page || 0})}`, {
            headers: {
                ...(sessionId && {'X-Session-Id': sessionId})
            }
        });
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
    const sessionId = req.cookies.session_id;

    try {
        const response = await axiosBackendClient.get(`${Backend_Url}/product/manufacturer/${manufacturerName}/p${page}?${new URLSearchParams({sort: sort || ''})}`, {
            headers: {
                ...(sessionId && {'X-Session-Id': sessionId})
            }
        });
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
    const sessionId = req.cookies.session_id;

    try {

        const response = await axiosBackendClient.get(`${Backend_Url}/product/category/${categoryName}/p${page}?${new URLSearchParams({sort: sort || ''})}`, {
            headers: {
                ...(sessionId && {'X-Session-Id': sessionId})
            }
        });

        const responseData = await response.data;
        return res.status(response.status).json(responseData);

    } catch (error) {
        console.error('Category: Error fetching data:', error);
        return res.status(error.status).end();
    }
});

router.get(`/review/overview/:productCode`, async (req, res) => {
    const {productCode} = req.params;
    const sessionId = req.cookies.session_id;

    if (!productCode) {
        return res.status(400).end();
    }

    try {

        const response = await axiosBackendClient.get(`${Backend_Url}/product/${productCode}/review/overview`, {
            headers: {
                ...(sessionId && {'X-Session-Id': sessionId})
            }
        });
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
        return res.status(400).end();
    }
    try {
        const response =
         await fetchWithSessionTokens(sessionId, async (tokens) => {
                 const [prodReq, reviewReq] = await Promise.all([
                     axiosBackendClient.get(`${Backend_Url}/product/${productCode}`, {
                         headers: {
                             'Content-Type': 'application/json',
                             'Authorization': 'Bearer ' + tokens.access_token,
                             ...(sessionId && {'X-Session-Id': sessionId}),
                         },
                         bffContext: {
                             req, res
                         }
                     }),
                     axiosBackendClient.get(`${Backend_Url}/product/${productCode}/review/overview`, {
                         headers: {
                             'Content-Type': 'application/json',
                             'Authorization': 'Bearer ' + tokens.access_token,
                             ...(sessionId && {'X-Session-Id': sessionId}),
                         },
                         bffContext: {
                             req, res
                         }
                     })
                 ])

                 return {
                     data: {
                         productDetails: prodReq.data,
                         ratingOverview: reviewReq.data
                     }
                 };
             }
            , {req, res});

        // const productDetails = productDetailResponse.data;
        // const ratingOverview = ratingOverviewResponse.data;

        return res.status(200).json(response.data);

    } catch (error) {
        console.error('Error fetching data from backend:', error);
        return res.status(error.status||500).end();
    }
});

router.get('/suggest/:name', async (req, res) => {
    try {

        const name = req.params.name;
        const sessionId = req.cookies.session_id;
        const response = await axiosBackendClient.get(`${Backend_Url}/product/suggest?${new URLSearchParams({name: name || ''})}`, {
            headers: {
                ...(sessionId && {'X-Session-Id': sessionId})
            }
        });
        const data = await response.data;

        return res.status(response.status).json(data);

    } catch (error) {
        console.error('Suggest: Error fetching suggestions from backend:', error);
        return res.status(500).end();
    }
});

router.get(`/search`, async (req, res) => {

    const {searchText, page, sort} = req.query;
    const sessionId = req.cookies.session_id;

    try {

        const url = new URL('/product/search', Backend_Url);

        url.searchParams.set('name', searchText);
        url.searchParams.set('page', page);
        url.searchParams.set('sort', sort || '');

        const response = await axiosBackendClient.get(url.toString(), {
            headers: {
                ...(sessionId && {'X-Session-Id': sessionId})
            }
        });

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
    const sessionId = req.cookies.session_id;
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

        const response = await axiosBackendClient.post(`${Backend_Url}/product/filter/${page}`, requestBody, {
            headers: {
                ...(sessionId && {'X-Session-Id': sessionId})
            }
        });

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
            const respons = await axiosBackendClient.post(`${Backend_Url}/product/reviews/paged`, {
                product_code: productCode,
                page,
                sort_order: sort,
                verified_only: verifiedOnly,
                rating_value: ratingValue
            }, {
                headers: {
                    'Content-Type': 'application/json',
                   ...(tokens?.access_token && {'Authorization': 'Bearer ' + tokens.access_token}),
                    ...(sessionId && { 'X-Session-Id': sessionId })
                },
                bffContext: {
                    req, res
                }
            })

            // console.log('pagedReview response:', respons);

            return respons;
        }, {req, res})

        // console.log('After wrapper: response:', response);
        // console.log('response?.status:', response?.status);
        // console.log('response?.data:', response?.data);

        const responseData = response?.data;

        // console.log('response status:', response?.status);
        // console.log('response data:', responseData);

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
            return await axiosBackendClient.get(`${Backend_Url}/product/review/specific?${new URLSearchParams({productCode: productCode || ''})}`, {
                headers: {
                    'Content-Type': 'application/json',
                   ...(tokens?.access_token && {'Authorization': 'Bearer ' + tokens.access_token}),
                    ...(sessionId && { 'X-Session-Id': sessionId })
                },
                bffContext: {
                    req, res
                }
            })
        }, {req, res})

        const responseData = await response.data;
        return res.status(response.status).json(responseData);

    } catch (error) {

        if (error.response) {
            console.warn(`${timestamp()} Handled backend error for fetching specific review data`);
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
            return await axiosBackendClient.post(`${Backend_Url}/product/review/add`, {
                product_code: productCode,
                rating: rating,
                review_text: reviewText
            }, {
                headers: {
                    'Content-Type': 'application/json',
                   ...(tokens?.access_token && {'Authorization': 'Bearer ' + tokens.access_token}),
                    ...(sessionId && { 'X-Session-Id': sessionId })
                },
                bffContext: {
                    req, res
                }
            })
        }, {req, res})

        const responseData = await response.data;
        return res.status(response.status).json(responseData);
    } catch (error) {

        if (error.response) {
            console.warn(`${timestamp()} Handled backend error for creating review`);
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
            return await axiosBackendClient.patch(`${Backend_Url}/product/review/update`, {
                product_code: productCode,
                rating: rating,
                review_text: reviewText
            }, {
                headers: {
                    'Content-Type': 'application/json',
                   ...(tokens?.access_token && {'Authorization': 'Bearer ' + tokens.access_token}),
                    ...(sessionId && { 'X-Session-Id': sessionId })
                },
                bffContext: {
                    req, res
                }
            })
        }, {req, res})

        const responseData = await response.data;
        return res.status(response.status).json(responseData);

    } catch (error) {

        if (error.response) {
            console.warn(`${timestamp()} Handled backend error for updating the review`);
            return res.status(error.response.status||500).json(error.response.data);
        }

        console.error('-------------------Error updating the review-------------------\n', error);
        return res.status(500).end();
    }
})

router.post(`/deleteReview`, async (req, res) => {
    const {productCode} = req.body;
    const sessionId = req.cookies.session_id;

    try {

        const response = await fetchWithSessionTokens(sessionId, async (tokens) => {
            return await axiosBackendClient.delete(`${Backend_Url}/product/review/delete?${new URLSearchParams({product_code: productCode || ''})}`, {
                headers: {
                    'Content-Type': 'application/json',
                   ...(tokens?.access_token && {'Authorization': 'Bearer ' + tokens.access_token}),
                    ...(sessionId && { 'X-Session-Id': sessionId })
                },
                bffContext: {
                    req, res
                }
            })
        }, {req, res})

        const responseData = await response.data;
        return res.status(response.status).json(responseData);

    } catch (error) {

        console.error('Error deleting the review ', error);
        return res.status(500).end();
    }
})

export default router;
