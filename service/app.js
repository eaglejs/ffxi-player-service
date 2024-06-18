const express = require('express');
const cors = require('cors');
const routes = require('./routes');
const app = express();
const port = process.env.PORT || 8080;

app.use(cors());

app.use('/', routes);

app.listen(port, () => {
  console.log(`Server is running on port ${port}`);
});