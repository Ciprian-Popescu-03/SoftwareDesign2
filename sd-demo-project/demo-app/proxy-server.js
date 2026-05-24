const http = require('http');
const httpProxy = require('http-proxy');

const proxy = httpProxy.createProxyServer({});

const server = http.createServer((req, res) => {
  res.setHeader('X-Content-Type-Options', 'nosniff');
  res.setHeader('X-Frame-Options', 'DENY');
  res.setHeader('Content-Security-Policy',
    "default-src 'self'; " +
    "script-src 'self'; " +
    "style-src 'self' 'unsafe-inline' https://fonts.googleapis.com; " +
    "img-src 'self' data:; " +
    "font-src 'self' https://fonts.gstatic.com; " +
    "connect-src 'self' http://localhost:8081; " +
    "frame-ancestors 'none'; " +
    "form-action 'self'"
  );

  proxy.web(req, res, { target: 'http://localhost:4200' });
});

server.listen(4201, () => {
  console.log('Proxy running on http://localhost:4201');
});
