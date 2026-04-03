import express from 'express';
const router = express.Router();
import { Backend_Url } from'./config.js';
import axiosBackendClient from '../axiosBackendClient.js';

router.get('/getFilters/:categoryName', async (req, res)=>{

  const categoryName = req.params.categoryName
  const sessionId = req.cookies.session_id;

  try
  {
      const response = await axiosBackendClient.get(`${Backend_Url}/category/filters?categoryName=${categoryName}`, {
          headers: {
              ...(sessionId && {'X-Session-Id': sessionId})
          }
      })

    return res.status(response.status).json(responseData || {})
  }catch (error) {
    console.error('-------------------Error getting category filters-------------------\n', error);
    return res.status(500).end();
  }
})

export default router;
