const chatScheme = {
  playerId: Number,
  playerName: String,
  chatLog: {
    "linkshell": [
      {
        messageType: String,
        message: String,
        timeStamp: String
      }
    ],

  },
};

module.exports = chatScheme;