const axios = require('axios');
const { Backend_Url } = require('../browserBFF/routes/config.js');

const backendClient = axios.create({ baseURL: Backend_Url });

backendClient.interceptors.response.use(
    response => response,
    async error => {
        const originalRequest = error.config;
        if (error.response?.status !== 401 || originalRequest._retry) {
            return Promise.reject(error);
        }

        originalRequest._retry = true;

        // Call refresh endpoint
        await axios.post(`${Backend_Url}/refresh/update`, null, {
            headers: { cookie: originalRequest.headers.cookie }
        });

        return backendClient(originalRequest); // retry automatically
    }
);

module.exports = backendClient;
