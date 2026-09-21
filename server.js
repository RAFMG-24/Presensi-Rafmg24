const http = require('http');
const fs = require('fs');
const path = require('path');

const PORT = process.env.APP_PORT || 3000;
const PUBLIC_DIR = path.join(__dirname, 'public');
const INDEX_HTML = path.join(PUBLIC_DIR, 'index.html');

const MIME_TYPES = {
  '.html': 'text/html; charset=utf-8',
  '.js': 'text/javascript; charset=utf-8',
  '.css': 'text/css; charset=utf-8',
  '.json': 'application/json; charset=utf-8',
  '.png': 'image/png',
  '.jpg': 'image/jpeg',
  '.jpeg': 'image/jpeg',
  '.gif': 'image/gif',
  '.svg': 'image/svg+xml',
  '.ico': 'image/x-icon',
  '.woff': 'font/woff',
  '.woff2': 'font/woff2',
  '.ttf': 'font/ttf'
};

const server = http.createServer((req, res) => {
  const parsedUrl = new URL(req.url, `http://${req.headers.host || 'localhost'}`);
  let pathname = decodeURIComponent(parsedUrl.pathname);

  // Health check for platform
  if (pathname === '/health' || pathname === '/__health') {
    res.writeHead(200, { 'Content-Type': 'application/json' });
    res.end(JSON.stringify({ status: 'healthy', app: 'Presensi Damkar Subang' }));
    return;
  }

  // Normalize path
  if (pathname === '/' || pathname === '' || pathname === '/index.html' || pathname === '/admin' || pathname === '/admin/' || pathname === '/dashboard' || pathname === '/admin-dashboard' || pathname === '/superadmin' || pathname === '/admin_dashboard.html') {
    if (fs.existsSync(INDEX_HTML)) {
      res.writeHead(200, {
        'Content-Type': 'text/html; charset=utf-8',
        'Cache-Control': 'no-cache'
      });
      fs.createReadStream(INDEX_HTML).pipe(res);
      return;
    }
  }

  // Check in public/
  let safePath = path.normalize(pathname).replace(/^(\.\.[\/\\])+/, '');
  let filePath = path.join(PUBLIC_DIR, safePath);

  if (fs.existsSync(filePath) && fs.statSync(filePath).isFile()) {
    const ext = path.extname(filePath).toLowerCase();
    const contentType = MIME_TYPES[ext] || 'application/octet-stream';
    res.writeHead(200, { 'Content-Type': contentType });
    fs.createReadStream(filePath).pipe(res);
    return;
  }

  // Check in root directory
  let rootFilePath = path.join(__dirname, safePath);
  if (fs.existsSync(rootFilePath) && fs.statSync(rootFilePath).isFile()) {
    const ext = path.extname(rootFilePath).toLowerCase();
    const contentType = MIME_TYPES[ext] || 'application/octet-stream';
    res.writeHead(200, { 'Content-Type': contentType });
    fs.createReadStream(rootFilePath).pipe(res);
    return;
  }

  // SPA fallback to index.html for all other routes
  if (fs.existsSync(INDEX_HTML)) {
    res.writeHead(200, {
      'Content-Type': 'text/html; charset=utf-8',
      'Cache-Control': 'no-cache'
    });
    fs.createReadStream(INDEX_HTML).pipe(res);
  } else {
    res.writeHead(404, { 'Content-Type': 'text/plain; charset=utf-8' });
    res.end('Web Dashboard Superadmin - Presensi Damkar Subang: index.html not found');
  }
});

process.on('uncaughtException', (err) => {
  console.error('[Damkar Server] Uncaught exception:', err.message);
});

process.on('unhandledRejection', (reason, promise) => {
  console.error('[Damkar Server] Unhandled rejection at:', promise, 'reason:', reason);
});

server.on('error', (err) => {
  console.error('[Damkar Server] Server error:', err.message);
});

server.listen(PORT, '0.0.0.0', () => {
  console.log(`[Damkar Subang Web Dashboard] Server running on http://0.0.0.0:${PORT}`);
});
