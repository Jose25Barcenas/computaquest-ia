const API_BASE = import.meta.env.VITE_API_URL
  ? `${import.meta.env.VITE_API_URL}/api`
  : '/api'

let onUnauthorized = null

export function setOnUnauthorized(callback) {
  onUnauthorized = callback
}

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
    if (onUnauthorized) onUnauthorized()
    throw new Error('No autorizado')
  }

  if (!response.ok) {
    const body = await response.json().catch(() => null)
    const firstError = body?.errors ? Object.values(body.errors)[0] : null
    const error = new Error(body?.message || firstError || 'Error del servidor')
    error.status = response.status
    error.errors = body?.errors || null
    throw error
  }

  if (response.status === 204) return null
  const text = await response.text()
  return text ? JSON.parse(text) : null
}

async function downloadCsv(endpoint, filename) {
  const token = getToken()
  const headers = {}
  if (token) headers['Authorization'] = `Bearer ${token}`

  const response = await fetch(`${API_BASE}${endpoint}`, { headers })

  if (!response.ok) {
    const body = await response.json().catch(() => null)
    throw new Error(body?.message || 'Error al descargar el archivo')
  }

  const blob = await response.blob()
  const url = window.URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  document.body.appendChild(link)
  link.click()
  link.remove()
  window.URL.revokeObjectURL(url)
}

const api = {
  register: (data) => request('/auth/register', { method: 'POST', body: JSON.stringify(data) }),
  login: (data) => request('/auth/login', { method: 'POST', body: JSON.stringify(data) }),
  getMe: (options = {}) => request('/auth/me', options),
  updateProfile: (data) => request('/auth/profile', { method: 'PUT', body: JSON.stringify(data) }),
  forgotPassword: (data) => request('/auth/forgot-password', { method: 'POST', body: JSON.stringify(data) }),
  resetPassword: (data) => request('/auth/reset-password', { method: 'POST', body: JSON.stringify(data) }),
  changePassword: (data) => request('/auth/change-password', { method: 'POST', body: JSON.stringify(data) }),

  getChallenges: (params = {}) => {
    const query = new URLSearchParams(params).toString()
    return request(`/challenges${query ? `?${query}` : ''}`)
  },
  getChallenge: (id, options = {}) => request(`/challenges/${id}`, options),
  createChallenge: (data) => request('/challenges', { method: 'POST', body: JSON.stringify(data) }),
  updateChallenge: (id, data) => request(`/challenges/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
  deleteChallenge: (id) => request(`/challenges/${id}`, { method: 'DELETE' }),

  getProgress: (options = {}) => request('/progress', options),
  completeChallenge: (data) => request('/progress/complete', { method: 'POST', body: JSON.stringify(data) }),
  getLeaderboard: () => request('/progress/leaderboard'),

  sendChatMessage: (data) => request('/chat/send', { method: 'POST', body: JSON.stringify(data) }),
  getChatHistory: (chatId) => request(`/chat/history/${chatId}`),
  getUserChats: () => request('/chat/chats'),

  getUsers: () => request('/users'),
  deleteUser: (id) => request(`/users/${id}`, { method: 'DELETE' }),

  getAdminStats: () => request('/admin/stats'),

  getAdminChallengeStats: () => request('/admin/challenge-stats'),

  getAdminActivity: () => request('/admin/activity'),

  resetAdminData: () => request('/admin/reset-data', { method: 'DELETE' }),

  getSurveyComparison: () => request('/surveys/compare'),

  submitSurvey: (data) => request('/surveys', { method: 'POST', body: JSON.stringify(data) }),
  getUserSurveys: () => request('/surveys'),
  getLatestSurvey: (type) => request(`/surveys/latest?type=${type}`),
  getSurveyStats: (type) => request(`/surveys/stats?type=${type}`),
  getSurveyStatsByDemographic: (type, demographic) => request(`/surveys/stats/${demographic}?type=${type}`),
  getAllSurveys: () => request('/surveys/all'),

  pilotGetSession: (code) => request(`/pilot/session?code=${encodeURIComponent(code)}`),
  pilotSubmitTest: (data) => request('/pilot/test', { method: 'POST', body: JSON.stringify(data) }),
  pilotSubmitMotivation: (data) => request('/pilot/motivation', { method: 'POST', body: JSON.stringify(data) }),
  pilotSubmitExpert: (data) => request('/pilot/expert', { method: 'POST', body: JSON.stringify(data) }),

  exportPilotCsv: () => downloadCsv('/admin/pilot/export.csv', 'piloto-resultados.csv'),
  exportPilotExpertCsv: () => downloadCsv('/admin/pilot/export-experto.csv', 'piloto-validacion-docente.csv'),
  deletePilotSession: (code) => request(`/admin/pilot/sessions?code=${encodeURIComponent(code)}`, { method: 'DELETE' }),
}

export default api
