const express =require( 'express');
const router = express.Router();
const { Backend_Url } = require('./config.js');
const {get} = require("axios");

router.get('/getFilters/:categoryName', async (req, res)=>{

  const categoryName = req.params.categoryName

  try
  {
      const response = await get(`${Backend_Url}/category/filters?categoryName=${categoryName}`, {})
      const responseData = response.data;

    return res.status(response.status).json(responseData || {})
  }catch (error) {
    console.error('-------------------Error getting category filters\n', error);
    return res.status(500).end();
  }
})

module.exports = router
