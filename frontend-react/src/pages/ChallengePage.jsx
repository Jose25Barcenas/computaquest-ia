import { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import api from '../services/api'
import { useAuth } from '../context/AuthContext'
import { useToast } from '../context/ToastContext'
import DragDrop from '../components/ChallengeTypes/DragDrop'
import Quiz from '../components/ChallengeTypes/Quiz'
import MultipleSelect from '../components/ChallengeTypes/MultipleSelect'
import Chat from '../components/Chat/Chat'
import { TYPE_INFO } from '../constants'

export default function ChallengePage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const { user, updateUser } = useAuth()
  const toast = useToast()
  const [challenge, setChallenge] = useState(null)
  const [loading, setLoading] = useState(true)
  const [completed, setCompleted] = useState(false)
  const [result, setResult] = useState(null)

  useEffect(() => {
    loadChallenge()
  }, [id])

  const loadChallenge = async () => {
    try {
      const data = await api.getChallenge(id)
      setChallenge(data)
    } catch (error) {
      toast.error('Error al cargar el reto')
      navigate('/challenges')
    } finally {
      setLoading(false)
    }
  }

  const handleComplete = async (score) => {
    try {
      const data = await api.completeChallenge({ challengeId: id, score })
      setCompleted(true)
      setResult(data)

      const xpEarned = score >= 70 ? (challenge?.xpReward || 100) : Math.floor((challenge?.xpReward || 100) / 2)
      const ptsEarned = score >= 70 ? (challenge?.pointsReward || 10) : Math.floor((challenge?.pointsReward || 10) / 2)

      if (score >= 70) {
        toast.success(`¡Reto completado! +${xpEarned} XP, +${ptsEarned} pts`)
      } else {
        toast.info(`Puntuacion: ${score}%. Necesitas 70% para pasar. +${xpEarned} XP, +${ptsEarned} pts`)
      }

      updateUser({
        xp: (user.xp || 0) + xpEarned,
        points: (user.points || 0) + ptsEarned,
      })
    } catch (error) {
      toast.error(error.message || 'Error al guardar progreso')
    }
  }

  if (loading) return <div className="loading-screen"><div className="spinner"></div></div>
  if (!challenge) return null

  const renderChallenge = () => {
    const content = challenge.content

    switch (content?.type) {
      case 'drag-drop':
        return <DragDrop content={content} onComplete={handleComplete} />
      case 'quiz':
        return <Quiz content={content} onComplete={handleComplete} />
      case 'multiple-select':
        return <MultipleSelect content={content} onComplete={handleComplete} />
      default:
        return <p>Tipo de reto no soportado</p>
    }
  }

  const xpEarned = result?.score >= 70 ? (challenge?.xpReward || 100) : Math.floor((challenge?.xpReward || 100) / 2)
  const ptsEarned = result?.score >= 70 ? (challenge?.pointsReward || 10) : Math.floor((challenge?.pointsReward || 10) / 2)

  const info = TYPE_INFO[challenge.type] || {}

  return (
    <div className="challenge-page-container">
      <div className="challenge-main">
        <button className="btn-secondary" onClick={() => navigate('/challenges')}>
          <i className="fa-solid fa-arrow-left"></i> Volver
        </button>

        <div className="challenge-detail glass-panel">
          <div className="challenge-detail-header">
            <div className="challenge-icon-lg" style={{ background: info.color }}>
              <i className={`fa-solid ${info.icon}`}></i>
            </div>
            <div>
              <span className="challenge-type-badge" style={{ color: info.color }}>{info.label}</span>
              <h1>{challenge.title}</h1>
            </div>
          </div>
          <p className="challenge-description">{challenge.description}</p>
          <div className="challenge-meta">
            <span><i className="fa-solid fa-star"></i> {challenge.xpReward} XP</span>
            <span><i className="fa-solid fa-coins"></i> {challenge.pointsReward} pts</span>
            {challenge.badgeName && <span><i className="fa-solid fa-award"></i> {challenge.badgeName}</span>}
          </div>
        </div>

        {!completed ? (
          <div className="challenge-workspace glass-panel">
            {renderChallenge()}
          </div>
        ) : (
          <div className="challenge-completed glass-panel">
            <i className="fa-solid fa-circle-check"></i>
            <h2>¡Reto Completado!</h2>
            <p>Puntuacion: {result?.score || 0}%</p>
            <p>+{xpEarned} XP, +{ptsEarned} pts</p>
            <button className="btn-primary" onClick={() => navigate('/challenges')}>
              <i className="fa-solid fa-gamepad"></i> Seguir Jugando
            </button>
          </div>
        )}
      </div>

      <div className="challenge-sidebar">
        <Chat />
      </div>
    </div>
  )
}
