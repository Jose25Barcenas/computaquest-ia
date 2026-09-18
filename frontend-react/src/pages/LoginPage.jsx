import { useState } from 'react'
import { useAuth } from '../context/AuthContext'
import { useToast } from '../context/ToastContext'

export default function LoginPage() {
  const [isLogin, setIsLogin] = useState(true)
  const [name, setName] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [avatar, setAvatar] = useState('avatar1')
  const [grade, setGrade] = useState('')
  const [loading, setLoading] = useState(false)
  const [errors, setErrors] = useState({})
  const { login, register } = useAuth()
  const toast = useToast()

  const avatars = ['avatar1', 'avatar2', 'avatar3', 'avatar4']

  const clearErrors = () => setErrors({})

  const switchMode = (toLogin) => {
    setIsLogin(toLogin)
    setErrors({})
    setName('')
    setEmail('')
    setPassword('')
    setGrade('')
    setAvatar('avatar1')
  }

  const validateFields = () => {
    const newErrors = {}

    if (!isLogin && !name.trim()) {
      newErrors.name = 'El nombre es obligatorio'
    }

    if (!email.trim()) {
      newErrors.email = 'El email es obligatorio'
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
      newErrors.email = 'Ingresa un email valido'
    }

    if (!password) {
      newErrors.password = 'La contrasena es obligatoria'
    } else if (password.length < 6) {
      newErrors.password = 'Minimo 6 caracteres'
    }

    if (!isLogin && !grade) {
      newErrors.grade = 'Selecciona tu grado'
    }

    setErrors(newErrors)
    return Object.keys(newErrors).length === 0
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    clearErrors()

    if (!validateFields()) return

    setLoading(true)

    try {
      if (isLogin) {
        await login(email, password)
        toast.success('¡Bienvenido de vuelta!')
      } else {
        await register(name, email, password, avatar, grade)
        toast.success('¡Cuenta creada! Ahora inicia sesion')
        setIsLogin(true)
        setEmail('')
        setPassword('')
      }
    } catch (error) {
      const msg = error.message || 'Error al procesar'

      if (msg.toLowerCase().includes('email') || msg.toLowerCase().includes('correo')) {
        setErrors({ email: msg })
      } else if (msg.toLowerCase().includes('contraseña') || msg.toLowerCase().includes('password')) {
        setErrors({ password: msg })
      } else if (msg.toLowerCase().includes('nombre') || msg.toLowerCase().includes('name')) {
        setErrors({ name: msg })
      } else if (msg.toLowerCase().includes('grado') || msg.toLowerCase().includes('grade')) {
        setErrors({ grade: msg })
      } else {
        setErrors({ general: msg })
      }
    } finally {
      setLoading(false)
    }
  }

  return (
    <main className="hero-container">
      <section className="hero-text animate-fade">
        <h1>Aprende Pensamiento Computacional Jugando</h1>
        <p>Conviertete en un maestro de la logica y la resolucion de problemas. Enfrentate a desafios inteligentes, sube de nivel con algoritmos y habla con nuestro tutor de Inteligencia Artificial.</p>
      </section>

      <section className="login-box glass-panel animate-fade">
        {errors.general && <div className="error-banner">{errors.general}</div>}

        {!isLogin ? (
          <div className="auth-form">
            <h2><i className="fa-solid fa-user-plus"></i> Registro de Heroe</h2>
            <form onSubmit={handleSubmit}>
              <div className="input-group">
                <label>Nombre de Estudiante</label>
                <input
                  type="text"
                  value={name}
                  onChange={e => { setName(e.target.value); if (errors.name) setErrors(prev => ({ ...prev, name: '' })) }}
                  placeholder="Ej. Miguel..."
                  className={errors.name ? 'input-error' : ''}
                  required
                />
                {errors.name && <span className="field-error">{errors.name}</span>}
              </div>
              <div className="input-group">
                <label>Email</label>
                <input
                  type="email"
                  value={email}
                  onChange={e => { setEmail(e.target.value); if (errors.email) setErrors(prev => ({ ...prev, email: '' })) }}
                  placeholder="tu@email.com"
                  className={errors.email ? 'input-error' : ''}
                  required
                />
                {errors.email && <span className="field-error">{errors.email}</span>}
              </div>
              <div className="input-group">
                <label>Contrasena</label>
                <input
                  type="password"
                  value={password}
                  onChange={e => { setPassword(e.target.value); if (errors.password) setErrors(prev => ({ ...prev, password: '' })) }}
                  placeholder="Minimo 6 caracteres"
                  className={errors.password ? 'input-error' : ''}
                  required
                  minLength={6}
                />
                {errors.password && <span className="field-error">{errors.password}</span>}
              </div>

              <div className="input-group">
                <label>Grado</label>
                <select
                  value={grade}
                  onChange={e => { setGrade(e.target.value); if (errors.grade) setErrors(prev => ({ ...prev, grade: '' })) }}
                  className={errors.grade ? 'input-error' : ''}
                  required
                >
                  <option value="">Selecciona tu grado</option>
                  <option value="8">Octavo</option>
                  <option value="9">Noveno</option>
                </select>
                {errors.grade && <span className="field-error">{errors.grade}</span>}
              </div>

              <label className="avatar-label">Selecciona tu Avatar</label>
              <div className="avatar-grid">
                {avatars.map(av => (
                  <div
                    key={av}
                    className={`avatar-option ${avatar === av ? 'selected' : ''}`}
                    onClick={() => setAvatar(av)}
                  >
                    <img src={`https://api.dicebear.com/7.x/bottts/svg?seed=${av}`} alt={av} />
                  </div>
                ))}
              </div>

              <button type="submit" className="btn-primary btn-full" disabled={loading}>
                {loading ? <i className="fa-solid fa-spinner fa-spin"></i> : 'Crear Cuenta'}
              </button>
              <p className="auth-switch">
                ¿Ya tienes cuenta? <a onClick={() => switchMode(true)}>Iniciar Sesion</a>
              </p>
            </form>
          </div>
        ) : (
          <div className="auth-form">
            <h2><i className="fa-solid fa-right-to-bracket"></i> Iniciar Sesion</h2>
            <form onSubmit={handleSubmit}>
              <div className="input-group">
                <label>Email</label>
                <input
                  type="email"
                  value={email}
                  onChange={e => { setEmail(e.target.value); if (errors.email) setErrors(prev => ({ ...prev, email: '' })) }}
                  placeholder="tu@email.com"
                  className={errors.email ? 'input-error' : ''}
                  required
                />
                {errors.email && <span className="field-error">{errors.email}</span>}
              </div>
              <div className="input-group">
                <label>Contrasena</label>
                <input
                  type="password"
                  value={password}
                  onChange={e => { setPassword(e.target.value); if (errors.password) setErrors(prev => ({ ...prev, password: '' })) }}
                  placeholder="Tu contrasena"
                  className={errors.password ? 'input-error' : ''}
                  required
                />
                {errors.password && <span className="field-error">{errors.password}</span>}
              </div>
              <button type="submit" className="btn-primary btn-full" disabled={loading}>
                {loading ? <i className="fa-solid fa-spinner fa-spin"></i> : 'Entrar al Mundo Virtual'}
              </button>
              <p className="auth-switch">
                ¿No tienes cuenta? <a onClick={() => switchMode(false)}>Registrate</a>
              </p>
            </form>
          </div>
        )}
      </section>
    </main>
  )
}
