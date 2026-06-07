export function sessionHeaderBuilder(res, sessionId, ttl) {
    if (res) {
        res.setHeader('x-session-id', sessionId);
        res.setHeader('x-session-ttl', ttl);
    }
}

export function deleteSessionHeaders(res) {
   if (res)
    {
        res.removeHeader('x-session-id');
        res.removeHeader('x-session-ttl');
    }
}
