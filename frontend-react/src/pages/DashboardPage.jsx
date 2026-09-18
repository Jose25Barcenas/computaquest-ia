import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { useToast } from '../context/ToastContext'
import api from '../services/api'
import RadarChart from '../components/RadarChart/RadarChart'

export default function DashboardPage() {
  const { user } = useAuth()
  const toast = useToast()
  const [progress, setProgress] = useState([])
  const [leaderboard, setLeaderboard] = useState([])

  useEffect(() => {
    loadData()
  }, [])

  const loadData = async () => {
    try {
      const [progressData, leaderboardData] = await Promise.all([
        api.getProgress(),
        api.getLeaderboard()
      ])
      setProgress(progressData)
      setLeaderboard(leaderboardData)
    } catch (error) {
      toast.error('Error al cargar datos del dashboard')
    }
  }

  const modules = [
    { id: 'DECOMPOSITION', icon: 'fa-puzzle-piece', title: 'Descomposicion', color: '#6366f1' },
    { id: 'PATTERNS', icon: 'fa-magnifying-glass', title: 'Reconocimiento de Patrones', color: '#10b981' },
    { id: 'ABSTRACTION', icon: 'fa-filter', title: 'Abstraccion', color: '#f59e0b' },
    { id: 'ALGORITHMS', icon: 'fa-code', title: 'Arquitectura de Algoritmos', color: '#ef4444' },
  ]

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
              <h2>{user?.name}</h2>
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
              {modules.map(mod => (
                <Link
                  key={mod.id}
                  to={`/challenges?type=${mod.id}`}
                  className="module-card"
                  style={{ '--module-color': mod.color }}
                >
                  <i className={`fa-solid ${mod.icon}`}></i>
                  <span>{mod.title}</span>
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
