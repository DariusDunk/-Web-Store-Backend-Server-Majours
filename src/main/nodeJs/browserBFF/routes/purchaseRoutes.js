const express =require( 'express');
const router = express.Router();
const { Backend_Url } = require('./config.js');

router.post('/complete', async (req, res)=>{
  try{

    console.log("inside purchase complete");
    const response = await fetch(`${Backend_Url}/purchase/complete`,{
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(req.body)
    });
    // const responseData = await response.json();
    const text = await response.text()
    // res.json(responseData);

    let responseData = null

    if (text) {
      try {
        responseData = JSON.parse(text);
      } catch (parseError)
      {
        console.error("INVALID JSON FROM BACKEND: " + text)
        return res.status(502).json({error: "Invalid response from backend"})
      }
    }

    res.status(response.status).json(responseData|| {})
  }
  catch (error) {
    console.error('Error:', error);
    res.status(500).json({ error: 'Internal server error' });
  }
})

module.exports = router
