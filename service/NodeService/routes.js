const express = require('express');
const router = express.Router();

const Health = require('./routes/Health.js');
const Player = require('./routes/Player.js');

router.use('/', Health);
router.use('/', Player);

module.exports = router;