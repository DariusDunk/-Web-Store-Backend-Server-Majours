const express =require( 'express');
const router = express.Router();
const { Backend_Url } = require('./config.js');

router.post(`/addfavourite`, async (req, res)=>{
  try{
    const response = await fetch(`${Backend_Url}/customer/addfavourite`,{
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(req.body)
    });
    const responseData = await response.text();
    res.json(responseData);
  }
  catch (error) {
    console.error('Error:', error);
    res.status(500).json({ error: 'Internal server error' });
  }
});

router.delete(`/removefav`, async (req, res)=>{
  try {
    const response = await fetch(`${Backend_Url}/customer/removefav`,{
      method: 'DELETE',
      headers: {'Content-Type': 'application/json'},
      body: JSON.stringify(req.body)
    });
    const responseData = await response.text();
    res.json(responseData);
  } catch (error)
  {
    console.error('Error:', error);
    res.status(500).json({ error: 'Internal server error' });
  }
})

router.post('/addtocart',async  (req, res) =>{
  try{
    const response = await fetch(`${Backend_Url}/customer/addtocart`,{
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(req.body)
    });
    const responseData = await response.text();
    res.json(responseData);
  }
  catch (error) {
    console.error('Error:', error);
    res.status(500).json({ error: 'Internal server error' });
  }
});

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
    console.log(`response: status: ${response.status} data: ${responseData}`);
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

      console.log("RESPONSE: " + responseText);
      res.status(response.status).send(responseText);
    }
    catch (error) {
      console.error('Error:', error);
      res.status(500).json({ error: 'Internal server error' });
    }
  }
)

module.exports = router
