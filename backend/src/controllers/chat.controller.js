const chatService = require('../services/chat.service');

exports.sendMessage = async (req, res, next) => {
  try {
    const { message, chatId } = req.body;

    if (!message || !message.trim()) {
      return res.status(400).json({
        success: false,
        message: 'El mensaje es requerido'
      });
    }

    const result = await chatService.sendMessage(
      req.user.id,
      message.trim(),
      chatId
    );

    res.json({
      success: true,
      data: result
    });
  } catch (error) {
    next(error);
  }
};

exports.getChatHistory = async (req, res, next) => {
  try {
    const { chatId } = req.params;
    const messages = await chatService.getChatHistory(req.user.id, chatId);

    res.json({
      success: true,
      data: { messages }
    });
  } catch (error) {
    next(error);
  }
};

exports.getUserChats = async (req, res, next) => {
  try {
    const chats = await chatService.getUserChats(req.user.id);

    res.json({
      success: true,
      count: chats.length,
      data: { chats }
    });
  } catch (error) {
    next(error);
  }
};
