import { render, screen } from '@testing-library/react'
import { describe, it, expect } from 'vitest'
import { ToastProvider, useToast } from '../context/ToastContext'

function TestComponent({ onToast }) {
  const toast = useToast()
  onToast(toast)
  return <div>test</div>
}

describe('ToastContext', () => {
  it('provides success, error, info functions', () => {
    let toastFunctions
    render(
      <ToastProvider>
        <TestComponent onToast={(t) => (toastFunctions = t)} />
      </ToastProvider>
    )
    expect(toastFunctions.success).toBeInstanceOf(Function)
    expect(toastFunctions.error).toBeInstanceOf(Function)
    expect(toastFunctions.info).toBeInstanceOf(Function)
  })
})
