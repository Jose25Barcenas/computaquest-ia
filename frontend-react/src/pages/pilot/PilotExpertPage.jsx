import { useState } from 'react'
import api from '../../services/api'
import { EXPERT_CRITERIA, EXPERT_VERDICTS, LIKERT_OPTIONS } from './pilotInstrument'

export default function PilotExpertPage() {
  const [reviewerCode, setReviewerCode] = useState('DOCENTE')
  const [scores, setScores] = useState({})
  const [comments, setComments] = useState('')
  const [verdict, setVerdict] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [done, setDone] = useState(false)

  async function handleSubmit(e) {
    e.preventDefault()
    setLoading(true)
    setError('')
    try {
      await api.pilotSubmitExpert({ reviewerCode, scores, comments, verdict })
      setDone(true)
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  if (done) {
    return (
      <div className="survey-container animate-fade">
        <div className="survey-result glass-panel" style={{ padding: '3rem' }}>
          <i className="fa-solid fa-circle-check" style={{ fontSize: '3rem', color: '#34d399' }}></i>
          <h2>Validación registrada</h2>
          <p className="survey-subtitle">Gracias, docente. Tu evaluación quedó guardada.</p>
        </div>
      </div>
    )
  }

  const allScored = EXPERT_CRITERIA.every(c => scores[c.key])

  return (
    <div className="survey-container animate-fade">
      <h2><i className="fa-solid fa-clipboard-check"></i> Validación por experto</h2>
      <p className="survey-subtitle">Docente de tecnología: evalúa la aplicación ComputaQuest-IA (1 = muy bajo, 5 = muy alto).</p>

      <form onSubmit={handleSubmit}>
        <div className="glass-panel" style={{ padding: '1.2rem', marginBottom: '1rem' }}>
          <label style={{ display: 'block', marginBottom: '0.5rem' }}>Tu identificador (opcional)</label>
          <input
            value={reviewerCode}
            onChange={(e) => setReviewerCode(e.target.value)}
            maxLength={20}
            style={{ width: '100%' }}
          />
        </div>

        <div className="survey-questions">
          {EXPERT_CRITERIA.map(criterion => (
            <div key={criterion.key} className="survey-question glass-panel">
              <p className="question-text">{criterion.label}</p>
              <div className="survey-options">
                {LIKERT_OPTIONS.map(opt => (
                  <label key={opt.value} className={`survey-option ${scores[criterion.key] === opt.value ? 'selected' : ''}`}>
                    <input
                      type="radio"
                      name={criterion.key}
                      checked={scores[criterion.key] === opt.value}
                      onChange={() => setScores({ ...scores, [criterion.key]: opt.value })}
                    />
                    <span>{opt.value}. {opt.label}</span>
                  </label>
                ))}
              </div>
            </div>
          ))}

          <div className="survey-question glass-panel">
            <p className="question-text"><strong>Comentarios</strong> (opcional)</p>
            <textarea
              value={comments}
              onChange={(e) => setComments(e.target.value)}
              maxLength={1000}
              rows={4}
              placeholder="Sugerencias, observaciones o recomendaciones"
              style={{ width: '100%', marginTop: '0.5rem', padding: '0.6rem', borderRadius: 8, background: 'rgba(0,0,0,0.2)', color: 'inherit', border: '1px solid rgba(255,255,255,0.15)' }}
            />
          </div>

          <div className="survey-question glass-panel">
            <p className="question-text"><strong>Veredicto</strong></p>
            <div className="survey-options">
              {EXPERT_VERDICTS.map(v => (
                <label key={v.value} className={`survey-option ${verdict === v.value ? 'selected' : ''}`}>
                  <input
                    type="radio"
                    name="verdict"
                    checked={verdict === v.value}
                    onChange={() => setVerdict(v.value)}
                  />
                  <span>{v.label}</span>
                </label>
              ))}
            </div>
          </div>
        </div>

        {error && <p style={{ color: '#f87171', marginBottom: '1rem' }}>{error}</p>}

        <div className="survey-actions">
          <button className="btn-primary" disabled={!allScored || !verdict || loading}>
            {loading ? <i className="fa-solid fa-spinner fa-spin"></i> : <i className="fa-solid fa-paper-plane"></i>} Enviar validación
          </button>
        </div>
      </form>
    </div>
  )
}
