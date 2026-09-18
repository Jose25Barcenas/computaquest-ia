const Progress = require('../models/progress.model');
const User = require('../models/user.model');

exports.getUserProgress = async (req, res, next) => {
  try {
    const progress = await Progress.find({ user: req.user.id })
      .populate('challenge', 'title type difficulty');
    
    res.json({
      success: true,
      count: progress.length,
      data: { progress }
    });
  } catch (error) {
    next(error);
  }
};

exports.completeChallenge = async (req, res, next) => {
  try {
    const { challengeId, score } = req.body;

    let progress = await Progress.findOne({
      user: req.user.id,
      challenge: challengeId
    });

    if (progress && progress.completed) {
      return res.status(400).json({
        success: false,
        message: 'Ya completaste este reto'
      });
    }

    if (!progress) {
      progress = new Progress({
        user: req.user.id,
        challenge: challengeId
      });
    }

    progress.score = score;
    progress.attempts += 1;
    progress.lastAttemptAt = new Date();
    progress.completed = true;
    progress.completedAt = new Date();

    await progress.save();

    const user = await User.findById(req.user.id);
    user.points += score >= 70 ? 10 : 5;
    user.xp += score >= 70 ? 100 : 50;

    if (user.xp >= 500) {
      user.level += 1;
      user.xp -= 500;
    }

    await user.save();

    res.json({
      success: true,
      data: {
        progress,
        user: user.toPublicJSON()
      }
    });
  } catch (error) {
    next(error);
  }
};

exports.getLeaderboard = async (req, res, next) => {
  try {
    const users = await User.find({ role: 'student' })
      .select('name level points badges avatar')
      .sort('-points')
      .limit(10);

    res.json({
      success: true,
      data: { leaderboard: users }
    });
  } catch (error) {
    next(error);
  }
};
