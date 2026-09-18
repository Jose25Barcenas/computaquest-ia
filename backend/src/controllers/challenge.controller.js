const Challenge = require('../models/challenge.model');

exports.getChallenges = async (req, res, next) => {
  try {
    const { type, difficulty } = req.query;
    const filter = { isActive: true };
    
    if (type) filter.type = type;
    if (difficulty) filter.difficulty = parseInt(difficulty);

    const challenges = await Challenge.find(filter).sort('order');
    
    res.json({
      success: true,
      count: challenges.length,
      data: { challenges }
    });
  } catch (error) {
    next(error);
  }
};

exports.getChallenge = async (req, res, next) => {
  try {
    const challenge = await Challenge.findById(req.params.id);
    
    if (!challenge) {
      return res.status(404).json({
        success: false,
        message: 'Reto no encontrado'
      });
    }

    res.json({
      success: true,
      data: { challenge }
    });
  } catch (error) {
    next(error);
  }
};

exports.createChallenge = async (req, res, next) => {
  try {
    const challenge = await Challenge.create(req.body);
    
    res.status(201).json({
      success: true,
      data: { challenge }
    });
  } catch (error) {
    next(error);
  }
};

exports.updateChallenge = async (req, res, next) => {
  try {
    const challenge = await Challenge.findByIdAndUpdate(
      req.params.id,
      req.body,
      { new: true, runValidators: true }
    );

    if (!challenge) {
      return res.status(404).json({
        success: false,
        message: 'Reto no encontrado'
      });
    }

    res.json({
      success: true,
      data: { challenge }
    });
  } catch (error) {
    next(error);
  }
};

exports.deleteChallenge = async (req, res, next) => {
  try {
    const challenge = await Challenge.findByIdAndDelete(req.params.id);

    if (!challenge) {
      return res.status(404).json({
        success: false,
        message: 'Reto no encontrado'
      });
    }

    res.json({
      success: true,
      message: 'Reto eliminado correctamente'
    });
  } catch (error) {
    next(error);
  }
};
