import { useState, useEffect, useCallback } from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { useToast } from '../context/ToastContext'
import api from '../services/api'
import RadarChart from '../components/RadarChart/RadarChart'
import { MODULES } from '../constants'

export default function DashboardPage() {
  const { user, updateUser } = useAuth()
  const toast = useToast()
  const [progress, setProgress] = useState([])
  const [leaderboard, setLeaderboard] = useState([])
  const [editingName, setEditingName] = useState(false)
  const [nameValue, setNameValue] = useState('')
  const [savingName, setSavingName] = useState(false)

  const startEditName = () => {
    setNameValue(user?.name || '')
    setEditingName(true)
  }

  const cancelEditName = () => {
    setNameValue('')
    setEditingName(false)
  }

  const saveName = async () => {
    const trimmed = nameValue.trim()
    if (!trimmed) {
      toast.error('El nombre no puede estar vacio')
      return
    }
    setSavingName(true)
    try {
      const data = await api.updateProfile({ name: trimmed })
      if (data?.token) localStorage.setItem('computaquest_token', data.token)
      updateUser({ name: trimmed })
      setEditingName(false)
      toast.success('Nombre actualizado correctamente')
    } catch {
      toast.error('Error al actualizar el nombre')
    } finally {
      setSavingName(false)
    }
  }

  const loadData = useCallback(async () => {
    try {
      const [progressData, leaderboardData] = await Promise.all([
        api.getProgress(),
        api.getLeaderboard()
      ])
      setProgress(progressData)
      setLeaderboard(leaderboardData)
    } catch {
      toast.error('Error al cargar datos del dashboard')
    }
  }, [toast])

  useEffect(() => {
    loadData()
  }, [loadData])

  const completedCount = progress.filter(p => p.completed).length

  return (
    <div className="dashboard-container">
      <h1 className="page-title">
        <i className="fa-solid fa-house"></i> Dashboard
      </h1>

      <div className="dashboard-grid">
        <div className="dashboard-main">
          <div className="profile-card glass-panel">
            <img
              src={`https://api.dicebear.com/7.x/bottts/svg?seed=${user?.avatar || 'avatar1'}`}
              alt="Avatar"
              className="profile-avatar"
            />
            <div className="profile-info">
              {editingName ? (
                <div className="name-edit-row">
                  <input
                    type="text"
                    value={nameValue}
                    onChange={e => setNameValue(e.target.value)}
                    maxLength={50}
                    autoFocus
                    onKeyDown={e => {
                      if (e.key === 'Enter') saveName()
                      if (e.key === 'Escape') cancelEditName()
                    }}
                  />
                  <button
                    className="btn-icon"
                    onClick={saveName}
                    disabled={savingName}
                    title="Guardar"
                  >
                    <i className={`fa-solid ${savingName ? 'fa-spinner fa-spin' : 'fa-check'}`}></i>
                  </button>
                  <button
                    className="btn-icon"
                    onClick={cancelEditName}
                    disabled={savingName}
                    title="Cancelar"
                  >
                    <i className="fa-solid fa-xmark"></i>
                  </button>
                </div>
              ) : (
                <div className="name-row">
                  <h2>{user?.name}</h2>
                  <button
                    className="btn-icon"
                    onClick={startEditName}
                    title="Editar nombre"
                  >
                    <i className="fa-solid fa-pen"></i>
                  </button>
                </div>
              )}
              <p className="profile-email">{user?.email}</p>
              <div className="profile-stats">
                <div className="stat">
                  <i className="fa-solid fa-layer-group"></i>
                  <span>Nv. {user?.level || 1}</span>
                </div>
                <div className="stat">
                  <i className="fa-solid fa-star"></i>
                  <span>{user?.xp || 0} XP</span>
                </div>
                <div className="stat">
                  <i className="fa-solid fa-coins"></i>
                  <span>{user?.points || 0} pts</span>
                </div>
                <div className="stat">
                  <i className="fa-solid fa-fire"></i>
                  <span>{user?.streak || 1} racha</span>
                </div>
              </div>
            </div>
          </div>

          <div className="badges-card glass-panel">
            <h3><i className="fa-solid fa-medal"></i> Insignias</h3>
            <div className="badges-grid">
              {user?.badges?.length > 0 ? (
                user.badges.map((badge, i) => (
                  <div key={i} className="badge-item">
                    <i className="fa-solid fa-award"></i>
                    <span>{badge}</span>
                  </div>
                ))
              ) : (
                <p className="badges-empty">Aun no tienes insignias. ¡Completa retos!</p>
              )}
            </div>
          </div>

          <div className="modules-card glass-panel">
            <h3><i className="fa-solid fa-gamepad"></i> Modulos de Aprendizaje</h3>
            <p className="modules-subtitle">Progreso: {completedCount} retos completados</p>
            <div className="modules-grid">
              {MODULES.map(mod => (
                <Link
                  key={mod.id}
                  to={`/challenges?type=${mod.id}`}
                  className="module-card"
                  style={{ '--module-color': mod.color }}
                >
                  <i className={`fa-solid ${mod.icon}`}></i>
                  <span>{mod.label}</span>
                </Link>
              ))}
            </div>
          </div>
        </div>

        <div className="dashboard-sidebar">
          <RadarChart progress={progress} />

          <div className="mini-leaderboard glass-panel">
            <h3><i className="fa-solid fa-trophy"></i> Top 5</h3>
            {leaderboard.slice(0, 5).map((entry, i) => (
              <div key={entry.userId} className="leaderboard-mini-entry">
                <span className="entry-pos">{i + 1}</span>
                <img
                  src={`https://api.dicebear.com/7.x/bottts/svg?seed=${entry.avatar}`}
                  alt={entry.name}
                />
                <span className="entry-name">{entry.name}</span>
                <span className="entry-pts">{entry.points} pts</span>
              </div>
            ))}
            <Link to="/challenges" className="btn-secondary btn-full">
              <i className="fa-solid fa-gamepad"></i> Ver Retos
            </Link>
          </div>
        </div>
      </div>
    </div>
  )
}
