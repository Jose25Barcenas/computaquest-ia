import { useState } from 'react'
import { useToast } from '../context/ToastContext'
import api from '../services/api'

const demographics = {
  age: [
    { value: 10, label: '10 anos' },
    { value: 11, label: '11 anos' },
    { value: 12, label: '12 anos' },
    { value: 13, label: '13 anos' },
    { value: 14, label: '14 anos' },
    { value: 15, label: '15 anos' },
  ],
  grade: [
    { value: '8vo', label: 'Octavo grado' },
    { value: '9no', label: 'Noveno grado' },
  ],
  gender: [
    { value: 'M', label: 'Masculino' },
    { value: 'F', label: 'Femenino' },
    { value: 'O', label: 'Otro / No especificar' },
  ],
}

// Preguntas basadas en investigaciones reales:
// - Technology Acceptance Model (TAM) - Davis, 1989
// - Computational Thinking Test - Korkmaz et al., 2017
// - Intrinsic Motivation Inventory - Ryan & Deci, 2000
const questions = [
  // Bloque 1: Motivacion hacia la Tecnologia (basado en TAM)
  {
    id: 1,
    text: 'Me motiva usar tecnologia para aprender cosas nuevas',
    dimension: 'motivacion_tecnologia',
    source: 'TAM - Perceived Usefulness',
  },
  {
    id: 2,
    text: 'Disfruto usar aplicaciones y juegos educativos',
    dimension: 'motivacion_tecnologia',
    source: 'TAM - Perceived Enjoyment',
  },
  {
    id: 3,
    text: 'La inteligencia artificial me genera curiosidad',
    dimension: 'motivacion_tecnologia',
    source: 'TAM - Attitude Toward Technology',
  },
  {
    id: 4,
    text: 'Quiero aprender a crear mis propias aplicaciones',
    dimension: 'motivacion_tecnologia',
    source: 'TAM - Behavioral Intention',
  },

  // Bloque 2: Percepcion del Pensamiento Computacional (basado en CT Test)
  {
    id: 5,
    text: 'Siento que puedo descomponer problemas complejos en partes pequenas',
    dimension: 'descomposicion',
    source: 'CT Test - Decomposition (Korkmaz et al., 2017)',
  },
  {
    id: 6,
    text: 'Reconozco patrones en los ejercicios y juegos que hago',
    dimension: 'reconocimiento_patrones',
    source: 'CT Test - Pattern Recognition',
  },
  {
    id: 7,
    text: 'Comprendo la importancia de seguir pasos ordenados para resolver problemas',
    dimension: 'algoritmos',
    source: 'CT Test - Algorithmic Thinking',
  },
  {
    id: 8,
    text: 'Me siento capaz de disenar algoritmos simples',
    dimension: 'algoritmos',
    source: 'CT Test - Algorithmic Thinking',
  },

  // Bloque 3: Actitud hacia el Aprendizaje (basado en IMI)
  {
    id: 9,
    text: 'Los retos y problemas me hacen sentir mas confianza',
    dimension: 'confianza',
    source: 'IMI - Perceived Competence',
  },
  {
    id: 10,
    text: 'Me gusta trabajar en equipo para encontrar soluciones',
    dimension: 'trabajo_equipo',
    source: 'IMI - Relatedness',
  },
  {
    id: 11,
    text: 'Cuando me equivoco, intento con otra estrategia',
    dimension: 'persistencia',
    source: 'IMI - Effort/Importance',
  },
  {
    id: 12,
    text: 'Quiero seguir aprendiendo sobre programacion y tecnologia',
    dimension: 'motivacion_aprendizaje',
    source: 'IMI - Interest/Enjoyment',
  },

  // Bloque 4: Abstraccion y Abstraccion (basado en CT Test)
  {
    id: 13,
    text: 'Puedo identificar lo mas importante de un problema',
    dimension: 'abstraccion',
    source: 'CT Test - Abstraction',
  },
  {
    id: 14,
    text: 'Entiendo que un mismo problema puede resolverse de diferentes maneras',
    dimension: 'pensamiento_critico',
    source: 'CT Test - Generalization',
  },
  {
    id: 15,
    text: 'Me siento capaz de explicar paso a paso como resolver un problema',
    dimension: 'comunicacion',
    source: 'CT Test - Communication',
  },
]

const likertOptions = [
  { value: 1, label: 'Totalmente en desacuerdo' },
  { value: 2, label: 'En desacuerdo' },
  { value: 3, label: 'Neutral' },
  { value: 4, label: 'De acuerdo' },
  { value: 5, label: 'Totalmente de acuerdo' },
]

export default function SurveyPage() {
  const toast = useToast()
  const [surveyType, setSurveyType] = useState(null)
  const [step, setStep] = useState('demographics') // demographics | questions | submitted
  const [demographicsData, setDemographicsData] = useState({
    age: '',
    grade: '',
    gender: '',
  })
  const [answers, setAnswers] = useState({})
  const [result, setResult] = useState(null)

  const handleDemographicChange = (field, value) => {
    setDemographicsData(prev => ({ ...prev, [field]: value }))
  }

  const handleAnswer = (questionId, value) => {
    setAnswers(prev => ({ ...prev, [questionId]: value }))
  }

  const allDemographicsFilled = demographicsData.age && demographicsData.grade && demographicsData.gender
  const allAnswered = questions.every(q => answers[q.id] !== undefined)
  const totalScore = Object.values(answers).reduce((a, b) => a + b, 0)

  const handleDemographicsSubmit = () => {
    if (allDemographicsFilled) {
      setStep('questions')
    }
  }

  const handleSubmit = async () => {
    try {
      const data = await api.submitSurvey({
        type: surveyType,
        demographics: demographicsData,
        answers,
      })
      setResult(data)
      setStep('submitted')
      toast.success('Encuesta enviada exitosamente')
    } catch (error) {
      toast.error(error.message || 'Error al enviar encuesta')
    }
  }

  const handleReset = () => {
    setSurveyType(null)
    setStep('demographics')
    setDemographicsData({ age: '', grade: '', gender: '' })
    setAnswers({})
    setResult(null)
  }

  // Paso 0: Seleccion de tipo
  if (!surveyType) {
    return (
      <div className="survey-container animate-fade">
        <h2><i className="fa-solid fa-clipboard-list"></i> Encuesta de Investigacion</h2>
        <p className="survey-subtitle">Plataforma ComputaQuest IA - Proyecto de Grado</p>
        <div className="survey-info glass-panel">
          <p><strong>Objetivo:</strong> Evaluar la motivacion y actitud hacia el pensamiento computacional en estudiantes de octavo y noveno grado.</p>
          <p><strong>Duracion:</strong> Aproximadamente 5-7 minutos.</p>
          <p><strong>Privacidad:</strong> Tus respuestas son anonimas y seran utilizadas solo con fines academicos.</p>
        </div>
        <div className="survey-type-grid">
          <button className="btn-primary survey-type-btn" onClick={() => setSurveyType('pre')}>
            <i className="fa-solid fa-play"></i>
            <span>Encuesta Pre</span>
            <small>Antes de iniciar los retos</small>
          </button>
          <button className="btn-primary survey-type-btn" onClick={() => setSurveyType('post')}>
            <i className="fa-solid fa-flag-checkered"></i>
            <span>Encuesta Post</span>
            <small>Despues de completar retos</small>
          </button>
        </div>
      </div>
    )
  }

  // Paso 2: Enviada
  if (step === 'submitted') {
    return (
      <div className="survey-container animate-fade">
        <div className="survey-result">
          <i className="fa-solid fa-circle-check" style={{ fontSize: '3rem', color: 'var(--success)' }}></i>
          <h2>Encuesta Enviada</h2>
          <p>Tipo: <strong>{surveyType === 'pre' ? 'Pre-intervencion' : 'Post-intervencion'}</strong></p>
          <p>Puntuacion total: <strong>{result?.totalScore || totalScore}</strong> / {questions.length * 5}</p>
          <div className="survey-demographics-summary glass-panel">
            <p><strong>Datos demograficos registrados:</strong></p>
            <p>Edad: {demographicsData.age} anos | Grado: {demographicsData.grade} | Genero: {demographicsData.gender === 'M' ? 'Masculino' : demographicsData.gender === 'F' ? 'Femenino' : 'Otro'}</p>
          </div>
          <p className="survey-note">Tu respuesta ha quedado registrada para el analisis de la investigacion.</p>
          <button className="btn-primary" onClick={handleReset}>
            Volver
          </button>
        </div>
      </div>
    )
  }

  // Paso 1: Datos demograficos
  if (step === 'demographics') {
    return (
      <div className="survey-container animate-fade">
        <h2>
          <i className="fa-solid fa-clipboard-list"></i>
          Encuesta {surveyType === 'pre' ? 'Pre-intervencion' : 'Post-intervencion'}
        </h2>
        <p className="survey-subtitle">Paso 1: Datos demograficos</p>

        <div className="demographics-form glass-panel">
          <div className="form-group">
            <label><i className="fa-solid fa-calendar"></i> Edad</label>
            <select
              value={demographicsData.age}
              onChange={(e) => handleDemographicChange('age', parseInt(e.target.value))}
            >
              <option value="">Selecciona tu edad</option>
              {demographics.age.map(opt => (
                <option key={opt.value} value={opt.value}>{opt.label}</option>
              ))}
            </select>
          </div>

          <div className="form-group">
            <label><i className="fa-solid fa-school"></i> Grado escolar</label>
            <select
              value={demographicsData.grade}
              onChange={(e) => handleDemographicChange('grade', e.target.value)}
            >
              <option value="">Selecciona tu grado</option>
              {demographics.grade.map(opt => (
                <option key={opt.value} value={opt.value}>{opt.label}</option>
              ))}
            </select>
          </div>

          <div className="form-group">
            <label><i className="fa-solid fa-user"></i> Genero</label>
            <select
              value={demographicsData.gender}
              onChange={(e) => handleDemographicChange('gender', e.target.value)}
            >
              <option value="">Selecciona tu genero</option>
              {demographics.gender.map(opt => (
                <option key={opt.value} value={opt.value}>{opt.label}</option>
              ))}
            </select>
          </div>
        </div>

        <div className="survey-actions">
          <button className="btn-secondary" onClick={handleReset}>
            Cancelar
          </button>
          <button className="btn-primary" disabled={!allDemographicsFilled} onClick={handleDemographicsSubmit}>
            <i className="fa-solid fa-arrow-right"></i> Continuar
          </button>
        </div>
      </div>
    )
  }

  // Paso 2: Preguntas
  return (
    <div className="survey-container animate-fade">
      <h2>
        <i className="fa-solid fa-clipboard-list"></i>
        Encuesta {surveyType === 'pre' ? 'Pre-intervencion' : 'Post-intervencion'}
      </h2>
      <p className="survey-subtitle">Paso 2: Responde segun tu experiencia actual</p>

      <div className="survey-progress">
        <div className="survey-progress-bar" style={{ width: `${(Object.keys(answers).length / questions.length) * 100}%` }}></div>
      </div>
      <p className="survey-progress-text">{Object.keys(answers).length} / {questions.length} respondidas</p>

      <div className="survey-questions">
        {questions.map(q => (
          <div key={q.id} className="survey-question glass-panel">
            <p className="question-text"><strong>{q.id}.</strong> {q.text}</p>
            <p className="question-dimension"><small>{q.dimension.replace(/_/g, ' ').toUpperCase()}</small></p>
            <div className="survey-options">
              {likertOptions.map(opt => (
                <label key={opt.value} className={`survey-option ${answers[q.id] === opt.value ? 'selected' : ''}`}>
                  <input
                    type="radio"
                    name={`q${q.id}`}
                    value={opt.value}
                    checked={answers[q.id] === opt.value}
                    onChange={() => handleAnswer(q.id, opt.value)}
                  />
                  <span>{opt.label}</span>
                </label>
              ))}
            </div>
          </div>
        ))}
      </div>

      <div className="survey-actions">
        <button className="btn-secondary" onClick={() => setStep('demographics')}>
          <i className="fa-solid fa-arrow-left"></i> Volver
        </button>
        <button className="btn-primary" disabled={!allAnswered} onClick={handleSubmit}>
          <i className="fa-solid fa-paper-plane"></i> Enviar Encuesta
        </button>
      </div>
    </div>
  )
}
