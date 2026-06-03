import express from 'express';
const router = express.Router();
import {Backend_Url, WEB_CLIENT_NAME} from '../config.js';
import axiosBackendClient from '../../axiosBackendClient.js';
import {fetchWithSessionTokens} from "../../services/requestTokenManager.js";

const CONTROLLER_ROUTE = `${Backend_Url}/admin/session`;

router.post(`/active-sessions/pdf`, async (req, res) => {
    const sessionId = req.cookies.session_id;
    const reqBody = req.body;
    const {startDate, endDate} = reqBody;
    const bodyForRequest = {
        start_date: startDate,
        end_date: endDate
    };

    try
    {
        const response = await fetchWithSessionTokens(sessionId, async (sessionData)=>{
            return await axiosBackendClient.post(`${CONTROLLER_ROUTE}/active/pdf`, bodyForRequest,
                {
                    headers:
                        {
                            'x-client_type': WEB_CLIENT_NAME,
                            ...(!sessionData?.is_guest && {'Authorization': 'Bearer ' + sessionData?.access_token}),
                            ...(sessionData?.session_id && {'x-session-id': sessionData?.session_id}),
                        },
                    bffContext: {
                        req, res
                    },
                    responseType: "stream"
                });
        },
            {req, res});

        res.setHeader("Content-Type", "application/pdf");
        res.setHeader(
            "Content-Disposition",
            `inline; filename=active-sessions-report.pdf`
        );

        response.data.on("error", (err) => {
            console.error("PDF stream error:", err);
            res.status(500).end();
        });

        response.data.pipe(res);

        return;
    }
    catch (error) {
        console.error('-------------------Error fetching active sessions-------------------\n', error);
        if (error.response)
            return res.status(error.response.status || 500).json(error.response.data);
        return res.status(500).end();
    }
})

router.get('/active-sessions', async (req, res) => {
    const sessionId = req.cookies.session_id;

    try{
        const response = await fetchWithSessionTokens(sessionId, async (sessionData) => {
                return await axiosBackendClient.get(`${CONTROLLER_ROUTE}/active/get`, {
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
        console.error('-------------------Error fetching active sessions-------------------\n', error);
        if (error.response)
            return res.status(error.response.status || 500).json(error.response.data);
        return res.status(500).end();

    }
})

export default router;