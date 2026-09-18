const router = require('express').Router();
const { sendMessage, getChatHistory, getUserChats } = require('../controllers/chat.controller');
const { protect } = require('../middleware/auth.middleware');

router.post('/send', protect, sendMessage);
router.get('/history/:chatId', protect, getChatHistory);
router.get('/chats', protect, getUserChats);

module.exports = router;
