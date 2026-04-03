import express from  'express';
const router = express.Router();
import { Backend_Url } from './config.js';
import axiosBackendClient from '../axiosBackendClient.js';

router.get('/names', async (req, res)=> {
  try {
      const sessionId = req.cookies.session_id;
      const response = await axiosBackendClient.get(`${Backend_Url}/category/names`, {
          headers: {
              ...(sessionId && {'X-Session-Id': sessionId})
          }
      });
      const responseData = response.data;

      return res.status(response.status).json(responseData || {})

  } catch (error) {
    console.error('-------------------Error fetching category names data-------------------\n', error);
    return res.status(500).end();
  }
});

export default router;
