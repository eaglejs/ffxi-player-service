const express = require('express');
const http = require('http');
const cors = require('cors');
const routes = require('./routes');
const app = express();
const ipfilter = require('express-ipfilter').IpFilter;
const ips = ['192.168.0.0/16', '::ffff:192.168.0.0/112'];
const protectedRoutes = require('./routes/protected-routes');

const port = process.env.EXPRESS_PORT || 8080;

app.use(cors());

// Request logging middleware
app.use((req, res, next) => {
  console.log(`${new Date().toISOString()} - ${req.method} ${req.url}`);
  next();
});

for (const protectedRoute of protectedRoutes) {
  app.use(protectedRoute, ipfilter(ips, { mode: 'allow', logLevel: 'deny'}));
}

app.use('/', routes);

// Create HTTP server from Express app
const server = http.createServer(app);

// Import WebSocket server from Player router
const Player = require('./routes/Player');
const wss = Player.wss;

// Handle WebSocket upgrade requests
server.on('upgrade', (request, socket, head) => {
  const pathname = request.url;
  
  console.log('WebSocket upgrade request received for:', pathname);
  
  if (pathname === '/ws/players' || pathname.startsWith('/ws/players')) {
    wss.handleUpgrade(request, socket, head, (ws) => {
      console.log('WebSocket client connected successfully');
      wss.emit('connection', ws, request);
    });
  } else {
    console.log('WebSocket upgrade rejected - invalid path:', pathname);
    socket.destroy();
  }
});

server.listen(port, () => {
  console.log(`Server is running on port ${port}`);
  console.log(`WebSocket server is running on the same port`);
});