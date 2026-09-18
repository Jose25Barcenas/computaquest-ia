const router = require('express').Router();
const {
  getUserProgress,
  completeChallenge,
  getLeaderboard
} = require('../controllers/progress.controller');
const { protect } = require('../middleware/auth.middleware');

router.get('/', protect, getUserProgress);
router.post('/complete', protect, completeChallenge);
router.get('/leaderboard', protect, getLeaderboard);

module.exports = router;
