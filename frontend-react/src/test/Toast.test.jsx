import { render, screen } from '@testing-library/react'
import { describe, it, expect } from 'vitest'
import Toast from '../components/Toast/Toast'

describe('Toast', () => {
  it('renders message', () => {
    render(<Toast message="Test message" type="success" />)
    expect(screen.getByText('Test message')).toBeInTheDocument()
  })

  it('applies correct type class', () => {
    const { container } = render(<Toast message="Error" type="error" />)
    expect(container.firstChild).toHaveClass('toast-error')
  })

  it('defaults to info type', () => {
    const { container } = render(<Toast message="Info" />)
    expect(container.firstChild).toHaveClass('toast-info')
  })
})
