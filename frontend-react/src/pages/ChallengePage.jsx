import { useState, useEffect, useCallback } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import api from '../services/api'
import { useAuth } from '../context/AuthContext'
import { useToast } from '../context/ToastContext'
import DragDrop from '../components/ChallengeTypes/DragDrop'
import Quiz from '../components/ChallengeTypes/Quiz'
import MultipleSelect from '../components/ChallengeTypes/MultipleSelect'
import Chat from '../components/Chat/Chat'
import { getTypeInfo } from '../constants'

export default function ChallengePage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const { updateUser } = useAuth()
  const toast = useToast()
  const [challenge, setChallenge] = useState(null)
  const [loading, setLoading] = useState(true)
  const [completed, setCompleted] = useState(false)
  const [alreadyCompleted, setAlreadyCompleted] = useState(false)
  const [result, setResult] = useState(null)

  const loadChallenge = useCallback(async (signal) => {
    setLoading(true)
    setCompleted(false)
    setAlreadyCompleted(false)
    setResult(null)
    try {
      const [data, progressList] = await Promise.all([
        api.getChallenge(id, { signal }),
        api.getProgress({ signal }).catch(() => []),
      ])
      if (signal?.aborted) return
      setChallenge(data)

      const saved = Array.isArray(progressList)
        ? progressList.find(p => p.challengeId === id && p.completed)
        : null
      if (saved) {
        setCompleted(true)
        setAlreadyCompleted(true)
        setResult(saved)
      }
    } catch (err) {
      if (err?.name === 'AbortError') return
      toast.error('Error al cargar el reto')
      navigate('/challenges')
    } finally {
      if (!signal?.aborted) setLoading(false)
    }
  }, [id, navigate, toast])

  useEffect(() => {
    const controller = new AbortController()
    loadChallenge(controller.signal)
    return () => controller.abort()
  }, [loadChallenge])

  const handleComplete = async (clientScore, userAnswers) => {
    try {
      const data = await api.completeChallenge({
        challengeId: id,
        userAnswers: userAnswers || [],
      })
      setCompleted(true)
      setResult(data)

      const score = data.score ?? 0
      const xpEarned = data.xpEarned ?? 0
      const ptsEarned = data.pointsEarned ?? 0

      if (score >= 70) {
        toast.success(`¡Reto completado! +${xpEarned} XP, +${ptsEarned} pts`)
      } else {
        toast.info(`Puntuacion: ${score}%. Necesitas 70% para pasar.`)
      }
      if (data.levelUp) {
        toast.success(`¡Subiste a nivel ${data.userLevel}!`)
      }

      // El servidor es la fuente unica de XP/puntos/nivel
      updateUser({
        xp: data.userXp,
        points: data.userPoints,
        level: data.userLevel,
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

  const score = result?.score ?? 0

  const info = getTypeInfo(challenge.type)

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
            {alreadyCompleted ? (
              <>
                <h2>Reto ya completado</h2>
                <p>Tu mejor puntuacion: {score}%</p>
                <p>Ya no es posible repetirlo, pero puedes seguir con los demas retos.</p>
              </>
            ) : (
              <>
                <h2>¡Reto Completado!</h2>
                <p>Puntuacion: {score}%</p>
                <p>+{result?.xpEarned ?? 0} XP, +{result?.pointsEarned ?? 0} pts</p>
              </>
            )}
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
