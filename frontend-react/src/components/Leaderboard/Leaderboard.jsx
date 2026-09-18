import { useEffect, useState } from 'react'
import api from '../../services/api'
import { useAuth } from '../../context/AuthContext'
import { useToast } from '../../context/ToastContext'

export default function Leaderboard() {
  const [entries, setEntries] = useState([])
  const { user } = useAuth()
  const toast = useToast()

  useEffect(() => {
    loadLeaderboard()
  }, [])

  const loadLeaderboard = async () => {
    try {
      const data = await api.getLeaderboard()
      setEntries(data)
    } catch (error) {
      toast.error('Error al cargar leaderboard')
    }
  }

  const positionIcons = ['fa-crown', 'fa-medal', 'fa-award']

  return (
    <div className="leaderboard glass-panel">
      <h3><i className="fa-solid fa-trophy"></i> Leaderboard</h3>
      <div className="leaderboard-list">
        {entries.map((entry, i) => (
          <div
            key={entry.userId}
            className={`leaderboard-entry ${entry.userId === user?.id ? 'current-user' : ''}`}
          >
            <span className="entry-position">
              {i < 3 ? (
                <i className={`fa-solid ${positionIcons[i]}`}></i>
              ) : (
                <span className="position-number">{entry.position}</span>
              )}
            </span>
            <img
              src={`https://api.dicebear.com/7.x/bottts/svg?seed=${entry.avatar}`}
              alt={entry.name}
              className="entry-avatar"
            />
            <div className="entry-info">
              <span className="entry-name">{entry.name}</span>
              <span className="entry-level">Nv. {entry.level}</span>
            </div>
            <span className="entry-points">{entry.points} pts</span>
          </div>
        ))}

        {entries.length === 0 && (
          <div className="leaderboard-empty">
            <i className="fa-solid fa-ranking-star"></i>
            <p>Sé el primero en el leaderboard</p>
          </div>
        )}
      </div>
    </div>
  )
}
