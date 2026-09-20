import { useState, useEffect } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { useToast } from '../context/ToastContext'
import api from '../services/api'
import Leaderboard from '../components/Leaderboard/Leaderboard'

export default function ChallengesPage() {
  const [challenges, setChallenges] = useState([])
  const [searchParams, setSearchParams] = useSearchParams()
  const typeFilter = searchParams.get('type')
  const toast = useToast()

  useEffect(() => {
    loadChallenges()
  }, [typeFilter])

  const loadChallenges = async () => {
    try {
      const params = typeFilter ? { type: typeFilter } : {}
      const data = await api.getChallenges(params)
      setChallenges(data)
    } catch (error) {
      toast.error('Error al cargar retos')
    }
  }

  const typeInfo = {
    decomposition: { icon: 'fa-puzzle-piece', color: '#6366f1', label: 'Descomposicion' },
    patterns: { icon: 'fa-magnifying-glass', color: '#10b981', label: 'Patrones' },
    abstraction: { icon: 'fa-filter', color: '#f59e0b', label: 'Abstraccion' },
    algorithms: { icon: 'fa-code', color: '#ef4444', label: 'Algoritmos' },
  }

  const difficultyStars = (n) => {
    return Array.from({ length: 5 }, (_, i) => (
      <i key={i} className={`fa-solid fa-star ${i < n ? 'active' : ''}`}></i>
    ))
  }

  return (
    <div className="challenges-container">
      <h1 className="page-title">
        <i className="fa-solid fa-gamepad"></i>
        {typeFilter ? typeInfo[typeFilter]?.label || 'Retos' : 'Todos los Retos'}
      </h1>

      {typeFilter && (
        <button className="btn-secondary" onClick={() => setSearchParams({})}>
          <i className="fa-solid fa-xmark"></i> Limpiar filtro
        </button>
      )}

      <div className="challenges-layout">
        <div className="challenges-grid">
          {challenges.map(challenge => {
            const info = typeInfo[challenge.type] || {}
            return (
              <Link
                key={challenge.id}
                to={`/challenge/${challenge.id}`}
                className="challenge-card glass-panel"
                style={{ '--card-accent': info.color }}
              >
                <div className="challenge-header">
                  <div className="challenge-icon" style={{ background: info.color }}>
                    <i className={`fa-solid ${info.icon}`}></i>
                  </div>
                  <div className="challenge-type">{info.label}</div>
                </div>
                <h3>{challenge.title}</h3>
                <p>{challenge.description}</p>
                <div className="challenge-footer">
                  <div className="challenge-difficulty">
                    {difficultyStars(challenge.difficulty)}
                  </div>
                  <div className="challenge-rewards">
                    <span><i className="fa-solid fa-star"></i> {challenge.xpReward} XP</span>
                    <span><i className="fa-solid fa-coins"></i> {challenge.pointsReward} pts</span>
                  </div>
                </div>
                {challenge.badgeName && (
                  <div className="challenge-badge">
                    <i className="fa-solid fa-award"></i> {challenge.badgeName}
                  </div>
                )}
              </Link>
            )
          })}

          {challenges.length === 0 && (
            <div className="challenges-empty">
              <i className="fa-solid fa-gamepad"></i>
              <p>No hay retos disponibles</p>
            </div>
          )}
        </div>

        <div className="challenges-sidebar">
          <Leaderboard />
        </div>
      </div>
    </div>
  )
}
