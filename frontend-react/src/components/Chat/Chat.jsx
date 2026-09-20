import { useState, useRef, useEffect } from 'react'
import { useToast } from '../../context/ToastContext'
import api from '../../services/api'

export default function Chat({ user }) {
  const [chats, setChats] = useState([])
  const [activeChat, setActiveChat] = useState(null)
  const [messages, setMessages] = useState([])
  const [input, setInput] = useState('')
  const [loading, setLoading] = useState(false)
  const messagesEndRef = useRef(null)
  const toast = useToast()

  useEffect(() => {
    loadChats()
  }, [])

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [messages])

  const loadChats = async () => {
    try {
      const data = await api.getUserChats()
      setChats(data)
    } catch (error) {
      toast.error('Error al cargar chats')
    }
  }

  const loadChatHistory = async (chatId) => {
    try {
      const data = await api.getChatHistory(chatId)
      setMessages(data)
      setActiveChat(chatId)
    } catch (error) {
      toast.error('Error al cargar historial')
    }
  }

  const sendMessage = async (e) => {
    e.preventDefault()
    if (!input.trim() || loading) return

    const userMessage = { role: 'USER', content: input.trim() }
    setMessages(prev => [...prev, userMessage])
    setInput('')
    setLoading(true)

    try {
      const data = await api.sendChatMessage({
        message: userMessage.content,
        chatId: activeChat || undefined,
      })

      setMessages(prev => [...prev, { role: 'ASSISTANT', content: data.message }])
      setActiveChat(data.chatId)
      loadChats()
    } catch (error) {
      setMessages(prev => [...prev, { role: 'ASSISTANT', content: 'Lo siento, hubo un error. Intenta de nuevo.' }])
      toast.error('Error al enviar mensaje')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="chat-container glass-panel">
      <div className="chat-header">
        <i className="fa-solid fa-robot"></i>
        <h3>Tutor IA</h3>
      </div>

      {chats.length > 0 && !activeChat && (
        <div className="chat-history-list">
          <p className="chat-history-title">Chats anteriores:</p>
          {chats.slice(0, 5).map(chat => (
            <button
              key={chat.id}
              className="chat-history-item"
              onClick={() => loadChatHistory(chat.id)}
            >
              <i className="fa-solid fa-message"></i>
              <span>{chat.messageCount} mensajes</span>
            </button>
          ))}
        </div>
      )}

      <div className="chat-messages" role="log" aria-live="polite">
        {messages.length === 0 && (
          <div className="chat-empty">
            <i className="fa-solid fa-robot"></i>
            <p>¡Hola! Soy tu tutor de pensamiento Computacional.</p>
            <p>Preguntame lo que quieras sobre los 4 pilares:</p>
            <div className="chat-suggestions">
              <button onClick={() => setInput('¿Que es la descomposicion?')}>Descomposicion</button>
              <button onClick={() => setInput('Explica el reconocimiento de patrones')}>Patrones</button>
              <button onClick={() => setInput('Que es la abstraccion?')}>Abstraccion</button>
              <button onClick={() => setInput('Crea un algoritmo simple')}>Algoritmos</button>
            </div>
          </div>
        )}

        {messages.map((msg, i) => (
          <div key={i} className={`chat-message ${msg.role === 'USER' ? 'user-message' : 'ai-message'}`}>
            <div className="message-avatar">
              <i className={`fa-solid fa-${msg.role === 'USER' ? 'user' : 'robot'}`}></i>
            </div>
            <div className="message-content">{msg.content}</div>
          </div>
        ))}

        {loading && (
          <div className="chat-message ai-message">
            <div className="message-avatar"><i className="fa-solid fa-robot"></i></div>
            <div className="message-content typing-indicator">
              <span></span><span></span><span></span>
            </div>
          </div>
        )}
        <div ref={messagesEndRef} />
      </div>

      <form className="chat-input" onSubmit={sendMessage}>
        <input
          type="text"
          id="chat-input"
          value={input}
          onChange={(e) => setInput(e.target.value)}
          placeholder="Escribe tu pregunta..."
          disabled={loading}
          aria-label="Mensaje para el tutor IA"
        />
        <button type="submit" disabled={loading || !input.trim()}>
          <i className="fa-solid fa-paper-plane"></i>
        </button>
      </form>
    </div>
  )
}
