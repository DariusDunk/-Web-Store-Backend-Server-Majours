export default function getExtractedExpiration(accessToken) {
    try {
        // Grab the middle section (payload), convert base64 to string, and parse JSON
        const payloadBase64 = accessToken.split('.')[1];
        const payloadJson = JSON.parse(Buffer.from(payloadBase64, 'base64').toString());

        // The 'exp' claim is a Unix timestamp in SECONDS. Convert to milliseconds.
        return payloadJson.exp * 1000;
        // return payloadJson.exp;
    } catch (error) {
        return 0; // If token is malformed, treat as expired
    }
}
export const TOKEN_REFRESH_BUFFER_MS = 60 * 1000;

// When saving to your cache:
// sessionCache.set(sessionId, {
//     accessToken: tokenString,
//     expiresAt: getExtractedExpiration(tokenString)
// });