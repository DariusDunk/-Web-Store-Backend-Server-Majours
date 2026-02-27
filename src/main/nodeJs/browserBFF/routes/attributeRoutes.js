const express =require( 'express');
const router = express.Router();
const { Backend_Url } = require('./config.js');

router.get('/getFilters/:categoryName', async (req, res)=>{

  const queryParts = req.url.split("/");
  const categoryName = queryParts[2];
  // console.log(`category name: ${categoryName}`)
  try
  {
    const response = await fetch(`${Backend_Url}/category/filters?categoryName=${categoryName}`,
      {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(res.body)
      });

    // const text = await response.text();

    const responseData = await response.json();

    // let responseData = null

    if (!response.ok) {
      return res.status(response.status).end();
    }

    // console.log("responseData: " + JSON.stringify(responseData))

    return res.status(response.status).json(responseData || {})
  }catch (error) {
    console.error('Error:', error);
    return res.status(500).json({ error: 'Internal server error' });
  }

})

module.exports = router
