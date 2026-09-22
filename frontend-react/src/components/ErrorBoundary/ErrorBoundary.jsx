import { Component } from 'react'

export default class ErrorBoundary extends Component {
  constructor(props) {
    super(props)
    this.state = { hasError: false, error: null }
  }

  static getDerivedStateFromError(error) {
    return { hasError: true, error }
  }

  componentDidCatch(error, errorInfo) {
    if (import.meta.env.DEV) {
      console.error('ErrorBoundary caught:', error, errorInfo)
    }
  }

  render() {
    if (this.state.hasError) {
      return (
        <div style={{
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          justifyContent: 'center',
          minHeight: '100vh',
          padding: '2rem',
          textAlign: 'center',
          color: 'var(--text)',
          background: 'var(--bg)',
        }}>
          <i className="fa-solid fa-triangle-exclamation" style={{ fontSize: '3rem', color: 'var(--danger)', marginBottom: '1rem' }}></i>
          <h2>Algo salio mal</h2>
          <p style={{ color: 'var(--text-secondary)', marginBottom: '1.5rem' }}>
            Ha ocurrido un error inesperado. Por favor, recarga la pagina.
          </p>
          <button
            onClick={() => window.location.reload()}
            className="btn-primary"
          >
            Recargar pagina
          </button>
        </div>
      )
    }

    return this.props.children
  }
}
