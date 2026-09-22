import { useState } from 'react'

export default function Quiz({ content, onComplete }) {
  const [currentQ, setCurrentQ] = useState(0)
  const [answers, setAnswers] = useState([])
  const [selected, setSelected] = useState(null)
  const [finished, setFinished] = useState(false)

  const questions = content?.questions || []
  const question = questions[currentQ]

  const handleAnswer = (answer) => {
    setSelected(answer)
  }

  const handleNext = () => {
    const newAnswers = [...answers, selected]
    setAnswers(newAnswers)
    setSelected(null)

    if (currentQ + 1 < questions.length) {
      setCurrentQ(currentQ + 1)
    } else {
      setFinished(true)
      onComplete(0, newAnswers)
    }
  }

  if (finished) {
    return (
      <div className="quiz-result">
        <i className="fa-solid fa-circle-check"></i>
        <h4>Quiz Completado!</h4>
        <p>Tu respuesta fue enviada al servidor para validacion.</p>
      </div>
    )
  }

  return (
    <div className="quiz-challenge">
      <div className="quiz-progress">
        <span>Pregunta {currentQ + 1} de {questions.length}</span>
        <div className="progress-bar">
          <div style={{ width: `${((currentQ + 1) / questions.length) * 100}%` }}></div>
        </div>
      </div>

      <h4 className="quiz-question">{question.q}</h4>

      <div className="quiz-options">
        {question.opts.map((opt, i) => (
          <button
            key={i}
            className={`quiz-option ${selected === opt ? 'selected' : ''}`}
            onClick={() => handleAnswer(opt)}
          >
            <span className="option-letter">{String.fromCharCode(65 + i)}</span>
            {opt}
          </button>
        ))}
      </div>

      <button
        className="btn-primary"
        onClick={handleNext}
        disabled={!selected}
      >
        {currentQ + 1 < questions.length ? (
          <>Siguiente <i className="fa-solid fa-arrow-right"></i></>
        ) : (
          <>Finalizar <i className="fa-solid fa-check"></i></>
        )}
      </button>
    </div>
  )
}
