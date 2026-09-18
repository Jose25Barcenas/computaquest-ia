import { createContext, useContext, useState, useEffect } from 'react'
import api from '../services/api'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const token = localStorage.getItem('computaquest_token')
    if (token) {
      api.getMe()
        .then(data => {
          setUser(data.user || data)
        })
        .catch(() => {
          localStorage.removeItem('computaquest_token')
        })
        .finally(() => setLoading(false))
    } else {
      setLoading(false)
    }
  }, [])

  const login = async (email, password) => {
    const data = await api.login({ email, password })
    localStorage.setItem('computaquest_token', data.token)
    setUser(data)
    return data
  }

  const register = async (name, email, password, avatar, grade) => {
    const data = await api.register({ name, email, password, avatar, grade })
    return data
  }

  const logout = () => {
    localStorage.removeItem('computaquest_token')
    setUser(null)
  }

  const updateUser = (userData) => {
    setUser(prev => ({ ...prev, ...userData }))
  }

  return (
    <AuthContext.Provider value={{ user, loading, login, register, logout, updateUser }}>
      {children}
    </AuthContext.Provider>
  )
}

export const useAuth = () => useContext(AuthContext)
