const express =require( 'express');
const router = express.Router();
const { Backend_Url } = require('./config.js');

router.get('/getFavourites/:page', async (req, res)=>{
  const page = req.params.page
  const accessToken = req.cookies['access_token'];

  try{
    const response = await fetch(`${Backend_Url}/customer/favourites/p/${page}`,
        {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': 'Bearer ' + accessToken,
        }
        });

    if (!response.ok) {
      return res.status(response.status).end();
    }

    const responseData = await response.json();
    return res.status(response.status).json(responseData);
  }

  catch (error) {
    console.error('Error fetching favourites: ', error);
    return res.status(500).json({ error: error.message });
  }
})

router.post(`/addFavourite/:productCode`, async (req, res)=>{

  const accessToken = req.cookies['access_token'];
  const productCode = req.params.productCode;

  try{
    const response = await fetch(`${Backend_Url}/customer/favorite/add?productCode=${productCode}`,{
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer ' + accessToken,
      },
      body: JSON.stringify(req.body)
    });

    if (!response.ok) {
      return res.status(response.status).end();
    }

    return res.status(response.status).end();
  }
  catch (error) {
    console.error('Error:', error);
    return res.status(500).json({ error: error.message });
  }
});

router.post(`/removeFav/single`, async (req, res)=>{

  const accessToken = req.cookies['access_token'];

  const {productCode, currentPage} = req.body;

  const requestBody = {product_code: productCode, current_page: currentPage};

  try {
    const response = await fetch(`${Backend_Url}/customer/favorite/remove/single`,{
      method: 'DELETE',
      headers: {'Content-Type': 'application/json',
      'Authorization': 'Bearer ' + accessToken},
      body: JSON.stringify(requestBody)
    });

    const responseData = await response.json();

    return res.status(response.status).json(responseData);
  } catch (error)
  {
    console.error('Error:', error);
    return res.status(500).json({ error: error.message });
  }
})

router.post(`/removeFav/detProd`, async (req, res)=>{

  const accessToken = req.cookies['access_token'];

  try {
    const response = await fetch(`${Backend_Url}/customer/favorite/remove`,{//todo dovur6i i syzdai v bekenda endpoint
      method: 'DELETE',
      headers: {'Content-Type': 'application/json',
        'Authorization': 'Bearer ' + accessToken},
      body: JSON.stringify(req.body)
    });

    return res.status(response.status).end();
  } catch (error)
  {
    console.error('Error:', error);
    return res.status(500).json({ error: error.message });
  }
})

router.post(`/removeFav/batch`, async (req, res)=>{
  try {
    const accessToken = req.cookies['access_token'];
    const {currentPage, productCodes} = req.body;

    const response = await fetch(`${Backend_Url}/customer/favorite/remove/batch`,{
      method: 'DELETE',
      headers: {'Content-Type': 'application/json',
      'Authorization': 'Bearer ' + accessToken},
      body: JSON.stringify({current_page: currentPage, product_codes: productCodes})
    });

    const responseData = await response.json();
    return res.status(response.status).json(responseData);
  } catch (error)
  {
    console.error('Error:', error);
    return res.status(500).json({ error: error.message });
  }
})

router.post('/addToCart',async  (req, res) =>{
  try{

    const {customerProductPairRequest,doIncrement} = req.body.data;

    // console.log(req.body);
    //
    // console.log("inside addtocart. PAIR: " + JSON.stringify(customerProductPairRequest) + "");
    // console.log("inside addtocart. QUANTITY: " + JSON.stringify(quantity) + "");

    const response = await fetch(`${Backend_Url}/customer/cart/add`,{
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({customerProductPairRequest: customerProductPairRequest, doIncrement: doIncrement})
    });

    if (!response.ok) {
      return res.status(response.status).end();
    }

    if (response.status === 200 || response.status === 201) {
      return res.status(response.status).json({message: await response.text()});
    }
    else
      return res.status(response.status).json(await response.json());
  }
  catch (error) {
    console.error('Error:', error);
    return res.status(500).json({ error: error.message });
  }
});

router.post('/addToCart/batch',async  (req, res) =>{
  try{
    const {customerId, productCodes} = req.body;
    const response = await fetch(`${Backend_Url}/customer/cart/add/batch`,{
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({customer_id: customerId, product_codes: productCodes})
    });

    if (!response.ok) {
      return res.status(response.status).end();
    }

    if (response.status === 200) {
      return res.status(response.status).json({message: await response.text()});
    }
    else
      return res.status(response.status).json(await response.json());
  }
  catch (error) {
    console.error('Error batch adding products to cart: ', error);
    return res.status(500).json({ error: error.message });
  }
})

router.post('/removeFromCart/:productCode',async  (req, res) =>{
  try{
    // const {customerId, productCode} = req.body;

    const productCode = req.params.productCode;
    const accessToken = req.cookies['access_token'];
    const response = await fetch(`${Backend_Url}/customer/cart/remove/${productCode}`,{
      method: 'DELETE',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer ' + accessToken
      }
    });

    if (!response.ok) {
      return res.status(response.status).end();
    }

    const responseData = await response.json();

    return res.status(response.status).json(responseData);
  }
  catch (error) {
    console.error('Error removing product from cart:', error);
    return res.status(500).json({ error: error.message });
  }
})

router.post(`/removeFromCart/batch/turbo`, async (req, res) =>{
  try{
    // throw new Error("THIS SHOULD CRASH EVERYTHING");
    const accessToken = req.cookies['access_token'];

    const productCodes = req.body;

    const response = await fetch(`${Backend_Url}/customer/cart/remove/batch`,{
      method: 'DELETE',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer ' + accessToken
      },
      body: JSON.stringify(productCodes)
    });

    if (!response.ok) {
      return res.status(response.status).end();
    }

    const responseData = await response.json();


    return res.status(response.status).json(responseData);
    // return res.status(409).end();
  }
  catch (error) {
    console.error('Error removing product from cart:', error);
    return res.status(500).json({ error: error.message });
  }
})




router.get('/getCart/:id',async (req,res)=>
{
  const {id} = req.params;

  try{
    const response = await fetch(`${Backend_Url}/customer/cart?id=${id}`);

    if (!response.ok) {
      return res.status(response.status).end();
    }


    const responseData = await response.json();
    const status = response.status;
    return res.status(status).json(responseData);
  }
  catch (error) {
    console.error('Error:', error);
   return res.status(500).json({ error: error.message });
  }
});

router.get('/getUserPfp/', async (req, res) =>
  {
    // const userId = req.url.split("/")[2];
    const accessToken = req.cookies['access_token'];

    try{
      const response = await fetch(`${Backend_Url}/customer/getPfp`,
          {
          method: 'GET',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': 'Bearer ' + accessToken,
          }
          });

      if (!response.ok) {
        return res.status(response.status).end();
      }

      const responseText = await response.text();

      // console.log("RESPONSE: " + responseText);
      return res.status(response.status).send(responseText);
    }
    catch (error) {
      console.error('Error:', error);
      return res.status(500).json({ error: error.message });
    }
  }
)

router.get('/me', async (req, res) =>
  {
    const accessToken = req.cookies['access_token'];
    try{
      const response = await fetch(`${Backend_Url}/customer/me`,
          {
          method: 'GET',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': 'Bearer ' + accessToken,
          }
          }
      )

      if (!response.ok) {
        return res.status(response.status).end();
      }

      const responseData = await response.json();
      // console.log("/Me refetch response: " + JSON.stringify(responseData));
      return res.status(response.status).json(responseData);

    }catch (error) {
      console.error('Error:', error);
      return res.status(500).json({ error: error.message });
    }
  })

module.exports = router
