import { useEffect, useState } from 'react'

export default function Toast({ message, type = 'info' }) {
  const [visible, setVisible] = useState(false)

  useEffect(() => {
    requestAnimationFrame(() => setVisible(true))
    const timer = setTimeout(() => setVisible(false), 2500)
    return () => clearTimeout(timer)
  }, [])

  const icons = {
    success: 'fa-circle-check',
    error: 'fa-circle-xmark',
    info: 'fa-circle-info',
  }

  return (
    <div className={`toast toast-${type} ${visible ? 'toast-show' : 'toast-hide'}`}>
      <i className={`fa-solid ${icons[type] || icons.info}`}></i>
      <span>{message}</span>
    </div>
  )
}
