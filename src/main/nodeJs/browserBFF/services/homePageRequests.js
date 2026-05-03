import axiosBackendClient from "../axiosBackendClient.js";
import {Backend_Url, WEB_CLIENT_NAME} from "../routes/config.js";

export async function getTopProductsOfTopSales(req, res, sessionData) {
    try
    {
        const response = await axiosBackendClient.get(`${Backend_Url}/product/topSalesProducts`,
            {
                headers: {
                    'Content-Type': 'application/json',
                    'x-client_type': WEB_CLIENT_NAME,
                    ...(!sessionData?.is_guest && {'Authorization': 'Bearer ' + sessionData?.access_token}),
                    ...(sessionData?.session_id && {'x-session-id': sessionData?.session_id}),
                },
                bffContext: {
                    req, res
                }
            });

        const responseData = response.data;

        console.log("Top products of top categories: " + JSON.stringify(responseData))

        return responseData;
    }
    catch (error) {
        console.error('-------------------Error getting top products of top sales-------------------\n', error);
        return [];
    }
}

export async function getTopProductsOfTopCategories(req, res, sessionData) {
    try
    {
        const response = await axiosBackendClient.get(`${Backend_Url}/product/topCategoryProducts`,
            {
                headers: {
                    'Content-Type': 'application/json',
                    'x-client_type': WEB_CLIENT_NAME,
                    ...(!sessionData?.is_guest && {'Authorization': 'Bearer ' + sessionData?.access_token}),
                    ...(sessionData?.session_id && {'x-session-id': sessionData?.session_id}),
                },
                bffContext: {
                    req, res
                }
            });

        const responseData = response.data;

        console.log("Top products of top categories: " + JSON.stringify(responseData))

        return responseData;
    }
    catch (error) {
        console.error('-------------------Error getting top products of top categories-------------------\n', error);
        return [];
    }
}