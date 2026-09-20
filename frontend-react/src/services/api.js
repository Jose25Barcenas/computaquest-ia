const API_BASE = import.meta.env.VITE_API_URL
  ? `${import.meta.env.VITE_API_URL}/api`
  : '/api'

function getToken() {
  return localStorage.getItem('computaquest_token')
}

async function request(endpoint, options = {}) {
  const token = getToken()
  const headers = {
    'Content-Type': 'application/json',
    ...options.headers,
  }

  if (token) {
    headers['Authorization'] = `Bearer ${token}`
  }

  const response = await fetch(`${API_BASE}${endpoint}`, {
    ...options,
    headers,
  })

  if (response.status === 401) {
    localStorage.removeItem('computaquest_token')
    window.location.href = '/'
    throw new Error('No autorizado')
  }

  if (!response.ok) {
    const error = await response.json().catch(() => ({ message: 'Error del servidor' }))
    throw new Error(error.message || 'Error del servidor')
  }

  if (response.status === 204) return null
  return response.json()
}

const api = {
  register: (data) => request('/auth/register', { method: 'POST', body: JSON.stringify(data) }),
  login: (data) => request('/auth/login', { method: 'POST', body: JSON.stringify(data) }),
  getMe: () => request('/auth/me'),
  updateProfile: (data) => request('/auth/profile', { method: 'PUT', body: JSON.stringify(data) }),

  getChallenges: (params = {}) => {
    const query = new URLSearchParams(params).toString()
    return request(`/challenges${query ? `?${query}` : ''}`)
  },
  getChallenge: (id) => request(`/challenges/${id}`),
  createChallenge: (data) => request('/challenges', { method: 'POST', body: JSON.stringify(data) }),
  updateChallenge: (id, data) => request(`/challenges/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
  deleteChallenge: (id) => request(`/challenges/${id}`, { method: 'DELETE' }),

  getProgress: () => request('/progress'),
  completeChallenge: (data) => request('/progress/complete', { method: 'POST', body: JSON.stringify(data) }),
  getLeaderboard: () => request('/progress/leaderboard'),

  sendChatMessage: (data) => request('/chat/send', { method: 'POST', body: JSON.stringify(data) }),
  getChatHistory: (chatId) => request(`/chat/history/${chatId}`),
  getUserChats: () => request('/chat/chats'),

  getUsers: () => request('/users'),
  deleteUser: (id) => request(`/users/${id}`, { method: 'DELETE' }),

  submitSurvey: (data) => request('/surveys', { method: 'POST', body: JSON.stringify(data) }),
  getUserSurveys: () => request('/surveys'),
  getLatestSurvey: (type) => request(`/surveys/latest?type=${type}`),
  getSurveyStats: (type) => request(`/surveys/stats?type=${type}`),
  getSurveyStatsByDemographic: (type, demographic) => request(`/surveys/stats/${demographic}?type=${type}`),
  getAllSurveys: () => request('/surveys/all'),
}

export default api
