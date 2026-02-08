const chatType = {
  type: Map,
  of: [{
    messageType: String,
    message: String,
    timeStamp: String
  }]
};

const chatScheme = {
  playerId: Number,
  chatLog: chatType,
};

module.exports = chatScheme;