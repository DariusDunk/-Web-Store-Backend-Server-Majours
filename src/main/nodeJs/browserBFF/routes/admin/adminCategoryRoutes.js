import express from 'express';
const router = express.Router();
import {Backend_Url, WEB_CLIENT_NAME} from '../config.js';
import axiosBackendClient from '../../axiosBackendClient.js';
import {fetchWithSessionTokens} from "../../services/requestTokenManager.js";

router.get(`/detailed/:id`, async (req, res) => {
    const sessionId = req.cookies.session_id;
    const {id} = req.params;

    try{

        const response = await fetchWithSessionTokens(sessionId, async (sessionData) => {
            return await axiosBackendClient.get(`${Backend_Url}/admin/category/detailed/${id}`, {
                headers:
                    {
                        'Content-Type': 'application/json',
                        'x-client_type': WEB_CLIENT_NAME,
                        ...(!sessionData?.is_guest && {'Authorization': 'Bearer ' + sessionData?.access_token}),
                        ...(sessionData?.session_id && {'x-session-id': sessionData?.session_id}),
                    },
                bffContext: {
                    req, res
                }
            });
        },
            {req, res});

        const responseData = response.data;
        return res.status(response.status).json(responseData || {});

    }
    catch (error) {
        console.error('-------------------Error fetching detailed category-------------------\n', error);
        if (error.response)
            return res.status(error.response.status || 500).json(error.response.data);
        return res.status(500).end();
    }
})

router.get('/all/compact', async (req, res) => {
    const sessionId = req.cookies.session_id;
    try {
        const response = await fetchWithSessionTokens(sessionId, async (sessionData) => {
            return await axiosBackendClient.get(`${Backend_Url}/admin/category/all/compact`, {
                headers:
                    {
                        'Content-Type': 'application/json',
                        'x-client_type': WEB_CLIENT_NAME,
                        ...(!sessionData?.is_guest && {'Authorization': 'Bearer ' + sessionData?.access_token}),
                        ...(sessionData?.session_id && {'x-session-id': sessionData?.session_id}),
                    },
                bffContext: {
                    req, res
                }
            })
        },
            {req, res});

        const responseData = response.data;
        return res.status(response.status).json(responseData || {});

    } catch (error)
        {
        console.error('-------------------Error fetching all categories-------------------\n', error);
        if (error.response)
            return res.status(error.response.status || 500).json(error.response.data);
        return res.status( 500).end();
        }
})

export default router;