/* eslint-env browser */
import { useState } from 'react'

export default function QRCode({ url, size = 200, title = 'Escanea para acceder' }) {
  const [copied, setCopied] = useState(false)

  const qrApiUrl = `https://api.qrserver.com/v1/create-qr-code/?size=${size}x${size}&data=${encodeURIComponent(url)}&format=png&margin=10`

  const handleCopy = async () => {
    try {
      await navigator.clipboard.writeText(url)
      setCopied(true)
      setTimeout(() => setCopied(false), 2000)
    } catch (err) {
      console.error('Error al copiar:', err)
    }
  }

  const handleDownload = async () => {
    try {
      const response = await fetch(qrApiUrl)
      const blob = await response.blob()
      const link = document.createElement('a')
      link.href = URL.createObjectURL(blob)
      link.download = 'computaquest-qr.png'
      link.click()
    } catch (err) {
      console.error('Error al descargar:', err)
    }
  }

  return (
    <div className="qr-container glass-panel">
      {title && <h3 className="qr-title">{title}</h3>}
      <div className="qr-image-wrapper">
        <img 
          src={qrApiUrl} 
          alt="Código QR de acceso a ComputaQuest" 
          className="qr-image"
          width={size}
          height={size}
        />
      </div>
      <p className="qr-url">{url}</p>
      <div className="qr-actions">
        <button className="btn-primary" onClick={handleCopy}>
          <i className={`fa-solid ${copied ? 'fa-check' : 'fa-copy'}`}></i>
          {copied ? 'Copiado!' : 'Copiar URL'}
        </button>
        <button className="btn-secondary" onClick={handleDownload}>
          <i className="fa-solid fa-download"></i>
          Descargar QR
        </button>
      </div>
    </div>
  )
}
