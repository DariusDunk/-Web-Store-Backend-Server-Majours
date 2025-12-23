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

router.post('/removeFromCart',async  (req, res) =>{
  try{
    const {customerId, productCode} = req.body;
    const response = await fetch(`${Backend_Url}/customer/cart/remove`,{
      method: 'DELETE',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({customerId: customerId, productCode: productCode})
    });
    res.status(response.status).end();
  }
  catch (error) {
    console.error('Error removing product from cart:', error);
    res.status(500).json({ error: 'Internal server error' });
  }
})

router.post(`/removeFromCart/batch`, async (req, res) =>{
  try{
    const {customerId, productCodes} = req.body;

    const response = await fetch(`${Backend_Url}/customer/cart/remove/batch`,{
      method: 'DELETE',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({customer_id: customerId, product_codes: productCodes})
    });

    res.status(response.status).end();

  }
  catch (error) {
    console.error('Error removing product from cart:', error);
    res.status(500).json({ error: 'Node server error' });
  }
})

router.post(`/keyk/register`, async (req, res) => {

  const {name, familyName, email, password} = req.body;

  // console.log("Node register" +
  //   "\nName: " + name +
  //   "\nFamily name: " + familyName +
  //   "\nEmail: " + email +
  //   "\nPassword: " + password)


  try{
    const response = await fetch(`${Backend_Url}/customer/register/customer`,{
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({first_name: name, last_name: familyName, email: email, password: password})
    });
    res.status(response.status).end();
  }
  catch (error) {
    console.error('Error with registration: ', error);
    res.status(500).json({ error: 'Internal server error' });
  }
})

router.post(`/keyk/login`, async (req, res) => {
  const {email, password} = req.body;

  // console.log("Node login: " + email + " " + password)

  const response = await fetch(`${Backend_Url}/customer/login/customer`,{
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({identifier: email, password: password})
  })

  // console.log("Node response: " + response.status)

  if (response.status !== 200)
  {
      return res.status(response.status).end();
  }

  const responseData = await response.json();
  const { access_token, refresh_token, expires_in, refresh_expires_in } = responseData;//TODO tuk zameni refresh_token sys session_id

  // console.log("Node response: " + JSON.stringify(responseData))

  res.cookie('access_token', access_token,
      {
        maxAge: expires_in * 1000,
        secure: false,
        path: '/',
        sameSite: 'lax',
        httpOnly: true
      })

  res.cookie('refresh_token', refresh_token,
      {
        maxAge: refresh_expires_in * 1000,
        secure: false,
        path: '/auth',
        sameSite: 'lax',
        httpOnly: true });

  // res.cookie('session_id', refresh_token,
  //     {
  //       maxAge: refresh_expires_in * 1000,
  //       secure: false,
  //       path: '/refresh',
  //       sameSite: 'lax',
  //       httpOnly: true });

  const userDataResponse = await fetch(`${Backend_Url}/customer/me`,
      {
        method: 'GET',
        headers: {
          Authorization: `Bearer ${access_token}`
        }
      });

  if (userDataResponse.status !== 200)
  {
    // console.log("Error fetching user data: " + userDataResponse.status);

    res.status(userDataResponse.status).end();
    return
  }

    const userData = await userDataResponse.json();

    // console.log("userData: " + JSON.stringify(userData));

    res.status(userDataResponse.status).json(userData);


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

router.get('/getCart/:id',async (req,res)=>
{
  const {id} = req.params;

  try{
    const response = await fetch(`${Backend_Url}/customer/cart?id=${id}`);
    const responseData = await response.json();
    const status = response.status;
    res.status(status).json(responseData);
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
