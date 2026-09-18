const OpenAI = require('openai');
const Chat = require('../models/chat.model');

const openai = new OpenAI({
  apiKey: process.env.OPENAI_API_KEY
});

const SYSTEM_PROMPT = `Eres el Tutor IA de ComputaQuest, una plataforma educativa de gamificación para enseñar pensamiento computacional a estudiantes de octavo y noveno grado.

Tus responsabilidades:
1. Enseñar los 4 pilares del pensamiento computacional: Descomposición, Reconocimiento de Patrones, Abstracción y Diseño de Algoritmos
2. Explicar conceptos de programación de forma sencilla y didáctica
3. Motivar a los estudiantes con ejemplos prácticos y cotidianos
4. Responder preguntas sobre lógica, algoritmos y resolución de problemas

Reglas:
- Responde en español
- Usa un tono amigable y motivador adecuado para adolescentes
- Relaciona los conceptos con situaciones cotidianas
- Si no entiendes algo, pide aclaraciones
- Mantén las respuestas concisas (máximo 3-4 oraciones)`;

exports.sendMessage = async (userId, message, chatId = null) => {
  try {
    let chat;
    
    if (chatId) {
      chat = await Chat.findOne({ _id: chatId, user: userId });
      if (!chat) {
        chat = await Chat.create({ user: userId, messages: [] });
      }
    } else {
      chat = await Chat.create({ user: userId, messages: [] });
    }

    chat.messages.push({ role: 'user', content: message });

    const apiMessages = [
      { role: 'system', content: SYSTEM_PROMPT },
      ...chat.messages.slice(-10).map(m => ({
        role: m.role,
        content: m.content
      }))
    ];

    const completion = await openai.chat.completions.create({
      model: 'gpt-3.5-turbo',
      messages: apiMessages,
      max_tokens: 300,
      temperature: 0.7
    });

    const assistantMessage = completion.choices[0].message.content;
    chat.messages.push({ role: 'assistant', content: assistantMessage });

    await chat.save();

    return {
      chatId: chat._id,
      message: assistantMessage
    };
  } catch (error) {
    console.error('Error en Chat IA:', error);
    throw error;
  }
};

exports.getChatHistory = async (userId, chatId) => {
  try {
    const chat = await Chat.findOne({ _id: chatId, user: userId });
    return chat ? chat.messages : [];
  } catch (error) {
    throw error;
  }
};

exports.getUserChats = async (userId) => {
  try {
    const chats = await Chat.find({ user: userId })
      .select('-messages')
      .sort('-createdAt');
    return chats;
  } catch (error) {
    throw error;
  }
};
