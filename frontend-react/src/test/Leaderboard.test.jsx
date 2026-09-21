import { render, screen } from '@testing-library/react'
import { describe, it, expect, vi } from 'vitest'
import { MemoryRouter } from 'react-router-dom'
import Leaderboard from '../components/Leaderboard/Leaderboard'
import { AuthProvider } from '../context/AuthContext'
import { ToastProvider } from '../context/ToastContext'

vi.mock('../services/api', () => ({
  default: {
    getLeaderboard: vi.fn().mockResolvedValue([]),
    getMe: vi.fn().mockRejectedValue(new Error('Not logged in')),
  },
}))

function renderLeaderboard() {
  return render(
    <MemoryRouter>
      <ToastProvider>
        <AuthProvider>
          <Leaderboard />
        </AuthProvider>
      </ToastProvider>
    </MemoryRouter>
  )
}

describe('Leaderboard', () => {
  it('renders leaderboard heading', () => {
    renderLeaderboard()
    expect(screen.getByText(/Leaderboard/)).toBeInTheDocument()
  })

  it('shows empty state when no entries', async () => {
    renderLeaderboard()
    expect(await screen.findByText(/Sé el primero/)).toBeInTheDocument()
  })
})
