import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'
import { useTheme } from '../../context/ThemeContext'

export default function Navbar() {
  const { user, logout } = useAuth()
  const { theme, toggleTheme } = useTheme()
  const navigate = useNavigate()

  const handleLogout = () => {
    logout()
    navigate('/')
  }

  return (
    <nav className="navbar">
      <Link to={user ? '/dashboard' : '/'} className="logo-area">
        <i className="fa-solid fa-brain"></i> ComputaQuest IA
      </Link>

      <div className="nav-controls">
        {user && (
          <>
            <Link to="/dashboard" className="nav-link">
              <i className="fa-solid fa-house"></i> Inicio
            </Link>
            <Link to="/challenges" className="nav-link">
              <i className="fa-solid fa-gamepad"></i> Retos
            </Link>
            <Link to="/survey" className="nav-link">
              <i className="fa-solid fa-clipboard-list"></i> Encuesta
            </Link>
            {user.role === 'ADMIN' && (
              <Link to="/admin" className="nav-link">
                <i className="fa-solid fa-gear"></i> Admin
              </Link>
            )}
            <span className="nav-user">
              <img
                src={`https://api.dicebear.com/7.x/bottts/svg?seed=${user.avatar || 'avatar1'}`}
                alt="Avatar"
                className="nav-avatar"
              />
              {user.name}
            </span>
            <button onClick={handleLogout} className="btn-secondary btn-sm">
              <i className="fa-solid fa-right-from-bracket"></i>
            </button>
          </>
        )}

        <button className="theme-switch" onClick={toggleTheme}>
          <i className={`fa-solid fa-${theme === 'dark' ? 'sun' : 'moon'}`}></i>
        </button>
      </div>
    </nav>
  )
}
