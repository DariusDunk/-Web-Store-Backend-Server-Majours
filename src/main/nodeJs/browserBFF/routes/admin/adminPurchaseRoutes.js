import express from 'express';

const router = express.Router();
import {Backend_Url, WEB_CLIENT_NAME} from '../config.js';
import axiosBackendClient from '../../axiosBackendClient.js';
import {fetchWithSessionTokens} from "../../services/requestTokenManager.js";

const PURCHASE_CONTROLLER_ROUTE = `${Backend_Url}/admin/purchase`;

router.patch(`/update-status`, async (req, res) => {
    const sessionId = req.cookies.session_id;
    const reqBody = req.body;

    console.log(JSON.stringify(reqBody));

    try {
        const response = await fetchWithSessionTokens(sessionId, async (sessionData) => {
                return await axiosBackendClient.patch(`${PURCHASE_CONTROLLER_ROUTE}/purchase-action`, reqBody, {
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

        return res.status(response.status).end();

    } catch (error) {
        console.error('-------------------Error updating purchase status-------------------\n', error);
        if (error.response)
            return res.status(error.response.status || 500).json(error.response.data);
        return res.status(500).end();
    }
});

router.get(`/pending-refund-count`, async (req, res) => {
    const sessionId = req.cookies.session_id;
    try {
        const response = await fetchWithSessionTokens(sessionId, async (sessionData) => {
            return await axiosBackendClient.get(`${PURCHASE_CONTROLLER_ROUTE}/pending-refund-count`, {
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
        console.error('-------------------Error fetching pending refund count-------------------\n', error);
        if (error.response)
            return res.status(error.response.status || 500).json(error.response.data);
        return res.status(500).end();
    }
})

router.get(`/get-all-purchases/:page`, async (req, res) => {
    const sessionId = req.cookies.session_id;
    const {page} = req.params;
    try {
        const response = await fetchWithSessionTokens(sessionId, async (sessionData) => {
            return await axiosBackendClient.get(`${PURCHASE_CONTROLLER_ROUTE}/all/${page}`, {
                headers:
                    {
                        'Content-Type': 'application/json',
                        'x-client_type': WEB_CLIENT_NAME,
                        ...(!sessionData?.is_guest && {'Authorization': 'Bearer ' + sessionData?.access_token}),
                        ...(sessionData?.session_id && {'x-session-id': sessionData?.session_id}),
                    },
                bffContext: {
                    req, res
                },

            });
        },
            {req, res});

        const responseData = response.data;
        return res.status(response.status).json(responseData || {});
    }
    catch (error) {
        console.error('-------------------Error fetching all purchases-------------------\n', error);
        if (error.response)
            return res.status(error.response.status || 500).json(error.response.data);
        return res.status(500).end();
    }
})

export default router;