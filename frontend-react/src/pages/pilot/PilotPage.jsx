import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import api from '../../services/api'
import { TEST_QUESTIONS, MOTIVATION_ITEMS, LIKERT_OPTIONS, PILLAR_LABELS } from './pilotInstrument'

const CODE_KEY = 'pilot_code'

export default function PilotPage() {
  const [code, setCode] = useState(() => localStorage.getItem(CODE_KEY) || '')
  const [codeError, setCodeError] = useState('')
  const [session, setSession] = useState(null)
  const [current, setCurrent] = useState('code') // code | hub | test | motivation
  const [answers, setAnswers] = useState({})
  const [startedAt, setStartedAt] = useState(null)
  const [openAnswer, setOpenAnswer] = useState('')
  const [loading, setLoading] = useState(false)
  const [notice, setNotice] = useState('')
  const [booting, setBooting] = useState(() => Boolean(localStorage.getItem(CODE_KEY)))
  const navigate = useNavigate()

  useEffect(() => {
    const saved = localStorage.getItem(CODE_KEY)
    if (!saved) {
      setBooting(false)
      return
    }
    let cancelled = false
    api.pilotGetSession(saved)
      .then((data) => {
        if (!cancelled) {
          setSession(data)
          setCurrent('hub')
        }
      })
      .catch(() => {
        if (!cancelled) localStorage.removeItem(CODE_KEY)
      })
      .finally(() => {
        if (!cancelled) setBooting(false)
      })
    return () => { cancelled = true }
  }, [])

  async function enterCode(e) {
    e.preventDefault()
    const clean = code.trim().toUpperCase()
    if (!/^P\d{2}$/.test(clean)) {
      setCodeError('El código tiene el formato P01, P02, etc.')
      return
    }
    setCodeError('')
    setLoading(true)
    setNotice('')
    try {
      const data = await api.pilotGetSession(clean)
      localStorage.setItem(CODE_KEY, clean)
      setSession(data)
      setCurrent('hub')
    } catch (err) {
      setCodeError(err.message)
    } finally {
      setLoading(false)
    }
  }

  function changeCode() {
    localStorage.removeItem(CODE_KEY)
    setSession(null)
    setCode('')
    setCurrent('code')
  }

  function startTest() {
    setAnswers({})
    setStartedAt(new Date().toISOString())
    setCurrent('test')
  }

  async function submitTest(type) {
    setLoading(true)
    setNotice('')
    try {
      const updated = await api.pilotSubmitTest({
        code: session.code,
        type,
        answers,
        startedAt,
        accountEmail: `pilot-${session.code.toLowerCase()}@piloto.local`,
      })
      setSession(updated)
      setCurrent('hub')
      setNotice(type === 'pre'
        ? '¡Pretest registrado! Ahora completa los retos y luego vuelve por el postest.'
        : '¡Postest registrado! Ya puedes responder el cuestionario de motivación.')
    } catch (err) {
      setNotice(err.message)
    } finally {
      setLoading(false)
    }
  }

  async function submitMotivation(e) {
    e.preventDefault()
    setLoading(true)
    setNotice('')
    try {
      const updated = await api.pilotSubmitMotivation({
        code: session.code,
        answers,
        openAnswer,
      })
      setSession(updated)
      setCurrent('hub')
      setNotice('¡Listo! Gracias por participar. Tu proceso está completo.')
    } catch (err) {
      setNotice(err.message)
    } finally {
      setLoading(false)
    }
  }

  const allTestAnswered = TEST_QUESTIONS.every(q => answers[q.id])
  const allMotivationAnswered = MOTIVATION_ITEMS.every(q => answers[q.id])

  if (booting) {
    return <div className="loading-screen"><div className="spinner"></div></div>
  }

  if (current === 'code') {
    return (
      <div className="survey-container animate-fade">
        <h2><i className="fa-solid fa-flask"></i> Estudio Piloto</h2>
        <p className="survey-subtitle">Ingresa el código anónimo que te fue asignado (P01, P02…). No se pide nombre ni correo.</p>
        <form onSubmit={enterCode} className="glass-panel" style={{ padding: '2rem' }}>
          <label style={{ display: 'block', marginBottom: '0.5rem' }}>Código de participante</label>
          <input
            value={code}
            onChange={(e) => setCode(e.target.value.toUpperCase())}
            placeholder="P01"
            maxLength={3}
            style={{ width: '100%', marginBottom: '1rem' }}
          />
          {codeError && <p style={{ color: '#f87171', marginBottom: '1rem' }}>{codeError}</p>}
          <button className="btn-primary" disabled={loading} style={{ width: '100%' }}>
            {loading ? <i className="fa-solid fa-spinner fa-spin"></i> : <i className="fa-solid fa-arrow-right"></i>} Continuar
          </button>
        </form>
      </div>
    )
  }

  if (current === 'hub') {
    return (
      <div className="survey-container animate-fade">
        <h2><i className="fa-solid fa-user-check"></i> Participante {session.code}</h2>
        <p className="survey-subtitle">Sigue los pasos en orden. Tu avance se guarda con tu código.</p>
        {notice && <div className="glass-panel" style={{ padding: '1rem', marginBottom: '1.5rem', borderLeft: '3px solid #34d399' }}>{notice}</div>}

        <div className="glass-panel" style={{ padding: '1.2rem', marginBottom: '1rem' }}>
          <p><strong>1. Pretest</strong></p>
          {session.pretest
            ? <p style={{ color: '#34d399' }}>Completado · Puntaje total: <strong>{session.pretest.totalScore}/60</strong></p>
            : <button className="btn-primary" onClick={startTest}><i className="fa-solid fa-play"></i> Iniciar pretest</button>}
          {session.pretest && <button className="btn-secondary" style={{ marginLeft: '0.5rem' }} onClick={startTest}>Repetir</button>}
        </div>

        <div className="glass-panel" style={{ padding: '1.2rem', marginBottom: '1rem' }}>
          <p><strong>2. Retos del juego</strong></p>
          <p style={{ marginBottom: '0.8rem' }}>Crea tu cuenta con el correo <code>pilot-{session.code.toLowerCase()}@piloto.local</code> y completa los retos y el chat con el asistente.</p>
          <button className="btn-secondary" onClick={() => navigate('/challenges')}>
            <i className="fa-solid fa-gamepad"></i> Ir a los retos
          </button>
        </div>

        <div className="glass-panel" style={{ padding: '1.2rem', marginBottom: '1rem' }}>
          <p><strong>3. Postest</strong></p>
          {session.postest
            ? <p style={{ color: '#34d399' }}>Completado · Puntaje total: <strong>{session.postest.totalScore}/60</strong></p>
            : <button className="btn-primary" onClick={startTest} disabled={!session.pretest}>
                <i className="fa-solid fa-flag-checkered"></i> Iniciar postest
              </button>}
          {session.postest && <button className="btn-secondary" style={{ marginLeft: '0.5rem' }} onClick={startTest}>Repetir</button>}
        </div>

        <div className="glass-panel" style={{ padding: '1.2rem' }}>
          <p><strong>4. Motivación</strong></p>
          {session.motivationSubmittedAt
            ? <p style={{ color: '#34d399' }}>Completado. ¡Gracias por participar!</p>
            : <button className="btn-primary" onClick={() => { setAnswers({}); setCurrent('motivation') }} disabled={!session.postest}>
                <i className="fa-solid fa-heart"></i> Responder motivación
              </button>}
        </div>

        <div style={{ textAlign: 'center', marginTop: '1.5rem' }}>
          <button className="btn-secondary" onClick={changeCode}>
            <i className="fa-solid fa-right-from-bracket"></i> Cambiar de código
          </button>
        </div>
      </div>
    )
  }

  if (current === 'motivation') {
    return (
      <div className="survey-container animate-fade">
        <h2><i className="fa-solid fa-heart"></i> Cuestionario de motivación</h2>
        <p className="survey-subtitle">Responde según tu experiencia usando la aplicación.</p>
        <form onSubmit={submitMotivation}>
          <div className="survey-questions">
            {MOTIVATION_ITEMS.map(item => (
              <div key={item.id} className="survey-question glass-panel">
                <p className="question-text"><strong>{item.id}.</strong> {item.text}</p>
                <div className="survey-options">
                  {LIKERT_OPTIONS.map(opt => (
                    <label key={opt.value} className={`survey-option ${answers[item.id] === opt.value ? 'selected' : ''}`}>
                      <input
                        type="radio"
                        name={`mot${item.id}`}
                        checked={answers[item.id] === opt.value}
                        onChange={() => setAnswers({ ...answers, [item.id]: opt.value })}
                      />
                      <span>{opt.label}</span>
                    </label>
                  ))}
                </div>
              </div>
            ))}
            <div className="survey-question glass-panel">
              <p className="question-text"><strong>7.</strong> ¿Qué mejorarías de la aplicación?</p>
              <textarea
                value={openAnswer}
                onChange={(e) => setOpenAnswer(e.target.value)}
                maxLength={500}
                rows={3}
                placeholder="Escribe tu sugerencia (opcional)"
                style={{ width: '100%', marginTop: '0.5rem', padding: '0.6rem', borderRadius: 8, background: 'rgba(0,0,0,0.2)', color: 'inherit', border: '1px solid rgba(255,255,255,0.15)' }}
              />
            </div>
          </div>
          <div className="survey-actions">
            <button type="button" className="btn-secondary" onClick={() => setCurrent('hub')}>Volver</button>
            <button className="btn-primary" disabled={!allMotivationAnswered || loading}>
              {loading ? <i className="fa-solid fa-spinner fa-spin"></i> : <i className="fa-solid fa-paper-plane"></i>} Enviar
            </button>
          </div>
        </form>
      </div>
    )
  }

  const type = !session.pretest ? 'pre' : 'post'

  return (
    <div className="survey-container animate-fade">
      <h2><i className="fa-solid fa-clipboard-list"></i> {type === 'pre' ? 'Pretest' : 'Postest'} de pensamiento computacional</h2>
      <p className="survey-subtitle">Responde según cómo te sientes ahora. No hay respuestas correctas o incorrectas.</p>
      {notice && <div className="glass-panel" style={{ padding: '1rem', marginBottom: '1rem', borderLeft: '3px solid #f87171' }}>{notice}</div>}

      <div className="survey-progress">
        <div className="survey-progress-bar" style={{ width: `${(Object.keys(answers).length / TEST_QUESTIONS.length) * 100}%` }}></div>
      </div>
      <p className="survey-progress-text">{Object.keys(answers).length} / {TEST_QUESTIONS.length} respondidas</p>

      <div className="survey-questions">
        {TEST_QUESTIONS.map(q => (
          <div key={q.id} className="survey-question glass-panel">
            <p className="question-text"><strong>{q.id}.</strong> {q.text}</p>
            <p className="question-dimension"><small>{PILLAR_LABELS[q.pillar]}</small></p>
            <div className="survey-options">
              {LIKERT_OPTIONS.map(opt => (
                <label key={opt.value} className={`survey-option ${answers[q.id] === opt.value ? 'selected' : ''}`}>
                  <input
                    type="radio"
                    name={`q${q.id}`}
                    checked={answers[q.id] === opt.value}
                    onChange={() => setAnswers({ ...answers, [q.id]: opt.value })}
                  />
                  <span>{opt.label}</span>
                </label>
              ))}
            </div>
          </div>
        ))}
      </div>

      <div className="survey-actions">
        <button className="btn-secondary" onClick={() => setCurrent('hub')}>Volver</button>
        <button className="btn-primary" disabled={!allTestAnswered || loading} onClick={() => submitTest(type)}>
          {loading ? <i className="fa-solid fa-spinner fa-spin"></i> : <i className="fa-solid fa-paper-plane"></i>} Enviar {type === 'pre' ? 'Pretest' : 'Postest'}
        </button>
      </div>
    </div>
  )
}
