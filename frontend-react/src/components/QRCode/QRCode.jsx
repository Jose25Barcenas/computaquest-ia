import { useState, useRef, useEffect } from 'react'

export default function QRCode({ url, size = 200, title = 'Escanea para acceder' }) {
  const [copied, setCopied] = useState(false)
  const [imgError, setImgError] = useState(false)
  const timerRef = useRef(null)

  const qrApiUrl = `https://api.qrserver.com/v1/create-qr-code/?size=${size}x${size}&data=${encodeURIComponent(url)}&format=png&margin=10`

  useEffect(() => {
    return () => {
      if (timerRef.current) clearTimeout(timerRef.current)
    }
  }, [])

  const handleCopy = async () => {
    try {
      await navigator.clipboard.writeText(url)
      setCopied(true)
      timerRef.current = setTimeout(() => setCopied(false), 2000)
    } catch {
      const textarea = document.createElement('textarea')
      textarea.value = url
      document.body.appendChild(textarea)
      textarea.select()
      document.execCommand('copy')
      document.body.removeChild(textarea)
      setCopied(true)
      timerRef.current = setTimeout(() => setCopied(false), 2000)
    }
  }

  const handleDownload = async () => {
    try {
      const response = await fetch(qrApiUrl)
      const blob = await response.blob()
      const objectUrl = URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = objectUrl
      link.download = 'computaquest-qr.png'
      link.click()
      setTimeout(() => URL.revokeObjectURL(objectUrl), 1000)
    } catch {
      const link = document.createElement('a')
      link.href = qrApiUrl
      link.target = '_blank'
      link.download = 'computaquest-qr.png'
      link.click()
    }
  }

  return (
    <div className="qr-container glass-panel">
      {title && <h3 className="qr-title">{title}</h3>}
      <div className="qr-image-wrapper">
        {imgError ? (
          <div style={{ width: size, height: size, display: 'flex', alignItems: 'center', justifyContent: 'center', background: '#f0f0f0', borderRadius: 8, flexDirection: 'column', gap: 8 }}>
            <i className="fa-solid fa-qrcode" style={{ fontSize: 48, color: '#6366f1' }}></i>
            <span style={{ fontSize: 12, color: '#666' }}>{url}</span>
          </div>
        ) : (
          <img
            src={qrApiUrl}
            alt="Código QR de acceso a ComputaQuest"
            className="qr-image"
            width={size}
            height={size}
            onError={() => setImgError(true)}
          />
        )}
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
