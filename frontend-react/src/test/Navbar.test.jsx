import { render, screen } from '@testing-library/react'
import { describe, it, expect, vi } from 'vitest'
import { MemoryRouter } from 'react-router-dom'
import Navbar from '../components/Navbar/Navbar'
import { AuthProvider } from '../context/AuthContext'
import { ThemeProvider } from '../context/ThemeContext'

vi.mock('../services/api', () => ({
  default: { getMe: vi.fn().mockRejectedValue(new Error('no token')) },
}))

function renderNavbar() {
  localStorage.removeItem('computaquest_token')
  return render(
    <MemoryRouter>
      <ThemeProvider>
        <AuthProvider>
          <Navbar />
        </AuthProvider>
      </ThemeProvider>
    </MemoryRouter>
  )
}

describe('Navbar', () => {
  it('shows logo', () => {
    renderNavbar()
    expect(screen.getByText(/ComputaQuest IA/)).toBeInTheDocument()
  })

  it('shows theme toggle button', () => {
    const { container } = renderNavbar()
    expect(container.querySelector('.theme-switch')).toBeInTheDocument()
  })
})
