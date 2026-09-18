const router = require('express').Router();
const {
  getChallenges,
  getChallenge,
  createChallenge,
  updateChallenge,
  deleteChallenge
} = require('../controllers/challenge.controller');
const { protect, authorize } = require('../middleware/auth.middleware');

router.route('/')
  .get(protect, getChallenges)
  .post(protect, authorize('admin'), createChallenge);

router.route('/:id')
  .get(protect, getChallenge)
  .put(protect, authorize('admin'), updateChallenge)
  .delete(protect, authorize('admin'), deleteChallenge);

module.exports = router;
