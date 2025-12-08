const express =require( 'express');
const router = express.Router();
const { Backend_Url } = require('./config.js');

router.get('/featured/:page', async (req, res)=>{
  const queryParts = req.url.split("/");
  const page = queryParts[2];
  // console.log("inside featured");
  try {
    const response = await fetch(`${Backend_Url}/product/findall?page=${page}`);
    if (response.status === 404) {
      res.redirect('/404.html');
    }
    else
    if (!response.ok) {
      throw new Error('Network response was not ok ' + response.statusText);
    }
    else
    {
      const data = await response.json();
      res.json(data);
    }
  } catch (error)
  {
    console.error('Search: Error fetching data:', error);
    res.status(500).json({ error: 'Failed to fetch data from the real server' });
  }
});

router.get('/manufacturer/:manufacturerName/p:page', async (req, res) => {
  const { manufacturerName, page} = req.params;
  // console.log("Manufacturer");
  try {
    const response = await fetch(`${Backend_Url}/product/manufacturer/${manufacturerName}/p${page}`);
    if (response.status === 404) {
      res.redirect('/404.html');
    }
    else
    if (!response.ok) {
      throw new Error('Network response was not ok ' + response.statusText);
    }
    else
    {
      const data = await response.json();
      res.json(data);
    }
  } catch (error) {
    console.error('Manufacturer: Error fetching data:', error);
    res.status(500).json({ error: 'Failed to fetch data from the real server' });
  }
});

router.get('/category/:categoryName/p:page', async (req, res) => {
  const {categoryName, page} = req.params;
  // console.log("category");
  try {
    const response = await fetch(`${Backend_Url}/product/category/${categoryName}/p${page}`);
    if (response.status === 404) {
      res.redirect('/404.html');
    }
    else
    if (!response.ok) {
      throw new Error('Network response was not ok ' + response.statusText);
    }
    else
    {
      const data = await response.json();
      res.json(data);
    }
  } catch (error) {
    console.error('Category: Error fetching data:', error);
    res.status(500).json({ error: 'Failed to fetch data from the real server' });
  }
});

router.get('/detail/:productCode', async (req, res)=>{
  const { productCode } = req.params;
  const { id } = req.query;

  if (!productCode || !id) {
    return res.status(400).json({ error: 'Missing required parameters' });
  }
  try {
    const productDetailsResponse = await fetch(`${Backend_Url}/product/${productCode}?id=${id}`);

    const ratingOverviewResponse = await fetch(`${Backend_Url}/product/${productCode}/review/overview`);


    if (!ratingOverviewResponse.ok) {
      throw new Error('Product fetching error: ' + ratingOverviewResponse.statusText);
    }

    if (!productDetailsResponse.ok) {
      throw new Error('Rating overview fetching error: ' + productDetailsResponse.statusText);
    }

    const productDetails = await productDetailsResponse.json();
    const ratingOverview = await ratingOverviewResponse.json();

    console.log(JSON.stringify(ratingOverview));

    res.json({productDetails, ratingOverview});

  } catch (error) {
    console.error('Error fetching data from backend:', error);
    res.status(500).json({ error: 'Internal Server Error' });
  }
});

router.get('/suggest/:name', async (req, res)=>{
  try {
    // console.log("suggest");
    const queryParts = req.url.split("/");
    const text = queryParts[2];

    const response = await fetch(`${Backend_Url}/product/suggest?name=${text}`);

    if (!response.ok) {
      throw new Error('Network response was not ok ' + response.statusText);
    }
    const data = await response.json();
    res.json(data);
  } catch (error) {
    console.error('Suggest: Error fetching data from backend:', error);
    res.status(500).json({ error: 'Internal Server Error' });
  }
});

router.get(`/search/:text/:page`, async (req, res)=>{
  const queryParts = req.url.split("/");
  const searchText = queryParts[2];
  const page = queryParts[3];
  // console.log("search");
  try {
    // console.log(`front end url: ${req.url}`);
    // console.log(`fetch url: ${Backend_Url}/product/search?name=${searchText}&page=${page}`);
    const response = await fetch(`${Backend_Url}/product/search?name=${searchText}&page=${page}`);
    if (response.status === 404) {
      res.redirect('/404.html');
    }
    else
    if (!response.ok) {
      throw new Error('Network response was not ok ' + response.statusText);
    }
    else
    {
      const data = await response.json();
      res.json(data);
    }
  } catch (error) {
    console.error('Search: Error fetching data:', error);
    res.status(500).json({ error: 'Failed to fetch data from the real server' });
  }
});

router.get('/category-filter/:category/pg:page', async (req, res) => {

  // console.log('filter search');

  // Parse page (it's the number after 'p', e.g., '0' for first page)
  const page = parseInt(req.params.page, 10);
  if (isNaN(page)) {
    return res.status(400).json({ error: 'Invalid page parameter' });
  }

  // Parse category
  const category = decodeURIComponent(req.params.category);

  // console.log('category', category);

  // Parse filter query params (short keys: p, m, r, a*)
  const filters = req.query;

  // console.log(filters);

  let minPrice = 0;
  let maxPrice = Infinity;  // Or some default max


  if (filters.pr) {

    // console.log(filters.pr);

    const priceRange = filters.pr.split('-');
    minPrice = parseInt(priceRange[0], 10) || 0;
    maxPrice = parseInt(priceRange[1], 10) || Infinity;
    // console.log( "PR: " + priceRange );
  }

  // console.log("price range:" + minPrice +" - "+ maxPrice);

  const manufacturers = filters.m ? filters.m.split(',').map(decodeURIComponent) : [];

  // console.log(manufacturers);

  const rating = filters.r ? filters.r: null;  // Assuming ratings are numbers

  // console.log("ratings: "+ ratings);

  const attributes = {};
  Object.keys(filters).forEach(key => {
    if (key.startsWith('a')) {
      const nameId = key.slice(1);  // e.g., '1' for 'a1'
      attributes[nameId] = filters[key].split(',').map(decodeURIComponent);
    }
  });

  // console.log("attributes: " + JSON.stringify(attributes));

  // Construct request body for backend (adjust keys as needed for your backend API)
  const requestBody = {
    filter_attributes: attributes,
    product_category: category,
    price_lowest: minPrice,
    price_highest: maxPrice,
    manufacturer_names: manufacturers,
    rating: rating,

  };

  // console.log("request body: " + JSON.stringify(requestBody));

  try {
    const response = await fetch(`${Backend_Url}/product/filter/${page}`, {
      method: 'POST',
      body: JSON.stringify(requestBody),
      headers: { 'Content-Type': 'application/json' }
    });

    if (!response.ok) {
      const text = await response.text();
      console.error(`Backend error: ${response.status} - ${text}`);
      return res.status(response.status).json({ error: 'Error from backend' });
    }

    const responseData = await response.json();
    res.status(200).json(responseData);
  } catch (error) {
    console.error('Proxy error:', error);
    res.status(502).json({ error: 'Invalid response from backend' });
  }
});

router.post('/getPagedReviews', async (req, res) => {
    // const page = parseInt(req.params.page, 10);
    const productCode = req.body.productCode;
    // const userId = req.body.userId;
    const userId = req.body.userId;
    const page = req.body.page;
    const sort = req.body.sortOrder;
    const verifiedOnly = req.body.verifiedOnly;
    const ratingValue = req.body.ratingValue

    // console.log("Code: " + productCode + " User: " + userId);

    // console.log(JSON.stringify(req.body))

    try {
        const response = await fetch(`${Backend_Url}/product/reviews/paged`,
            {
                method: 'POST',
                body: JSON.stringify({
                    product_code: productCode,
                    user_id: userId,
                    page,
                    sort_order: sort,
                    verified_only: verifiedOnly,
                    rating_value: ratingValue }),
                headers: { 'Content-Type': 'application/json' }
            });

        if (!response.ok) {
            const text = response.text();
            console.error(`Backend error: ${response.status} - ${text}`);
            return res.status(response.status).json({ error: 'Error from backend' });
        }

        const responseData = await response.json();

        console.log(responseData);

        res.status(response.status).json(responseData);
    }
    catch (error) {
        console.error('Reviews: Error fetching data: ', error);
    }
})

router.get(`/getReview/:productCode/:userId`, async (req, res) => {
    const {productCode, userId} = req.params;

    try {
        const response = await fetch(`${Backend_Url}/product/review/specific?userId=${userId}&productCode=${productCode}`);

        // console.log("STATUS: " + response.status);

        const responseData = await response.json();
        // console.log("STATUS: " + response.status + " DATA: " + JSON.stringify(responseData));
       return res.status(response.status).json(responseData);
    }
    catch (error) {
        console.error('Reviews: Error fetching specific review data: ', error);
    }
})

router.post(`/addReview`, async (req, res) => {
    const userId = req.body.userId;
    const productCode = req.body.productCode;
    const rating = req.body.rating;
    const reviewText = req.body.reviewText;

    console.log( "User: " + userId + " Product: " + productCode + " Rating: " + rating + " Review: " + reviewText)

    console.log(JSON.stringify(req.body))
    try {
        const response = await fetch(`${Backend_Url}/product/review/add`,
            {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    user_id: userId,
                    product_code: productCode,
                    rating: rating,
                    review_text: reviewText}),
            }
        )
        return res.status(response.status).json(response.statusText);
    }
    catch (error) {
        console.error('Error creating/updating the review ', error);
    }
})



module.exports = router
