const express =require( 'express');
const router = express.Router();
const { Backend_Url } = require('./config.js');
const {get} = require("axios");

router.get('/names', async (req, res)=> {
  try {
      const response = await get(`${Backend_Url}/category/names`);
      const responseData = response.data;

      return res.status(response.status).json(responseData || {})

  } catch (error) {
    console.error('-------------------Error fetching category names data-------------------\n', error);
    return res.status(500).end();
  }
});

module.exports = router
