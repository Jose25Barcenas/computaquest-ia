import { useState } from 'react'

export default function DragDrop({ content, onComplete }) {
  const [items, setItems] = useState(() => {
    if (!content?.items) return []
    const shuffled = [...content.items]
    for (let i = shuffled.length - 1; i > 0; i--) {
      const j = Math.floor(Math.random() * (i + 1));
      [shuffled[i], shuffled[j]] = [shuffled[j], shuffled[i]]
    }
    return shuffled
  })
  const [draggedItem, setDraggedItem] = useState(null)

  const handleDragStart = (e, item) => {
    setDraggedItem(item)
    e.dataTransfer.effectAllowed = 'move'
    if (e.dataTransfer.setData) {
      e.dataTransfer.setData('text/plain', item.id)
    }
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

  const handleTouchStart = (e, item) => {
    setDraggedItem(item)
  }

  const handleTouchEnd = (e, index) => {
    if (!draggedItem) return
    const touch = e.changedTouches[0]
    const target = document.elementFromPoint(touch.clientX, touch.clientY)
    const dropTarget = target?.closest('.drag-item')
    if (dropTarget) {
      const dropIndex = parseInt(dropTarget.dataset.index, 10)
      if (!isNaN(dropIndex)) {
        const newItems = [...items]
        const dragIdx = newItems.findIndex(i => i.id === draggedItem.id)
        newItems.splice(dragIdx, 1)
        newItems.splice(dropIndex, 0, draggedItem)
        setItems(newItems)
      }
    }
    setDraggedItem(null)
  }

  const handleCheck = () => {
    const userOrder = items.map(i => i.id)
    onComplete(0, userOrder)
  }

  return (
    <div className="drag-drop-challenge">
      <h4><i className="fa-solid fa-arrows-up-down"></i> Ordena los elementos</h4>
      <p className="challenge-instruction">Arrastra y suelta para poner en el orden correcto:</p>

      <div className="drag-list">
        {items.map((item, index) => (
          <div
            key={item.id}
            data-index={index}
            className={`drag-item ${draggedItem?.id === item.id ? 'dragging' : ''}`}
            draggable
            onDragStart={(e) => handleDragStart(e, item)}
            onDragOver={(e) => handleDragOver(e, index)}
            onDrop={(e) => handleDrop(e, index)}
            onTouchStart={(e) => handleTouchStart(e, item)}
            onTouchEnd={(e) => handleTouchEnd(e, index)}
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
