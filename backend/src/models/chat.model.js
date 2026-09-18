const mongoose = require('mongoose');

const chatMessageSchema = new mongoose.Schema({
  role: {
    type: String,
    enum: ['user', 'assistant'],
    required: true
  },
  content: {
    type: String,
    required: true
  }
});

const chatSchema = new mongoose.Schema({
  user: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'User',
    required: true
  },
  messages: [chatMessageSchema],
  challengeType: {
    type: String
  }
}, {
  timestamps: true
});

chatSchema.index({ user: 1, createdAt: -1 });

module.exports = mongoose.model('Chat', chatSchema);
