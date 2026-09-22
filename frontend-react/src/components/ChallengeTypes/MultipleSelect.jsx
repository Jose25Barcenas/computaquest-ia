import { useState } from 'react'

export default function MultipleSelect({ content, onComplete }) {
  const [selected, setSelected] = useState([])

  const options = content?.options || []

  const toggleOption = (option) => {
    setSelected(prev =>
      prev.includes(option)
        ? prev.filter(o => o !== option)
        : [...prev, option]
    )
  }

  const handleCheck = () => {
    onComplete(0, selected)
  }

  return (
    <div className="multiple-select-challenge">
      <div className="scenario-box">
        {content?.scenarioIcon && <i className={`fa-solid ${content.scenarioIcon}`}></i>}
        <p>{content?.scenario}</p>
      </div>

      <div className="select-options">
        {options.map((opt, i) => (
          <button
            key={i}
            className={`select-option ${selected.includes(opt) ? 'selected' : ''}`}
            onClick={() => toggleOption(opt)}
          >
            <span className={`checkbox ${selected.includes(opt) ? 'checked' : ''}`}>
              {selected.includes(opt) && <i className="fa-solid fa-check"></i>}
            </span>
            {opt}
          </button>
        ))}
      </div>

      <p className="select-hint">
        Selecciona las opciones que consideres correctas
      </p>

      <button
        className="btn-primary"
        onClick={handleCheck}
        disabled={selected.length === 0}
      >
        <i className="fa-solid fa-check"></i> Verificar
      </button>
    </div>
  )
}
