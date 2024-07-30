const express = require('express');
const bodyParser = require('body-parser');
const mongoose = require('mongoose');
const router = express.Router();
const WebSocket = require('ws');
const userScheme = require('../schemas/User');

mongoose.connect('mongodb://localhost:27017/ffxi');

const wss = new WebSocket.Server({ port: 8081 });

wss.on('connection', function connection(ws) {
  ws.send('{"msg": "Hello! I am the server!"}')

  // Set up a ping interval to keep the connection alive
  const pingInterval = setInterval(() => {
    if (ws.readyState === WebSocket.OPEN) {
      ws.ping();
    }
  }, 30000); // Ping every 30 seconds

  ws.on('close', () => {
    clearInterval(pingInterval);
  });

  ws.on('pong', () => {
    console.log('Received pong from client');
  });
});

const userSchema = new mongoose.Schema(userScheme);

const users = mongoose.model('users', userSchema);

router.use(bodyParser.json());
router.use(bodyParser.urlencoded({ extended: true }));

router.get('/get_user', async (req, res) => {
  const playerName = req.query.playerName.toLowerCase();

  try {
    const user = await users.findOne({ playerName });
    res.send(user);
  } catch (error) {
    console.error(error);
    res.status(500).send('An error occurred while retrieving the user.');
  }
});

router.get('/get_users', async (req, res) => {
  try {
    const allUsers = await users.find({}).sort({ 'playerName': 1 });
    res.send(allUsers);
  } catch (error) {
    console.error(error);
    res.status(500).send('An error occurred while retrieving the users.');
  }
});

router.post('/set_online', async (req, res) => {
  const data = req.body;
  const playerName = data.playerName.toLowerCase();
  const last_online = parseInt(data.lastOnline);

  try {
    await users.findOneAndUpdate(
      { playerName: playerName },
      { $set: { lastOnline: last_online } },
      { upsert: true, new: true }
    );

    wss.clients.forEach(client => {
      if (client.readyState === WebSocket.OPEN) {
        client.send(JSON.stringify({
          playerName: playerName,
          lastOnline: last_online
        }));
      }
    });

    res.send(`Online: OK`);
  } catch (error) {
    console.error(error);
    res.status(500).send('An error occurred while updating the online status.');
  }
});

router.post('/set_jobs', async (req, res) => {

  const data = req.body;
  const main_job = data.mainJob;
  const sub_job = data.subJob;
  const playerName = data.playerName.toLowerCase();;

  try {
    await users.findOneAndUpdate(
      { playerName: playerName },
      { $set: { mainJob: main_job, subJob: sub_job } },
      { upsert: true, new: true }
    );

    wss.clients.forEach(client => {
      if (client.readyState === WebSocket.OPEN) {
        client.send(JSON.stringify({
          playerName: playerName,
          mainJob: main_job,
          subJob: sub_job
        }));
      }
    });

    res.send(`Jobs: OK`);
  } catch (error) {
    console.error(error);
    res.status(500).send('An error occurred while updating the main job.');
  }
});

router.post('/set_gil', async (req, res) => {
  const data = req.body;
  const playerName = data.playerName.toLowerCase();
  const gil = data.gil;

  try {
    await users.findOneAndUpdate
      (
        { playerName: playerName },
        { $set: { gil: gil } },
        { upsert: true, new: true }
      );

    wss.clients.forEach(client => {
      if (client.readyState === WebSocket.OPEN) {
        client.send(JSON.stringify({
          playerName: playerName,
          gil: gil
        }));
      }
    });

    res.send(`Gil: OK`);
  } catch (error) {
    console.error(error);
    res.status(500).send('An error occurred while updating the gil.');
  }
});

router.post('/set_player_status', async (req, res) => {
  const data = req.body;
  const playerName = data.playerName.toLowerCase();
  const status = data.status;

  try {
    await users.findOneAndUpdate
      (
        { playerName: playerName },
        { $set: { status: status } },
        { upsert: true, new: true }
      );

    wss.clients.forEach(client => {
      if (client.readyState === WebSocket.OPEN) {
        client.send(JSON.stringify({
          playerName: playerName,
          status: status
        }));
      }
    });

    res.send(`Status: OK`);
  } catch (error) {
    console.error(error);
    res.status(500).send('An error occurred while updating the status.');
  }
});

router.post('/set_hpp', async (req, res) => {
  const data = req.body;
  const playerName = data.playerName.toLowerCase();
  const hpp = data.hpp;
  const debug = false;

  try {
    await users.findOneAndUpdate(
      { playerName: playerName },
      { $set: { hpp: hpp } },
      { upsert: true, new: true }
    );

    wss.clients.forEach(client => {
      if (client.readyState === WebSocket.OPEN) {
        if (playerName == "piplup" && debug) {
          client.send(JSON.stringify({
            playerName: playerName,
            hpp: 0
          }));
        } else {
          client.send(JSON.stringify({
            playerName: playerName,
            hpp: hpp
          }));
        }

      }
    });

    res.send(`HP: OK`);
  } catch (error) {
    console.error(error);
    res.status(500).send('An error occurred while updating the HP.');
  }
});

router.post('/set_mpp', async (req, res) => {
  const data = req.body;
  const playerName = data.playerName.toLowerCase();
  const mpp = data.mpp;

  try {
    await users.findOneAndUpdate(
      { playerName: playerName },
      { $set: { mpp: mpp } },
      { upsert: true, new: true }
    );

    wss.clients.forEach(client => {
      if (client.readyState === WebSocket.OPEN) {
        client.send(JSON.stringify({
          playerName: playerName,
          mpp: mpp
        }));
      }
    });

    res.send(`MP: OK`);
  } catch (error) {
    console.error(error);
    res.status(500).send('An error occurred while updating the MP.');
  }
});

router.post('/set_tp', async (req, res) => {
  const data = req.body;
  const playerName = data.playerName.toLowerCase();
  const tp = data.tp;

  try {
    await users.findOneAndUpdate
      (
        { playerName: playerName },
        { $set: { tp: tp } },
        { upsert: true, new: true }
      );

    wss.clients.forEach(client => {
      if (client.readyState === WebSocket.OPEN) {
        client.send(JSON.stringify({
          playerName: playerName,
          tp: tp
        }));
      }
    });

    res.send(`TP: OK`);
  } catch (error) {
    console.error(error);
    res.status(500).send('An error occurred while updating the TP.');
  }
});

router.post('/set_stats', async (req, res) => {
  const data = req.body;
  const playerName = data.playerName.toLowerCase();
  const { masterLevel, mainJobLevel, subJobLevel, attack, defense, currentExemplar, requiredExemplar,
    title, nationRank, fireResistance, iceResistance, windResistance, earthResistance, lightningResistance,
    waterResistance, lightResistance, darkResistance,
    baseSTR, baseAGI, baseDEX, baseVIT, baseINT, baseMND, baseCHR, addedSTR, addedAGI, addedDEX, addedVIT, addedINT, addedMND, addedCHR
  } = data;
  const stats = {
    baseSTR, baseAGI, baseDEX, baseVIT, baseINT, baseMND, baseCHR, addedSTR, addedAGI, addedDEX, addedVIT, addedINT, addedMND, addedCHR,
    fireResistance, iceResistance, windResistance, earthResistance, lightningResistance, waterResistance, lightResistance, darkResistance
  }

  try {
    await users.findOneAndUpdate(
      { playerName: playerName },
      {
        $set: {
          masterLevel: masterLevel,
          mainJobLevel: mainJobLevel,
          subJobLevel: subJobLevel,
          attack: attack,
          defense: defense,
          stats: stats,
          title: title,
          nationRank: nationRank,
          currentExemplar: currentExemplar,
          requiredExemplar: requiredExemplar,
        }
      },
      { upsert: true, new: true }
    );

    wss.clients.forEach(client => {
      if (client.readyState === WebSocket.OPEN) {
        client.send(JSON.stringify({
          playerName: playerName,
          masterLevel: masterLevel,
          mainJobLevel: mainJobLevel,
          subJobLevel: subJobLevel,
          attack: attack,
          defense: defense,
          stats: stats,
          title: title,
          nationRank: nationRank,
          currentExemplar: currentExemplar,
          requiredExemplar: requiredExemplar,
        }));
      }
    });

    res.send(`Stats: OK`);
  } catch (error) {
    console.error(error);
    res.status(500).send('An error occurred while updating the stats.');
  }
});

router.post('/set_currency2', async (req, res) => {
  const data = req.body;
  const playerName = data.playerName.toLowerCase();
  const currency2 = { domainPoints,eschaBeads,eschaSilt,gallantry,gallimaufry,hallmarks,mogSegments,mweyaPlasmCorpuscles,potpourri } = data;

  try {
    await users.findOneAndUpdate(
      { playerName: playerName },
      { $set: { currency2: currency2 } },
      { upsert: true, new: true }
    );
    res.send(`Currency2: OK`);
  } catch (error) {
    console.error(error);
    res.status(500).send('An error occurred while updating the currency2.');
  }

  wss.clients.forEach(client => {
    if (client.readyState === WebSocket.OPEN) {
      client.send(JSON.stringify({
        playerName: playerName,
        currency2: currency2
      }));
    }
  });

});

router.post('/set_buffs', async (req, res) => {
  const data = req.body;
  const playerName = data.playerName.toLowerCase();
  let buffs = data.buffs;

  // Remove the last character if it is a comma
  if (buffs[buffs.length - 1] === ',') {
    buffs = buffs.slice(0, -1);
  }

  try {
    await users.findOneAndUpdate(
      { playerName: playerName },
      { $set: { buffs: buffs } },
      { upsert: true, new: true }
    );

    wss.clients.forEach(client => {
      if (client.readyState === WebSocket.OPEN) {
        client.send(JSON.stringify({
          playerName: playerName,
          buffs: buffs
        }));
      }
    });

    res.send(`Buffs: OK`);
  } catch (error) {
    console.error(error);
    res.status(500).send('An error occurred while updating the buffs.');
  }

});

router.post('/set_ability_recasts', async (req, res) => {
  const { playerName, abilities } = req.body;
  const abilitiesParsed = JSON.parse(abilities) || [];
  // Basic validation
  if (typeof playerName !== 'string' || !Array.isArray(abilitiesParsed)) {
    return res.status(400).send('Invalid input');
  }

  const lowerCasePlayerName = playerName.toLowerCase();

  try {
    const user = await users.findOneAndUpdate(
      { playerName: lowerCasePlayerName },
      { $set: { abilities: abilitiesParsed } },
      { upsert: true, new: true }
    );

    // Optionally, notify clients via WebSocket
    wss.clients.forEach(client => {
      if (client.readyState === WebSocket.OPEN) {
        client.send(JSON.stringify({
          playerName: lowerCasePlayerName,
          abilities: user.abilities
        }));
      }
    });

    res.send(`Abilities updated for ${playerName}`);
  } catch (error) {
    console.error(error);
    res.status(500).send('An error occurred while updating abilities.');
  }
});

router.post('/set_zone', async (req, res) => {
  const data = req.body;
  const playerName = data.playerName.toLowerCase();
  const zone = data.zone;

  try {
    await users.findOneAndUpdate(
      { playerName: playerName },
      { $set: { zone: zone } },
      { upsert: true, new: true }
    );

    wss.clients.forEach(client => {
      if (client.readyState === WebSocket.OPEN) {
        client.send(JSON.stringify({
          playerName: playerName,
          zone: zone
        }));
      }
    });

    res.send(`Zone: OK`);
  } catch (error) {
    console.error(error);
    res.status(500).send('An error occurred while updating the zone.');
  }
});

module.exports = router;