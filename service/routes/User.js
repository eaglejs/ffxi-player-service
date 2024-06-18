const express = require('express');
const bodyParser = require('body-parser');
const mongoose = require('mongoose');
const router = express.Router();
const WebSocket = require('ws');

mongoose.connect('mongodb://localhost:27017/ffxi');

const wss = new WebSocket.Server({ port: 8081 });

wss.on('connection', function connection(ws) {
	ws.on('message', function incoming(message) {
		console.log('received: %s', message);
	});
	ws.send('{"msg": "Hello! I am the server!"}')
});

const userSchema = new mongoose.Schema({
	attack: Number,
	buffs: String,
	currentExemplar: Number,
	defense: Number,
	lastOnline: Number,
	mainJobLevel: Number,
	masterLevel: Number,
	requiredExemplar: Number,
	subJob: String,
	subJobLevel: Number,
	zone: String,
  mainJob: String,
  playerName: String,
	hpp: Number,
	mpp: Number,
});

const users = mongoose.model('users', userSchema);

router.use(bodyParser.json());
router.use(bodyParser.urlencoded({ extended: true }));

router.get('/get_user', async(req, res) => {
	const playerName = req.query.playerName.toLowerCase();

	try {
		const user = await users.findOne({ playerName });
		res.send(user);
	} catch (error) {
		console.error(error);
		res.status(500).send('An error occurred while retrieving the user.');
	}
});

router.get('/get_users', async(req, res) => {
	try {
		const allUsers = await users.find({}).sort({ 'playerName': 1});
		res.send(allUsers);
	} catch (error) {
		console.error(error);
		res.status(500).send('An error occurred while retrieving the users.');
	}
});

router.post('/set_online', async(req, res) => {
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

router.post('/set_hpp', async(req, res) => {
	const data = req.body;
	const playerName = data.playerName.toLowerCase();
	const hpp = data.hpp;

	try {
		await users.findOneAndUpdate(
			{ playerName: playerName },
			{ $set: { hpp: hpp } },
			{ upsert: true, new: true }
		);
		
		wss.clients.forEach(client => {
			if (client.readyState === WebSocket.OPEN) {
				client.send(JSON.stringify({
					playerName: playerName,
					hpp: hpp
				}));
			}
		});

		res.send(`HP: OK`);
	} catch (error) {
		console.error(error);
		res.status(500).send('An error occurred while updating the HP.');
	}
});

router.post('/set_mpp', async(req, res) => {
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

router.post('/set_stats', async(req, res) => {
	const data = req.body;
	const playerName = data.playerName.toLowerCase();
	const masterLevel = data.masterLevel;
	const mainJobLevel = data.mainJobLevel;
	const subJobLevel = data.subJobLevel;
	const attack = data.attack;
	const defense = data.defense;
	const currentExemplar = data.currentExemplar;
	const requiredExemplar = data.requiredExemplar;

	try {
		await users.findOneAndUpdate(
			{ playerName: playerName },
			{ $set: { 
				masterLevel: masterLevel,
				mainJobLevel: mainJobLevel,
				subJobLevel: subJobLevel,
				attack: attack,
				defense: defense,
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

router.post('/set_buffs', async(req, res) => {
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

router.post('/set_zone', async(req, res) => {
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