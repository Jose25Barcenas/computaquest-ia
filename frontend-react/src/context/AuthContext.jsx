import { createContext, useContext, useState, useEffect, useCallback, useMemo } from 'react'
import api, { setOnUnauthorized } from '../services/api'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null)
  const [loading, setLoading] = useState(() => Boolean(localStorage.getItem('computaquest_token')))

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
    if (!token) return () => controller.abort()

    api.getMe({ signal: controller.signal })
      .then(data => {
        setUser(data.user || data)
      })
      .catch(err => {
        if (err?.name === 'AbortError') return
        // Un 401 ya cierra sesion via setOnUnauthorized (api.js).
        // Otros errores (red, 500) no deben destruir la sesion.
        if (err?.status === 401) setUser(null)
      })
      .finally(() => setLoading(false))

    return () => controller.abort()
  }, [])

  const login = useCallback(async (email, password) => {
    const data = await api.login({ email, password })
    localStorage.setItem('computaquest_token', data.token)
    setUser(data)
    return data
  }, [])

  const register = useCallback(async (name, email, password, avatar, grade) => {
    const data = await api.register({ name, email, password, avatar, grade })
    return data
  }, [])

  const updateUser = useCallback((userData) => {
    setUser(prev => ({ ...prev, ...userData }))
  }, [])

  const value = useMemo(
    () => ({ user, loading, login, register, logout, updateUser }),
    [user, loading, login, register, logout, updateUser]
  )

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  )
}

export const useAuth = () => useContext(AuthContext)
