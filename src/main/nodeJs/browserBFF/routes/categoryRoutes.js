const express =require( 'express');
const router = express.Router();
const { Backend_Url } = require('./config.js');

router.get('/names', async (req, res)=> {
  console.log("category names");
  try {
    const response = await fetch(`${Backend_Url}/category/names`);
    if (response.status === 404) {
      res.redirect('/404.html');
    } else if (!response.ok) {
      throw new Error('Network response was not ok ' + response.statusText);
    } else {
      const data = await response.json();
      res.json(data);
    }
  } catch (error) {
    console.error('Names: Error fetching data:', error);
    res.status(500).json({error: 'Failed to fetch data from the real server'});
  }

});

module.exports = router
