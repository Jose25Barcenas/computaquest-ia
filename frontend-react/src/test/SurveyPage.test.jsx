import { render, screen } from '@testing-library/react'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import SurveyPage from '../pages/SurveyPage'
import { ToastProvider } from '../context/ToastContext'

const mocks = vi.hoisted(() => ({
  getUserSurveys: vi.fn(),
  submitSurvey: vi.fn(),
}))

vi.mock('../services/api', () => ({
  setOnUnauthorized: vi.fn(),
  default: {
    getUserSurveys: mocks.getUserSurveys,
    submitSurvey: mocks.submitSurvey,
  },
}))

function renderSurvey() {
  return render(
    <ToastProvider>
      <SurveyPage />
    </ToastProvider>
  )
}

describe('SurveyPage', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('disables the pre survey button when the student already responded', async () => {
    mocks.getUserSurveys.mockResolvedValue([
      { type: 'pre', createdAt: '2026-10-01T10:00:00Z' },
    ])
    renderSurvey()

    const preButton = await screen.findByRole('button', { name: /Encuesta Pre - Respondida/ })
    expect(preButton).toBeDisabled()
    expect(screen.getByText(/Respondida el/)).toBeInTheDocument()

    const postButton = screen.getByRole('button', { name: /Encuesta Post/ })
    expect(postButton).toBeEnabled()
  })

  it('enables both buttons for a student who has not responded', async () => {
    mocks.getUserSurveys.mockResolvedValue([])
    renderSurvey()

    expect(await screen.findByRole('button', { name: /Encuesta Pre/ })).toBeEnabled()
    expect(screen.getByRole('button', { name: /Encuesta Post/ })).toBeEnabled()
  })

  it('shows a thank-you panel when both surveys are answered', async () => {
    mocks.getUserSurveys.mockResolvedValue([
      { type: 'pre', createdAt: '2026-10-01T10:00:00Z' },
      { type: 'post', createdAt: '2026-10-15T10:00:00Z' },
    ])
    renderSurvey()

    expect(await screen.findByText(/Ya respondiste las encuestas pre y post/)).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /Encuesta Post - Respondida/ })).toBeDisabled()
  })
})
