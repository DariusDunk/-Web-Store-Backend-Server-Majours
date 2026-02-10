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
        return res.redirect('/404.html');
    }
      if (!response.ok) {
          return res.status(response.status).end();
      }

      const data = await response.json();
      return res.json(data);
    // }
  } catch (error)
  {
    console.error('Search: Error fetching data:', error);
      return res.status(error.status).json({ error: error.message });
  }
});

router.get('/manufacturer/:manufacturerName/p:page', async (req, res) => {
  const { manufacturerName, page} = req.params;
  // console.log("Manufacturer");
  try {
    const response = await fetch(`${Backend_Url}/product/manufacturer/${manufacturerName}/p${page}`);
    if (response.status === 404) {
        return res.redirect('/404.html');
    }
      if (!response.ok) {
          return res.status(response.status).end();
      }
      const data = await response.json();
      return res.json(data);

  } catch (error) {
    console.error('Manufacturer: Error fetching data:', error);
      return res.status(error.status).json({ error: error.message });
  }
});

router.get('/category/:categoryName/p:page', async (req, res) => {
  const {categoryName, page} = req.params;
  // console.log("category");
  try {
    const response = await fetch(`${Backend_Url}/product/category/${categoryName}/p${page}`);

    if (response.status === 404) {
        return res.redirect('/404.html');
    }
      if (!response.ok) {
          return res.status(response.status).end();
      }

      const data = await response.json();
      return res.json(data);

  } catch (error) {
    console.error('Category: Error fetching data:', error);
      return res.status(error.status).json({ error: error.message });
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
        return res.status(ratingOverviewResponse.status).end();
    }

    if (!productDetailsResponse.ok) {
        return res.status(productDetailsResponse.status).end();
    }

    const productDetails = await productDetailsResponse.json();
    const ratingOverview = await ratingOverviewResponse.json();

    // console.log(JSON.stringify(ratingOverview));

      return res.json({productDetails, ratingOverview});

  } catch (error) {
    console.error('Error fetching data from backend:', error);
      return res.status(error.status).json({ error: error.message });
  }
});

router.get('/suggest/:name', async (req, res)=>{
  try {
    // console.log("suggest");
    const queryParts = req.url.split("/");
    const text = queryParts[2];

    const response = await fetch(`${Backend_Url}/product/suggest?name=${text}`);

      if (!response.ok) {
          return res.status(response.status).end();
      }
    const data = await response.json();
      return json(data);
  } catch (error) {
    console.error('Suggest: Error fetching data from backend:', error);
      return res.status(500).json({ error: 'Internal Server Error' });
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
        return res.redirect('/404.html');
    }

      if (!response.ok) {
          return res.status(response.status).end();
      }

      const data = await response.json();
        return res.json(data);

  } catch (error) {
    console.error('Search: Error fetching data:', error);
      return res.status(500).json({ error: 'Failed to fetch data from the real server' });
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
      console.error(`Product filter error: ${response.status} - ${text}`);
      return res.status(response.status).json({ error: 'Error from backend' });
    }

    const responseData = await response.json();
      return res.status(200).json(responseData);
  } catch (error) {
    console.error('Proxy error:', error);
      return res.status(502).json({ error: 'Invalid response from backend' });
  }
});

router.post('/getPagedReviews', async (req, res) => {
    // const page = parseInt(req.params.page, 10);
    const productCode = req.body.productCode;
    // const userId = req.body.userId;
    const page = req.body.page;
    const sort = req.body.sortOrder;
    const verifiedOnly = req.body.verifiedOnly;
    const ratingValue = req.body.ratingValue
    const accessToken = req.cookies['access_token'];
    // console.log("Token: " + accessToken);

    // console.log("Code: " + productCode + " User: " + userId);

    // console.log(JSON.stringify(req.body))

    try {
        const response = await fetch(`${Backend_Url}/product/reviews/paged`,
            {
                method: 'POST',
                body: JSON.stringify({
                    product_code: productCode,
                    page,
                    sort_order: sort,
                    verified_only: verifiedOnly,
                    rating_value: ratingValue }),
                headers: { 'Content-Type': 'application/json' ,
                    'Authorization': 'Bearer ' + accessToken}
            });

        if (!response.ok) {
            const text = response.text();
            console.error(`Review fetch error: ${response.status} - ${text}`);
            return res.status(response.status).json({ error: 'Error from backend' });
        }

        const responseData = await response.json();

        // console.log(responseData);

        return res.status(response.status).json(responseData);
    }
    catch (error) {
        console.error('Reviews: Error fetching data: ', error);
        return res.status(500).json({error: 'Internal server error'});
    }
})

router.get(`/getReview/:productCode`, async (req, res) => {
    const {productCode} = req.params;
    // const accessToken = req.cookies['access_token'];
    // console.log("Token: " + accessToken);
    const accessToken = req.cookies['access_token'];

    try {
        const response = await fetch(`${Backend_Url}/product/review/specific?productCode=${productCode}`,
            {
                method: 'GET',
                headers: { 'Content-Type': 'application/json' ,
                'Authorization': 'Bearer ' + accessToken
                }
            });

        // console.log("STATUS: " + response.status);
        if (!response.ok) {
            return res.status(response.status).end();
        }
        const responseData = await response.json();
        // console.log("STATUS: " + response.status + " DATA: " + JSON.stringify(responseData));
       return res.status(response.status).json(responseData);
    }
    catch (error) {
        console.error('Reviews: Error fetching specific review data: ', error);
        return res.status(500).json({error: 'Internal server error'});
    }
})

router.post(`/addReview`, async (req, res) => {
    const productCode = req.body.productCode;
    const rating = req.body.rating;
    const reviewText = req.body.reviewText;
    const accessToken = req.cookies['access_token'];

    // console.log( "User: " + userId + " Product: " + productCode + " Rating: " + rating + " Review: " + reviewText)

    // console.log(JSON.stringify(req.body))
    try {
        const response = await fetch(`${Backend_Url}/product/review/add`,
            {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' ,
                'Authorization': 'Bearer ' + accessToken},
                body: JSON.stringify({
                    product_code: productCode,
                    rating: rating,
                    review_text: reviewText}),
            }
        )

        if (!response.ok) {
            return res.status(response.status).end();
        }

        if (response.status === 207)
        {
            const responseData = await response.json();
            // console.log(responseData);
            return res.status(response.status).json(responseData);
        }


        return res.status(response.status).json(response.statusText);
    }
    catch (error) {
        console.error('Error creating/updating the review ', error);
        return res.status(500).json({error: 'Internal server error'});
    }
})

router.post(`/updateReview`, async (req, res) => {
    const accessToken = req.cookies['access_token'];
    const productCode = req.body.productCode;
    const rating = req.body.rating;
    const reviewText = req.body.reviewText;

    // console.log( "INSIDE UPDATE REVIEW: " + "User: " + userId + " Product: " + productCode + " Rating: " + rating + " Review: " + reviewText)

    try{
        const response = await fetch(Backend_Url + "/product/review/update",
            {
                method: 'PATCH',
                headers: { 'Content-Type': 'application/json' ,
                'Authorization': 'Bearer ' + accessToken},
                body: JSON.stringify({
                    product_code: productCode,
                    rating: rating,
                    review_text: reviewText}),
            })

        if (!response.ok) {
            return res.status(response.status).end();
        }

        if (response.status === 207)
        {
            const responseData = await response.json();
            // console.log(responseData);
            return res.status(response.status).json(responseData);
        }

        return res.status(response.status).json(response.statusText);
    }

    catch (error) {
        console.error('Error updating the review ', error);
        return res.status(500).json({error: 'Internal server error'});
    }

})

router.post(`/deleteReview`, async (req, res) => {
    const {productCode} = req.body;
    const accessToken = req.cookies['access_token'];

    // console.log( "INSIDE DELETE REVIEW: " + "Product: " + productCode + " Customer: " + customerId)

    try{
        const response = await fetch(`${Backend_Url}/product/review/delete?product_code=${productCode}`,
            {
                method: 'DELETE',
                headers: {'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + accessToken}
            })

        if (!response.ok) {
            return res.status(response.status).end();
        }

        return res.status(response.status).json(response.statusText);
    }
    catch (error) {
        console.error('Error deleting the review ', error);
        return res.status(500).json({error: 'Internal server error'});
    }
})


module.exports = router
