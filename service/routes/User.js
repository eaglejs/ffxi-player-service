const express = require('express');
const bodyParser = require('body-parser');
const mongoose = require('mongoose');
const router = express.Router();
const iconv = require('iconv-lite');
const WebSocket = require('ws');
const userScheme = require('../schemas/User');
const chatScheme = require('../schemas/Chat');
const e = require('express');

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
const chatSchema = new mongoose.Schema(chatScheme);

const users = mongoose.model('users', userSchema);
const chats = mongoose.model('chats', chatSchema);

router.use(bodyParser.json());
router.use(bodyParser.urlencoded({ extended: true }));

router.post('/initialize_user', async (req, res) => {
  try {
    const data = req.body;
    const playerId = parseInt(data.playerId);
    const playerName = data.playerName.toLowerCase();
    const lastOnline = parseInt(data.lastOnline);

    await users.findOneAndUpdate(
      { playerName: playerName },
      { $set: { playerId: playerId, playerName: playerName, lastOnline: lastOnline } },
      { upsert: true, new: true }
    );

    res.send(`User ${playerName} initialized`);
  } catch (error) {
    console.error(error);
    res.status(500).send('An error occurred while initializing the user.');
  }
});

router.get('/get_user', async (req, res) => {
  try {
    const playerId = parseInt(req.query.playerId);
    const user = await users.findOne({ playerId });
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
  try {
    const data = req.body;
    const playerId = parseInt(data.playerId);
    const playerName = data.playerName.toLowerCase();
    const last_online = parseInt(data.lastOnline);

    await users.findOneAndUpdate(
      { playerId: playerId },
      { $set: { playerId: playerId, playerName: playerName, lastOnline: last_online } },
      { upsert: true, new: true }
    );

    wss.clients.forEach(client => {
      if (client.readyState === WebSocket.OPEN) {
        client.send(JSON.stringify({
          playerId: playerId,
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
  try {
    const data = req.body;
    const main_job = data.mainJob;
    const sub_job = data.subJob;
    const playerId = parseInt(data.playerId);
    const playerName = data.playerName.toLowerCase();

    await users.findOneAndUpdate(
      { playerId: playerId },
      { $set: { mainJob: main_job, subJob: sub_job } },
      { upsert: true, new: true }
    );

    wss.clients.forEach(client => {
      if (client.readyState === WebSocket.OPEN) {
        client.send(JSON.stringify({
          playerId: playerId,
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
  try {
    const data = req.body;
    const playerId = parseInt(data.playerId);
    const playerName = data.playerName.toLowerCase();
    const gil = data.gil;

    await users.findOneAndUpdate
      (
        { playerId: playerId },
        { $set: { gil: gil } },
        { upsert: true, new: true }
      );

    wss.clients.forEach(client => {
      if (client.readyState === WebSocket.OPEN) {
        client.send(JSON.stringify({
          playerId: playerId,
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
  try {
    const data = req.body;
    const playerId = parseInt(data.playerId);
    const playerName = data.playerName.toLowerCase();
    const status = data.status;

    await users.findOneAndUpdate
      (
        { playerId: playerId },
        { $set: { status: status } },
        { upsert: true, new: true }
      );

    wss.clients.forEach(client => {
      if (client.readyState === WebSocket.OPEN) {
        client.send(JSON.stringify({
          playerId: playerId,
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
  try {
    const data = req.body;
    const playerId = parseInt(data.playerId);
    const playerName = data.playerName.toLowerCase();
    const hpp = data.hpp;
    const debug = false;

    await users.findOneAndUpdate(
      { playerId: playerId },
      { $set: { hpp: hpp } },
      { upsert: true, new: true }
    );

    wss.clients.forEach(client => {
      if (client.readyState === WebSocket.OPEN) {
        if (playerName == "piplup" && debug) {
          client.send(JSON.stringify({
            playerId: playerId,
            playerName: playerName,
            hpp: 0
          }));
        } else {
          client.send(JSON.stringify({
            playerId: playerId,
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
  try {
    const data = req.body;
    const playerId = parseInt(data.playerId);
    const playerName = data.playerName.toLowerCase();
    const mpp = data.mpp;

    await users.findOneAndUpdate(
      { playerId: playerId },
      { $set: { mpp: mpp } },
      { upsert: true, new: true }
    );

    wss.clients.forEach(client => {
      if (client.readyState === WebSocket.OPEN) {
        client.send(JSON.stringify({
          playerId: playerId,
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
  try {
    const data = req.body;
    const playerId = parseInt(data.playerId);
    const playerName = data.playerName.toLowerCase();
    const tp = data.tp;

    await users.findOneAndUpdate
      (
        { playerId: playerId },
        { $set: { tp: tp } },
        { upsert: true, new: true }
      );

    wss.clients.forEach(client => {
      if (client.readyState === WebSocket.OPEN) {
        client.send(JSON.stringify({
          playerId: playerId,
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
  try {
    const data = req.body;
    const playerId = parseInt(data.playerId);
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

    await users.findOneAndUpdate(
      { playerId: playerId },
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
          playerId: playerId,
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

router.post('/set_currency1', async (req, res) => {
  try {
    const data = req.body;
    const playerId = parseInt(data?.playerId);
    const playerName = data.playerName.toLowerCase();
    const currency1 = {
      conquestPointsBastok,
      conquestPointsSandoria,
      conquestPointsWindurst,
      deeds,
      dominionNotes,
      imperialStanding,
      loginPoints,
      nyzulTokens,
      sparksOfEminence,
      therionIchor,
      unityAccolades,
      voidstones,
    } = data;

    if (playerId === '') {
      return res.status(400).send('Invalid input');
    }

    await users.findOneAndUpdate(
      { playerId: playerId },
      { $set: { currency1: currency1 } },
      { upsert: true, new: true }
    );
    res.send(`Currency1: OK`);
    wss.clients.forEach(client => {
      if (client.readyState === WebSocket.OPEN) {
        client.send(JSON.stringify({
          playerId: playerId,
          playerName: playerName,
          currency1: currency1
        }));
      }
    });
  } catch (error) {
    console.error(error);
    res.status(500).send('An error occurred while updating the currency1.');
  }

});

router.post('/set_currency2', async (req, res) => {
  try {
    const data = req.body;
    const playerId = parseInt(data.playerId);
    const playerName = data.playerName.toLowerCase();
    const currency2 = { domainPoints, eschaBeads, eschaSilt, gallantry, gallimaufry, hallmarks, mogSegments, mweyaPlasmCorpuscles, potpourri, coalitionImprimaturs } = data;

    if (playerId === '') {
      return res.status(400).send('Invalid input');
    }

    await users.findOneAndUpdate(
      { playerId: playerId },
      { $set: { currency2: currency2 } },
      { upsert: true, new: true }
    );
    res.send(`Currency2: OK`);
    wss.clients.forEach(client => {
      if (client.readyState === WebSocket.OPEN) {
        client.send(JSON.stringify({
          playerId: playerId,
          playerName: playerName,
          currency2: currency2
        }));
      }
    });
  } catch (error) {
    console.error(error);
    res.status(500).send('An error occurred while updating the currency2.');
  }

});

router.post('/update_exp_history', async (req, res) => {
  try {
    // (8|253) = exp, (371|372) = limit, (718|735) = capacity, (809|810) = exemplar
    const expIds = [8, 253, 371, 372, 718, 735, 809, 810];
    const data = req.body;
    const playerId = parseInt(data.playerId);
    const playerName = data.playerName.toLowerCase();
    const expType = parseInt(data.expType);
    const points = parseInt(data.points);
    const chain = parseInt(data.chain);
    const timestamp = data.timestamp
    let type = '';

    if (!expIds.includes(expType)) {
      return res.status(400).send('Invalid input');
    }

    if ([8, 253, 371, 372].includes(expType)) {
      type = 'experience';
    } else if ([718, 735].includes(expType)) {
      type = 'capacity';
    } else if ([809, 810].includes(expType)) {
      type = 'exemplar';
    }

    const user = await users.findOne({ playerId: playerId });
    let expHistory = user?.expHistory;

    const updateField = `expHistory.${type}`;
    expHistoryByType = expHistory[type];

    if (expHistoryByType?.length >= 50) {
      expHistoryByType.shift();
    }
    expHistoryByType.push({ points, chain, timestamp });

    const updateData = {
      [`${updateField}`]: expHistoryByType
    };

    const result = await users.findOneAndUpdate(
      { playerId: playerId },
      {
        $set
          : updateData
      },
      { upsert: true, new: true }
    );

    wss.clients.forEach(client => {
      if (client.readyState === WebSocket.OPEN) {
        client.send(JSON.stringify({
          playerId: playerId,
          playerName: playerName,
          expHistory: result.expHistory
        }));
      }
    });

    res.send(`Experience history: OK`);

  } catch (error) {
    console.error(error);
    res.status(500).send('An error occurred while updating the experience history.');
  }

});

router.post('/reset_exp_history', async (req, res) => {
  try {
    const playerId = parseInt(req.body.playerId);
    const playerName = req.body.playerName.toLowerCase();

    const result = await users.findOneAndUpdate(
      { playerId: playerId },
      {
        $set: {
          [`expHistory`]: {
            experience: [],
            capacity: [],
            exemplar: []
          }
        }
      },
      { upsert: true, new: true }
    );

    wss.clients.forEach(client => {
      if (client.readyState === WebSocket.OPEN) {
        client.send(JSON.stringify({
          playerId: playerId,
          playerName: playerName,
          expHistory: result.expHistory
        }));
      }
    });

    res.send(`Experience history reset: OK`);
  } catch (error) {
    console.error(error);
    res.status(500).send('An error occurred while resetting the experience history.');
  }
});

router.post('/set_buffs', async (req, res) => {
  try {
    const data = req.body;
    const playerId = parseInt(data.playerId);
    const playerName = data.playerName.toLowerCase();
    let buffs = data.buffs;

    // Remove the last character if it is a comma
    if (buffs[buffs.length - 1] === ',') {
      buffs = buffs.slice(0, -1);
    }

    await users.findOneAndUpdate(
      { playerId: playerId },
      { $set: { buffs: buffs } },
      { upsert: true, new: true }
    );

    wss.clients.forEach(client => {
      if (client.readyState === WebSocket.OPEN) {
        client.send(JSON.stringify({
          playerId: playerId,
          playerName: playerName,
          buffs: buffs,
        }));
      }
    });

    res.send(`Buffs: OK`);
  } catch (error) {
    console.error(error);
    res.status(500).send('An error occurred while updating the buffs.');
  }

});

router.post('/set_buffs_json', async (req, res) => {
  try {
    const data = req.body;
    const playerId = parseInt(data.playerId);
    const playerName = data.playerName.toLowerCase();
    const buffs = data.buffs;
    await users.findOneAndUpdate(
      { playerId: playerId },
      { $set: { buffs: buffs } },
      { upsert: true, new: true }
    );

    wss.clients.forEach(client => {
      if (client.readyState === WebSocket.OPEN) {
        client.send(JSON.stringify({
          playerId: playerId,
          playerName: playerName,
          buffs: buffs,
        }));
      }
    });

    res.send(`Buffs: OK`);
  } catch (error) {
    console.error(error);
    res.status(500).send('An error occurred while updating the buffs.');
  }
});

router.post('/refresh_buffs', async (req, res) => {
  try {
    const playerId = parseInt(req.body.playerId);
    const playerName = req.body.playerName.toLowerCase();
    const user = await users.findOne({ playerId: playerId });
    const buffs = user.buffs;
    const currentTime = new Date().getTime();

    for (const [key, buff] of buffs.entries()) {
      const buffTime = new Date(buff.utc_time).getTime();

      if (currentTime > buffTime) {
        buffs.delete(key);
      }
    }

    await users.findOneAndUpdate({ playerId: playerId }, { $set: { buffs: buffs } });
    wss.clients.forEach(client => {
      if (client.readyState === WebSocket.OPEN) {
        client.send(JSON.stringify({
          playerId: playerId,
          playerName: playerName,
          buffs: buffs,
        }));
      }
    });
    res.send("Buffs refreshed");

  } catch (error) {
    console.error(error);
    res.status(500).send('An error occurred while removing buffs.');
  }
});

router.post('/set_ability_recasts', async (req, res) => {
  try {
    const { playerName, abilities } = req.body;
    const playerId = parseInt(req.body.playerId);
    const abilitiesParsed = JSON.parse(abilities) || [];
    // Basic validation
    if (typeof playerName !== 'string' || !Array.isArray(abilitiesParsed)) {
      return res.status(400).send('Invalid input');
    }

    const lowerCasePlayerName = playerName.toLowerCase();

    const user = await users.findOneAndUpdate(
      { playerId: playerId },
      { $set: { abilities: abilitiesParsed } },
      { upsert: true, new: true }
    );

    // Optionally, notify clients via WebSocket
    wss.clients.forEach(client => {
      if (client.readyState === WebSocket.OPEN) {
        client.send(JSON.stringify({
          playerId: playerId,
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
  try {
    const data = req.body;
    const playerId = parseInt(data.playerId);
    const playerName = data.playerName.toLowerCase();
    const zone = data.zone;

    await users.findOneAndUpdate(
      { playerId: playerId },
      { $set: { zone: zone } },
      { upsert: true, new: true }
    );

    wss.clients.forEach(client => {
      if (client.readyState === WebSocket.OPEN) {
        client.send(JSON.stringify({
          playerId: playerId,
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

router.get('/get_chat_log', async (req, res) => {
  try {
    const playerId = parseInt(req.query.playerId);
    const chat = await chats.findOne({ playerId: playerId });

    res.send(chat.chatLog);
  } catch (error) {
    console.error(error);
    res.status(500).send('An error occurred while retrieving the chat log.');
  }
});

router.post('/set_message', async (req, res) => {
  try {
    const data = req.body;
    const playerId = parseInt(data.playerId);
    const playerName = data.playerName.toLowerCase();
    const message = data.message;
    const messageType = data.messageType;
    const timeStamp = new Date().toISOString();

    const decodedMessage = message.replace(/(\x7F1|\n)/g, '');

    await chats.findOneAndUpdate(
      { playerName: playerName, playerId: playerId },
      {
        $push: {
          chatLog: {
            $each: [{ messageType, message: decodedMessage, timeStamp }],
            $slice: -1000 // Keep only the latest 1000 messages
          }
        }
      },
      { upsert: true, new: true }
    );

    wss.clients.forEach(client => {
      if (client.readyState === WebSocket.OPEN) {
        client.send(JSON.stringify({
          playerName: playerName,
          playerId: playerId,
          chatLog: { messageType, message: decodedMessage, timeStamp }
        }));
      }
    });

    res.send(`Message: OK`);
  } catch (error) {
    console.error(error);
    res.status(500).send('An error occurred while updating the message.');
  }
});

module.exports = router;