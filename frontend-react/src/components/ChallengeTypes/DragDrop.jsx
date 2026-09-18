import { useState } from 'react'

export default function DragDrop({ content, onComplete }) {
  const [items, setItems] = useState(() => {
    if (!content?.items) return []
    return [...content.items].sort(() => Math.random() - 0.5)
  })
  const [draggedItem, setDraggedItem] = useState(null)

  const handleDragStart = (e, item) => {
    setDraggedItem(item)
    e.dataTransfer.effectAllowed = 'move'
  }

  const handleDragOver = (e, index) => {
    e.preventDefault()
    e.dataTransfer.dropEffect = 'move'
  }

  const handleDrop = (e, index) => {
    e.preventDefault()
    if (!draggedItem) return

    const newItems = [...items]
    const dragIndex = newItems.findIndex(i => i.id === draggedItem.id)
    const dropIndex = index

    newItems.splice(dragIndex, 1)
    newItems.splice(dropIndex, 0, draggedItem)

    setItems(newItems)
    setDraggedItem(null)
  }

  const handleCheck = () => {
    const correctOrder = content?.correctOrder || []
    const userOrder = items.map(i => i.id)
    const isCorrect = JSON.stringify(correctOrder) === JSON.stringify(userOrder)
    const score = isCorrect ? 100 : Math.floor((correctOrder.filter((id, i) => id === userOrder[i]).length / correctOrder.length) * 100)
    onComplete(score)
  }

  return (
    <div className="drag-drop-challenge">
      <h4><i className="fa-solid fa-arrows-up-down"></i> Ordena los elementos</h4>
      <p className="challenge-instruction">Arrastra y suelta para poner en el orden correcto:</p>

      <div className="drag-list">
        {items.map((item, index) => (
          <div
            key={item.id}
            className={`drag-item ${draggedItem?.id === item.id ? 'dragging' : ''}`}
            draggable
            onDragStart={(e) => handleDragStart(e, item)}
            onDragOver={(e) => handleDragOver(e, index)}
            onDrop={(e) => handleDrop(e, index)}
          >
            <span className="drag-number">{index + 1}</span>
            <span className="drag-text">{item.text}</span>
            <i className="fa-solid fa-grip-vertical drag-handle"></i>
          </div>
        ))}
      </div>

      <button className="btn-primary" onClick={handleCheck}>
        <i className="fa-solid fa-check"></i> Verificar Orden
      </button>
    </div>
  )
}
