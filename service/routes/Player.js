const express = require('express');
const bodyParser = require('body-parser');
const mongoose = require('mongoose');
const router = express.Router();
const iconv = require('iconv-lite');
const WebSocket = require('ws');
const playerScheme = require('../schemas/Player');
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

const playerSchema = new mongoose.Schema(playerScheme);
const chatSchema = new mongoose.Schema(chatScheme);

const players = mongoose.model('players', playerSchema);
const chats = mongoose.model('chats', chatSchema);

router.use(bodyParser.json());
router.use(bodyParser.urlencoded({ extended: true }));

function removeControlCharacters(str) {
  return str.replace(/[\x00-\x1F\x7F]/g, '');
}

router.post('/initialize_player', async (req, res) => {
  try {
    const data = req.body;
    const playerId = parseInt(data.playerId);
    const playerName = data.playerName.toLowerCase();
    const lastOnline = parseInt(data.lastOnline);

    await players.findOneAndUpdate(
      { playerName: playerName },
      { $set: { playerId: playerId, playerName: playerName, lastOnline: lastOnline } },
      { upsert: true, new: true }
    );

    res.send(`User ${playerName} initialized`);
  } catch (error) {
    console.error('initialize_player', error);
    res.status(500).send('An error occurred while initializing the player.');
  }
});

router.get('/get_player', async (req, res) => {
  try {
    const playerId = parseInt(req.query.playerId);
    const player = await players.findOne({ playerId });
    res.send(player);
  } catch (error) {
    console.error('get_player', error);
    res.status(500).send('An error occurred while retrieving the player.');
  }
});

router.get('/get_players', async (req, res) => {
  try {
    // get all online players that lastOnline is within the last 60 seconds
    const currentTime = Math.floor(Date.now() / 1000);
    const thresholdTime = currentTime - 60; // 60 seconds ago
    const allOnlinePlayers = await players.find({ lastOnline: { $gte: thresholdTime } }).sort({ 'playerName': 1 });
    res.send(allOnlinePlayers);
  } catch (error) {
    console.error('get_players', error);
    res.status(500).send('An error occurred while retrieving the players.');
  }
});

router.post('/set_online', async (req, res) => {
  try {
    const data = req.body;
    const playerId = parseInt(data.playerId);
    const playerName = data.playerName.toLowerCase();
    const last_online = parseInt(data.lastOnline);

    await players.findOneAndUpdate(
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
    console.error('set_online', error);
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

    await players.findOneAndUpdate(
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
    console.error('set_jobs', error);
    res.status(500).send('An error occurred while updating the main job.');
  }
});

router.post('/set_gil', async (req, res) => {
  try {
    const data = req.body;
    const playerId = parseInt(data.playerId);
    const playerName = data.playerName.toLowerCase();
    const gil = data.gil;

    await players.findOneAndUpdate
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
    console.error('set_gil', error);
    res.status(500).send('An error occurred while updating the gil.');
  }
});

router.post('/set_player_status', async (req, res) => {
  try {
    const data = req.body;
    const playerId = parseInt(data.playerId);
    const playerName = data.playerName.toLowerCase();
    const status = data.status;

    await players.findOneAndUpdate
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
    console.error('set_player_status', error);
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

    await players.findOneAndUpdate(
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
    console.error('set_hpp', error);
    res.status(500).send('An error occurred while updating the HP.');
  }
});

router.post('/set_mpp', async (req, res) => {
  try {
    const data = req.body;
    const playerId = parseInt(data.playerId);
    const playerName = data.playerName.toLowerCase();
    const mpp = data.mpp;

    await players.findOneAndUpdate(
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
    console.error('set_mpp', error);
    res.status(500).send('An error occurred while updating the MP.');
  }
});

router.post('/set_tp', async (req, res) => {
  try {
    const data = req.body;
    const playerId = parseInt(data.playerId);
    const playerName = data.playerName.toLowerCase();
    const tp = data.tp;

    await players.findOneAndUpdate
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
    console.error('set_tp', error);
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

    await players.findOneAndUpdate(
      { playerId: playerId },
      {
        $set: {
          masterLevel: parseInt(masterLevel),
          mainJobLevel: parseInt(mainJobLevel),
          subJobLevel: parseInt(subJobLevel),
          attack: parseInt(attack),
          defense: parseInt(defense),
          stats: stats,
          title: title,
          nationRank: parseInt(nationRank),
          currentExemplar: parseInt(currentExemplar),
          requiredExemplar: parseInt(requiredExemplar),
        }
      },
      { upsert: true, new: true }
    );

    wss.clients.forEach(client => {
      if (client.readyState === WebSocket.OPEN) {
        client.send(JSON.stringify({
          playerId: parseInt(playerId),
          playerName: playerName,
          masterLevel: parseInt(masterLevel),
          mainJobLevel: parseInt(mainJobLevel),
          subJobLevel: parseInt(subJobLevel),
          attack: parseInt(attack),
          defense: parseInt(defense),
          stats: stats,
          title: title,
          nationRank: parseInt(nationRank),
          currentExemplar: parseInt(currentExemplar),
          requiredExemplar: parseInt(requiredExemplar),
        }));
      }
    });

    res.send(`Stats: OK`);
  } catch (error) {
    console.error('set_stats', error);
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

    await players.findOneAndUpdate(
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
    console.error('set_currency1', error);
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

    await players.findOneAndUpdate(
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
    console.error('set_currency2', error);
    res.status(500).send('An error occurred while updating the currency2.');
  }

});

router.post('/update_merits', async (req, res) => {
  try {
    const data = req.body;
    const playerId = parseInt(data.playerId);
    const playerName = data.playerName.toLowerCase();
    const { total, max } = data;

    await players.findOneAndUpdate(
      { playerId: playerId },
      { $set: { merits: { total: total, max: max } } },
      { upsert: true, new: true }
    );

    wss.clients.forEach(client => {
      if (client.readyState === WebSocket.OPEN) {
        client.send(JSON.stringify({
          playerId: playerId,
          playerName: playerName,
          merits: { total: total, max: max }
        }));
      }
    });

    res.send(`Merits: OK`);
  } catch (error) {
    console.error('update_merits', error);
    res.status(500).send('An error occurred while updating the merits.');
  }
});

router.post('/update_capacity_points', async (req, res) => {
  try {
    const data = req.body;
    const playerId = parseInt(data.playerId);
    const playerName = data.playerName.toLowerCase();
    const { numberOfJobPoints } = data;

    await players.findOneAndUpdate(
      { playerId: playerId },
      {
        $set: {
          capacityPoints: {
            total: parseInt(numberOfJobPoints)
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
          capacityPoints: {
            total: parseInt(numberOfJobPoints)
          }
        }));
      }
    });

    res.send(`Capacity points: OK`);
  }
  catch (error) {
    console.error('update_capacity_points', error);
    res.status(500).send('An error occurred while updating the capacity points.');
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

    const player = await players.findOne({ playerId: playerId });
    let expHistory = player?.expHistory;

    const updateField = `expHistory.${type}`;
    expHistoryByType = expHistory[type];

    if (expHistoryByType?.length >= 50) {
      expHistoryByType.shift();
    }
    expHistoryByType.push({ points, chain, timestamp });

    const updateData = {
      [`${updateField}`]: expHistoryByType
    };

    const result = await players.findOneAndUpdate(
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
    console.error('update_exp_history', error);
    res.status(500).send('An error occurred while updating the experience history.');
  }

});

router.post('/reset_exp_history', async (req, res) => {
  try {
    const playerId = parseInt(req.body.playerId);
    const playerName = req.body.playerName.toLowerCase();

    const result = await players.findOneAndUpdate(
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
    console.error('reset_exp_history', error);
    res.status(500).send('An error occurred while resetting the experience history.');
  }
});

router.get('/get_buffs', async (req, res) => {
  try {
    const playerId = parseInt(req.query.playerId);
    const player = await players.findOne({ playerId: parseInt(playerId) });
    if (!player || !player.buffs) {
      console.error('get_buffs', 'Player not found or buffs are empty');
      return res.send({ playerName: player.playerName, playerId, buffs: [] });
    }
    // Convert the buffs Map to an array of objects
    const allBuffs = Array.from([...player.buffs.values()] ?? []).reduce((acc, buff) => {
      if (buff) {
        acc.push({
          buff_id: buff.buff_id,
          buff_name: buff.buff_name,
          buff_type: buff.buff_type,
          buff_duration: buff.buff_duration,
          utc_time: buff.utc_time,
          timestamp: buff.timestamp
        });
      }
      return acc;
    }, []);
    // Sort all buffs by timestamp in ascending order by utc_time
    allBuffs.sort((a, b) => {
      return new Date(a.utc_time) - new Date(b.utc_time);
    });
    res.send({ playerName: player.playerName, playerId, allBuffs });
  } catch (error) {
    console.error('get_buffs', error);
    res.status(500).send('An error occurred while retrieving the buffs.');
  }
});

router.post('/set_buffs_json', async (req, res) => {
  try {
    const data = req.body;
    const playerId = parseInt(data.playerId);
    const playerName = data.playerName.toLowerCase();
    const buffs = data.buffs;

    await players.findOneAndUpdate(
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
    console.error('set_buffs_json', error);
    res.status(500).send('An error occurred while updating the buffs.');
  }
});

router.post('/refresh_buffs', async (req, res) => {
  try {
    const playerId = parseInt(req.body.playerId);
    const playerName = req.body.playerName.toLowerCase();
    const player = await players.findOne({ playerId: playerId });
    const buffs = player.buffs;
    const currentTime = new Date().getTime();

    for (const [key, buff] of buffs.entries()) {
      const buffTime = new Date(buff.utc_time).getTime();

      if (currentTime > buffTime) {
        buffs.delete(key);
      }
    }

    await players.findOneAndUpdate({ playerId: playerId }, { $set: { buffs: buffs } });
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
    console.error('refresh_buffs', error);
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

    const player = await players.findOneAndUpdate(
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
          abilities: player.abilities
        }));
      }
    });

    res.send(`Abilities updated for ${playerName}`);
  } catch (error) {
    console.error('set_ability_recasts', error);
    res.status(500).send('An error occurred while updating abilities.');
  }
});

router.post('/set_zone', async (req, res) => {
  try {
    const data = req.body;
    const playerId = parseInt(data.playerId);
    const playerName = data.playerName.toLowerCase();
    const zone = data.zone;

    await players.findOneAndUpdate(
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
    console.error('set_zone', error);
    res.status(500).send('An error occurred while updating the zone.');
  }
});

router.get('/get_chat_log', async (req, res) => {
  try {
    const playerId = parseInt(req.query.playerId);
    // get the last 5000 messages across all types
    const player = await chats.findOne({ playerId });

    if (!player || !player.chatLog) {
      console.error('get_chat_log', 'Player not found or chatLog is empty');
      return res.send({ chatLog: [] });
    }

    // Convert the chatLog Map to an array of objects
    const allMessages = Array.from(player.chatLog ?? []).reduce((acc, [key, value]) => {
      if (value) {
        value.forEach((message) => {
          acc.push({
            messageType: key,
            message: removeControlCharacters(message.message),
            timeStamp: message.timeStamp
          });
        });
      }
      return acc;
    }, []);

    // Sort all messages by timestamp in ascending order
    allMessages.sort((a, b) => {
      return new Date(a.timeStamp) - new Date(b.timeStamp);
    });

    // Limit to the latest 5000 messages
    const limitedMessages = allMessages.slice(-5000);

    res.send(limitedMessages);
  } catch (error) {
    console.error('get_chat_log', error);
    res.status(500).send('An error occurred while retrieving the chat log.');
  }
});

router.get('/get_chat_log_by_type', async (req, res) => {
  try {
    const playerId = parseInt(req.query.playerId);
    const messageType = req.query.messageType.trim().toUpperCase();

    // get the last 5000 messages across all types
    const player = await chats.findOne({ playerId });

    if (!player || !player.chatLog) {
      console.error('get_chat_log', 'Player not found or chatLog is empty');
      return res.send({ chatLog: [] });
    }

    // Convert the chatLog Map to an array of objects
    const allMessages = Array.from(player.chatLog.get(messageType) ?? []).reduce((acc, value) => {
      if (value) {
        acc.push({
          messageType: messageType,
          message: removeControlCharacters(value.message),
          timeStamp: value.timeStamp
        });
      }
      return acc;
    }, []);

    // Limit to the latest 5000 messages
    const limitedMessages = allMessages.slice(-5000);

    res.send(limitedMessages);
  } catch (error) {
    console.error('get_chat_log_by_type', error);
    res.status(500).send('An error occurred while retrieving the chat log.');
  }
});

router.post('/set_messages', async (req, res) => {
  try {
    const data = req.body;

    if (!data.playerId || !data.playerName || typeof data.messages !== 'object' || data.messages === null || !data.messageType) {
      return res.status(400).send('Missing or invalid required fields: playerId, playerName, messages (must be an object), messageType.');
    }

    const playerId = parseInt(data.playerId);
    if (isNaN(playerId)) {
      return res.status(400).send('Invalid playerId.');
    }

    const playerName = String(data.playerName).toLowerCase();
    const messageType = String(data.messageType); // Ensure messageType is a string for use as a dynamic key
    const timeStamp = new Date().toISOString();

    const inputMessageValues = Object.values(data.messages);

    if (inputMessageValues.length === 0) {
      return res.send('Message: OK (No messages to process)');
    }

    const messagesPackage = inputMessageValues.map(value => {
      const messageString = typeof value === 'string' ? value : String(value); // Ensure value is a string
      // const decodedMessage = messageString.replace(/(\x7F1|\n)/g, '');
      return { messageType, message: messageString, timeStamp };
    });

    if (messagesPackage.length === 0) {
      // This case might occur if all messages were empty strings or non-string coercible after processing
      return res.send('Message: OK (No valid messages to process after decoding)');
    }

    await chats.findOneAndUpdate(
      { playerId },
      { $push: { [`chatLog.${messageType}`]: { $each: messagesPackage, $slice: -5000 } } },
      { upsert: true } // Removed `new: true` as the updated document is not used here
    );

    const wsPayload = JSON.stringify({
      playerName: playerName,
      playerId: playerId,
      chatLog: messagesPackage
    });

    wss.clients.forEach(client => {
      if (client.readyState === WebSocket.OPEN) {
        client.send(wsPayload);
      }
    });

    res.send(`Message: OK`);
  } catch (error) {
    console.error('set_messages', error);
    res.status(500).send('An error occurred while updating the message.');
  }
});

module.exports = router;