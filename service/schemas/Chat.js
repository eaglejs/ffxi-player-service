const chatScheme = {
  playerId: Number,
  playerName: String,
  chatLog: [{
    messageType: String,
    message: String,
    timeStamp: String
  }],
};

module.exports = chatScheme;