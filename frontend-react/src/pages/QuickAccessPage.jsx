import QRCode from '../components/QRCode/QRCode'

function getAppUrl() {
  const { origin } = window.location
  return origin
}

export default function QuickAccessPage() {
  const appUrl = getAppUrl()

  return (
    <div className="qr-page animate-fade">
      <div className="qr-page-header">
        <h1><i className="fa-solid fa-qrcode"></i> Acceso Rapido</h1>
        <p>Escanea el codigo QR para acceder a la plataforma</p>
      </div>

      <QRCode 
        url={appUrl} 
        size={250}
        title="ComputaQuest IA"
      />

      <div className="qr-instructions glass-panel">
        <h3><i className="fa-solid fa-list-ol"></i> Instrucciones</h3>
        <div className="qr-steps">
          <div className="qr-step">
            <span className="qr-step-number">1</span>
            <p className="qr-step-text">Abre la camara de tu celular</p>
          </div>
          <div className="qr-step">
            <span className="qr-step-number">2</span>
            <p className="qr-step-text">Apunta al codigo QR</p>
          </div>
          <div className="qr-step">
            <span className="qr-step-number">3</span>
            <p className="qr-step-text">Toca el enlace que aparece</p>
          </div>
          <div className="qr-step">
            <span className="qr-step-number">4</span>
            <p className="qr-step-text">Registrate y comienza la encuesta</p>
          </div>
        </div>
      </div>
    </div>
  )
}
