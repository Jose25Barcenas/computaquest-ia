let ctx = null

function getCtx() {
  if (!ctx) ctx = new (window.AudioContext || window.webkitAudioContext)()
  if (ctx.state === 'suspended') ctx.resume()
  return ctx
}

function tone(c, { type = 'square', freq = 440, start = 0, dur = 0.1, vol = 0.18 }) {
  const o = c.createOscillator()
  const g = c.createGain()
  const t0 = c.currentTime + start
  o.type = type
  o.frequency.setValueAtTime(freq, t0)
  g.gain.setValueAtTime(vol, t0)
  g.gain.exponentialRampToValueAtTime(0.001, t0 + dur)
  o.connect(g)
  g.connect(c.destination)
  o.start(t0)
  o.stop(t0 + dur)
}

export function playClick() {
  try {
    const c = getCtx()
    tone(c, { type: 'square', freq: 880, dur: 0.07, vol: 0.13 })
    tone(c, { type: 'square', freq: 1175, dur: 0.06, vol: 0.11, start: 0.05 })
  } catch { /* silenciar audio bloqueado */ }
}

export function playSelect() {
  try {
    const c = getCtx()
    tone(c, { type: 'triangle', freq: 523, dur: 0.12, vol: 0.16, start: 0 })
    tone(c, { type: 'triangle', freq: 659, dur: 0.12, vol: 0.16, start: 0.08 })
    tone(c, { type: 'triangle', freq: 784, dur: 0.18, vol: 0.16, start: 0.16 })
  } catch { /* silenciar audio bloqueado */ }
}

export function playFanfare() {
  try {
    const c = getCtx()
    tone(c, { type: 'square', freq: 523, dur: 0.13, vol: 0.13, start: 0 })
    tone(c, { type: 'square', freq: 659, dur: 0.13, vol: 0.13, start: 0.11 })
    tone(c, { type: 'square', freq: 784, dur: 0.13, vol: 0.13, start: 0.22 })
    tone(c, { type: 'square', freq: 1047, dur: 0.5, vol: 0.13, start: 0.33 })
  } catch { /* silenciar audio bloqueado */ }
}

export function playSuccess() {
  try {
    const c = getCtx()
    tone(c, { type: 'sine', freq: 784, dur: 0.15, vol: 0.15, start: 0 })
    tone(c, { type: 'sine', freq: 988, dur: 0.15, vol: 0.15, start: 0.12 })
    tone(c, { type: 'sine', freq: 1175, dur: 0.35, vol: 0.15, start: 0.24 })
  } catch { /* silenciar audio bloqueado */ }
}
