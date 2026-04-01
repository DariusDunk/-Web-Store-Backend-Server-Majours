import axios from 'axios';
import { Backend_Url } from './routes/config.js';
const axiosBackendClient = axios.create({ baseURL: Backend_Url , withCredentials: true});
import {refreshToken} from './services/axiosInterceptor.js';
import sessionCache from "./services/sessionCache.js";

const refreshPromises = new Map();

axiosBackendClient.interceptors.response.use(
    response => response,
    async error => {
        const originalRequest = error.config;
        if (error.response?.status !== 401) {
            // console.error(error);
            return Promise.reject(error);
        }

        // console.log("Request failed due to unauthorized access.");

        if (!originalRequest?.bffContext) {
            return Promise.reject(error);
        }

        const req = originalRequest?.bffContext?.req;
        const res = originalRequest?.bffContext?.res;
        const sessionId = req?.cookies?.session_id;

        if (!sessionId) {
            return Promise.reject(error);
        }

        if (originalRequest._retry) {
            return Promise.reject(error);
        }

        originalRequest._retry = true;

        if (!refreshPromises.has(sessionId)) {
            // console.log("Attempting to refresh token...");

            const refreshPromise = refreshToken(sessionId, res)
                .catch(err => {
                    console.error("Refresh token failed: " + err);
                    throw err;
                })
                .finally(() => {
                    refreshPromises.delete(sessionId);
                });

            refreshPromises.set(sessionId, refreshPromise);

        } else {
            // console.log("Token refresh already in progress, request paused...");
        }

        return refreshPromises
            .get(sessionId)
            .then(() => {

                const updatedTokens = sessionCache.get(sessionId);

                if (updatedTokens && updatedTokens.access_token) {
                    originalRequest.headers['Authorization'] = 'Bearer ' + updatedTokens.access_token;
                }
              return axiosBackendClient(originalRequest)
            })
            .catch(err => Promise.reject(err));
    }
);

export default axiosBackendClient;


