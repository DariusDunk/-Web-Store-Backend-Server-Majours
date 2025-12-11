const express =require( 'express');
const router = express.Router();
const { Backend_Url } = require('./config.js');

router.get('/getFavourites/:userId/:page', async (req, res)=>{
  const {userId, page} = req.params

  try{
    const response = await fetch(`${Backend_Url}/customer/favourites/${userId}/p/${page}`);
    const responseData = await response.json();
    res.status(response.status).json(responseData);
  }

  catch (error) {
    console.error('Error fetching favourites: ', error);
    res.status(500).json({ error: 'Internal server error' });
  }
})

router.post(`/addFavourite`, async (req, res)=>{
  try{
    const response = await fetch(`${Backend_Url}/customer/favorite/add`,{
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(req.body)
    });
    const responseData = await response.text();
    res.status(response.status).json(responseData);
  }
  catch (error) {
    console.error('Error:', error);
    res.status(500).json({ error: 'Internal server error' });
  }
});

router.post(`/removeFav`, async (req, res)=>{
  try {
    const response = await fetch(`${Backend_Url}/customer/favorite/remove`,{
      method: 'DELETE',
      headers: {'Content-Type': 'application/json'},
      body: JSON.stringify(req.body)
    });
    const responseData = await response.text();
    res.status(response.status).json(responseData);
  } catch (error)
  {
    console.error('Error:', error);
    res.status(500).json({ error: 'Internal server error' });
  }
})

router.post(`/removeFav/batch`, async (req, res)=>{
  try {

    const {customerId, productCodes} = req.body;

    // console.log("inside removeFav/batch. Body: " + JSON.stringify(req.body) + "");

    const response = await fetch(`${Backend_Url}/customer/favorite/remove/batch`,{
      method: 'DELETE',
      headers: {'Content-Type': 'application/json'},
      body: JSON.stringify({customer_id: customerId, product_codes: productCodes})
    });
    const responseData = await response.text();
    res.status(response.status).json(responseData);
  } catch (error)
  {}
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

    if (response.status === 200 || response.status === 201) {
      res.status(response.status).json({message: await response.text()});
    }
    else
      res.status(response.status).json(await response.json());
  }
  catch (error) {
    console.error('Error:', error);
    res.status(500).json({ error: 'Internal server error' });
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

    if (response.status === 200) {
      res.status(response.status).json({message: await response.text()});
    }
    else
      res.status(response.status).json(await response.json());
  }
  catch (error) {
    console.error('Error batch adding products to cart: ', error);
    res.status(500).json({ error: 'Internal server error' });
  }
})

router.post('/removeFromCart',async  (req, res) =>{//TODO testvai
  try{
    const {customerId, productCode} = req.body.data;
    const response = await fetch(`${Backend_Url}/customer/cart/remove`,{
      method: 'DELETE',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({customerId: customerId, productCode: productCode})
    });
    res.json(await response.json());
  }
  catch (error) {
    console.error('Error removing product from cart:', error);
    res.status(500).json({ error: 'Internal server error' });
  }
})

router.post('/registration', async (req, res)=>{
  try{
    const response = await fetch(`${Backend_Url}/customer/registration`,{
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(req.body)
    });
    const responseData = await response.text();
    // console.log(`response: status: ${response.status} data: ${responseData}`);
    res.status(response.status).send(responseData);
  }
  catch (error) {
    console.error('Error:', error);
    res.status(500).json({ error: 'Internal server error' });
  }
});

router.post('/login', async (req, res)=>{
  try{
    const response = await fetch(`${Backend_Url}/customer/login`,{
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(req.body)
    });
    const responseData = await response.text();
    res.status(response.status).send(responseData);
  }
  catch (error) {
    console.error('Error:', error);
    res.status(500).json({ error: 'Internal server error' });
  }
});

router.get('/cart/:id',async (req,res)=>
{
  const queryParts = req.url.split("/");
  const id = queryParts[2];

  try{
    const response = await fetch(`${Backend_Url}/customer/cart?id=${id}`);
    const responseData = await response.json();
    res.json(responseData);
  }
  catch (error) {
    console.error('Error:', error);
    res.status(500).json({ error: 'Internal server error' });
  }
});

router.get('/getUserPfp/:id', async (req, res) =>
  {
    const userId = req.url.split("/")[2];

    try{
      const response = await fetch(`${Backend_Url}/customer/getPfp?id=${userId}`);

      const responseText = await response.text();

      // console.log("RESPONSE: " + responseText);
      res.status(response.status).send(responseText);
    }
    catch (error) {
      console.error('Error:', error);
      res.status(500).json({ error: 'Internal server error' });
    }
  }
)

module.exports = router
