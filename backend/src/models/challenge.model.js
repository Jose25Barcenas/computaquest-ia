const mongoose = require('mongoose');
const { CHALLENGE_TYPES } = require('../config/constants');

const challengeSchema = new mongoose.Schema({
  title: {
    type: String,
    required: [true, 'El título es requerido'],
    trim: true
  },
  description: {
    type: String,
    required: [true, 'La descripción es requerida']
  },
  type: {
    type: String,
    enum: Object.values(CHALLENGE_TYPES),
    required: [true, 'El tipo es requerido']
  },
  difficulty: {
    type: Number,
    min: 1,
    max: 5,
    default: 1
  },
  xpReward: {
    type: Number,
    default: 100
  },
  pointsReward: {
    type: Number,
    default: 10
  },
  badgeName: {
    type: String
  },
  content: {
    type: mongoose.Schema.Types.Mixed,
    required: true
  },
  isActive: {
    type: Boolean,
    default: true
  },
  order: {
    type: Number,
    default: 0
  }
}, {
  timestamps: true
});

challengeSchema.index({ type: 1, isActive: 1 });

module.exports = mongoose.model('Challenge', challengeSchema);
