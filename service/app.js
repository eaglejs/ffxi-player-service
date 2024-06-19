const express = require('express');
const cors = require('cors');
const routes = require('./routes');
const app = express();
const ipfilter = require('express-ipfilter').IpFilter;
const ips = ['192.168.0.0/16', '::ffff:192.168.0.0/112'];
const protectedRoutes = require('./routes/protected-routes');

const port = process.env.PORT || 8080;

app.use(cors());

for (const protectedRoute of protectedRoutes) {
  app.use(protectedRoute, ipfilter(ips, { mode: 'allow', logLevel: 'deny'}));
}

app.use('/', routes);

app.listen(port, () => {
  console.log(`Server is running on port ${port}`);
});