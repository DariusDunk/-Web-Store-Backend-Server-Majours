import express from 'express';
const router = express.Router();
import {Backend_Url, WEB_CLIENT_NAME} from './config.js';
import {fetchWithSessionTokens} from "../services/requestTokenManager.js";
import axiosBackendClient from '../axiosBackendClient.js';


const timestamp = () => {
  const now = new Date();
  return `[${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')} ${String(now.getHours()).padStart(2, '0')}-${String(now.getMinutes()).padStart(2, '0')}-${String(now.getSeconds()).padStart(2, '0')}]`;
};

const authIntentHeader = 'x-auth-intent';

router.post('/complete', async (req, res)=>{
  try{

    let sessionId = req.cookies.session_id;
    const authIntent = req.get(authIntentHeader);

    if (authIntent && (sessionId === undefined || sessionId === null))
    {
      return res.status(401).json({message: "Unauthorized purchase activity"});
    }

    const response = await fetchWithSessionTokens(sessionId,
        async (sessionData) => {
          return await axiosBackendClient.post(`${Backend_Url}/purchase/complete`, {
            method: 'POST',
            headers: {
              'Content-Type': 'application/json',
              'x-client_type': WEB_CLIENT_NAME,
              ...(!sessionData?.is_guest && {'Authorization': 'Bearer ' + sessionData?.access_token}),
              ...(sessionData?.session_id && {'x-session-id': sessionData?.session_id}),
            },
            body: JSON.stringify(req.body),
            bffContext: {
              req, res
            }
          })
        },
        {req, res});

    const responseData = response.data;

    return res.status(response.status).json(responseData || {});

  }
  catch (error) {
    console.error("error in complete purchase: ", error)

    if (error.response) {
      console.warn(`${timestamp()} Handled backend error for complete purchase request`);
      return res.status(error.response.status || 500).end();
    }

    return res.status(500).json({error: "Invalid response from backend"});
  }
})

export default router
