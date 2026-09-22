import { createContext, useContext, useState, useEffect, useCallback } from 'react'
import api, { setOnUnauthorized } from '../services/api'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null)
  const [loading, setLoading] = useState(true)

  const logout = useCallback(() => {
    localStorage.removeItem('computaquest_token')
    setUser(null)
  }, [])

  useEffect(() => {
    setOnUnauthorized(logout)
    return () => setOnUnauthorized(null)
  }, [logout])

  useEffect(() => {
    const controller = new AbortController()
    const token = localStorage.getItem('computaquest_token')
    if (token) {
      api.getMe({ signal: controller.signal })
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
    return () => controller.abort()
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
