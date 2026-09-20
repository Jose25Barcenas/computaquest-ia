import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'
import { useTheme } from '../../context/ThemeContext'

export default function Navbar() {
  const { user, logout } = useAuth()
  const { theme, toggleTheme } = useTheme()
  const navigate = useNavigate()
  const [menuOpen, setMenuOpen] = useState(false)

  const handleLogout = () => {
    logout()
    navigate('/')
    setMenuOpen(false)
  }

  const closeMenu = () => setMenuOpen(false)

  return (
    <nav className="navbar">
      <Link to={user ? '/dashboard' : '/'} className="logo-area" onClick={closeMenu}>
        <i className="fa-solid fa-brain"></i> ComputaQuest IA
      </Link>

      {user && (
        <button
          className="menu-toggle"
          onClick={() => setMenuOpen(!menuOpen)}
          aria-label={menuOpen ? 'Cerrar menu' : 'Abrir menu'}
          aria-expanded={menuOpen}
        >
          <i className={`fa-solid fa-${menuOpen ? 'xmark' : 'bars'}`}></i>
        </button>
      )}

      <div className={`nav-controls ${menuOpen ? 'nav-open' : ''}`}>
        {user && (
          <>
            <Link to="/dashboard" className="nav-link" onClick={closeMenu}>
              <i className="fa-solid fa-house"></i> Inicio
            </Link>
            <Link to="/challenges" className="nav-link" onClick={closeMenu}>
              <i className="fa-solid fa-gamepad"></i> Retos
            </Link>
            <Link to="/survey" className="nav-link" onClick={closeMenu}>
              <i className="fa-solid fa-clipboard-list"></i> Encuesta
            </Link>
            {user.role === 'ADMIN' && (
              <Link to="/admin" className="nav-link" onClick={closeMenu}>
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
              <i className="fa-solid fa-right-from-bracket"></i> Cerrar sesion
            </button>
          </>
        )}

        <button className="theme-switch" onClick={toggleTheme} aria-label={theme === 'dark' ? 'Cambiar a tema claro' : 'Cambiar a tema oscuro'}>
          <i className={`fa-solid fa-${theme === 'dark' ? 'sun' : 'moon'}`}></i>
        </button>
      </div>
    </nav>
  )
}
