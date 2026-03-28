const axios = require('axios');
const { Backend_Url } = require('../browserBFF/routes/config.js');

const axiosBackendClient = axios.create({ baseURL: Backend_Url , withCredentials: true});

axiosBackendClient.interceptors.response.use(//TODO kogato stigne6 do tuk, vij interceptora ot frontenda, tyi kato tozi tam e po-nova versiq
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

        return axiosBackendClient(originalRequest); // retry automatically
    }
);

module.exports = axiosBackendClient;
