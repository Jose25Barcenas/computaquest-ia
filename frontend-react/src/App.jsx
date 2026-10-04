import { Routes, Route, Navigate } from 'react-router-dom'
import { useAuth } from './context/AuthContext'
import { homePathFor } from './constants'
import Navbar from './components/Navbar/Navbar'
import LoginPage from './pages/LoginPage'
import DashboardPage from './pages/DashboardPage'
import ChallengesPage from './pages/ChallengesPage'
import ChallengePage from './pages/ChallengePage'
import AdminPage from './pages/AdminPage'
import SurveyPage from './pages/SurveyPage'
import QuickAccessPage from './pages/QuickAccessPage'

function ProtectedRoute({ children, adminOnly = false, studentOnly = false }) {
  const { user, loading } = useAuth()

  if (loading) return <div className="loading-screen"><div className="spinner"></div></div>
  if (!user) return <Navigate to="/" />
  if (adminOnly && user.role !== 'ADMIN') return <Navigate to="/dashboard" />
  if (studentOnly && user.role === 'ADMIN') return <Navigate to="/admin" />

  return children
}

function PublicRoute({ children }) {
  const { user, loading } = useAuth()

  if (loading) return <div className="loading-screen"><div className="spinner"></div></div>
  if (user) return <Navigate to={homePathFor(user)} />

  return children
}

function NotFoundRoute() {
  const { user, loading } = useAuth()

  if (loading) return <div className="loading-screen"><div className="spinner"></div></div>
  return <Navigate to={user ? homePathFor(user) : '/'} />
}

export default function App() {
  return (
    <>
      <Navbar />
      <main className="main-content">
        <Routes>
          <Route path="/" element={<PublicRoute><LoginPage /></PublicRoute>} />
          <Route path="/access" element={<QuickAccessPage />} />
          <Route path="/dashboard" element={<ProtectedRoute studentOnly><DashboardPage /></ProtectedRoute>} />
          <Route path="/challenges" element={<ProtectedRoute studentOnly><ChallengesPage /></ProtectedRoute>} />
          <Route path="/challenge/:id" element={<ProtectedRoute studentOnly><ChallengePage /></ProtectedRoute>} />
          <Route path="/survey" element={<ProtectedRoute studentOnly><SurveyPage /></ProtectedRoute>} />
          <Route path="/admin" element={<ProtectedRoute adminOnly><AdminPage /></ProtectedRoute>} />
          <Route path="*" element={<NotFoundRoute />} />
        </Routes>
      </main>
    </>
  )
}
