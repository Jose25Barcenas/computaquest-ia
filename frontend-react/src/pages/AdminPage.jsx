import { useState, useEffect } from 'react'
import api from '../services/api'
import { useToast } from '../context/ToastContext'

const timeAgo = (iso) => {
  if (!iso) return ''
  const diff = Date.now() - new Date(iso).getTime()
  const min = Math.floor(diff / 60000)
  if (min < 1) return 'ahora mismo'
  if (min < 60) return `hace ${min} min`
  const hours = Math.floor(min / 60)
  if (hours < 24) return `hace ${hours} h`
  const days = Math.floor(hours / 24)
  return `hace ${days} d`
}

export default function AdminPage() {
  const [tab, setTab] = useState('overview')
  const [users, setUsers] = useState([])
  const [challenges, setChallenges] = useState([])
  const [surveys, setSurveys] = useState([])
  const [stats, setStats] = useState(null)
  const [comparison, setComparison] = useState(null)
  const [challengeStats, setChallengeStats] = useState([])
  const [activity, setActivity] = useState([])
  const [loading, setLoading] = useState(true)
  const toast = useToast()

  const [newChallenge, setNewChallenge] = useState({
    title: '',
    description: '',
    type: 'decomposition',
    difficulty: 1,
    xpReward: 100,
    pointsReward: 10,
    badgeName: '',
    contentStr: '{}',
  })

  const [contentError, setContentError] = useState('')

  const loadData = async () => {
    try {
      const [usersData, challengesData, surveysData, statsData, comparisonData, challengeStatsData, activityData] = await Promise.all([
        api.getUsers(),
        api.getChallenges(),
        api.getAllSurveys(),
        api.getAdminStats().catch(() => null),
        api.getSurveyComparison().catch(() => null),
        api.getAdminChallengeStats().catch(() => []),
        api.getAdminActivity().catch(() => []),
      ])
      setUsers(Array.isArray(usersData) ? usersData : usersData?.users || [])
      setChallenges(Array.isArray(challengesData) ? challengesData : [])
      setSurveys(Array.isArray(surveysData) ? surveysData : [])
      setStats(statsData)
      setComparison(comparisonData)
      setChallengeStats(Array.isArray(challengeStatsData) ? challengeStatsData : [])
      setActivity(Array.isArray(activityData) ? activityData : [])
    } catch {
      toast.error('Error al cargar datos')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadData()
  }, [])

  const handleDeleteUser = async (id) => {
    if (!confirm('¿Eliminar este usuario?')) return
    try {
      await api.deleteUser(id)
      setUsers(prev => prev.filter(u => u.id !== id))
      toast.success('Usuario eliminado')
    } catch {
      toast.error('Error al eliminar usuario')
    }
  }

  const handleDeleteChallenge = async (id) => {
    if (!confirm('¿Eliminar este reto?')) return
    try {
      await api.deleteChallenge(id)
      setChallenges(prev => prev.filter(c => c.id !== id))
      toast.success('Reto eliminado')
    } catch {
      toast.error('Error al eliminar reto')
    }
  }

  const handleCreateChallenge = async (e) => {
    e.preventDefault()
    setContentError('')
    let parsedContent
    try {
      parsedContent = JSON.parse(newChallenge.contentStr)
    } catch {
      setContentError('JSON invalido. Revisa la sintaxis.')
      return
    }
    try {
      const { contentStr: _contentStr, ...rest } = newChallenge
      const data = await api.createChallenge({ ...rest, content: parsedContent })
      setChallenges(prev => [...prev, data])
      setNewChallenge({
        title: '', description: '', type: 'decomposition',
        difficulty: 1, xpReward: 100, pointsReward: 10, badgeName: '', contentStr: '{}',
      })
      toast.success('Reto creado exitosamente')
    } catch {
      toast.error('Error al crear reto')
    }
  }

  if (loading) return <div className="loading-screen"><div className="spinner"></div></div>

  const dimensionLabel = (key) =>
    key.replace(/_/g, ' ').replace(/\b\w/g, c => c.toUpperCase())

  const exportCsv = () => {
    if (!comparison) return
    const rows = [
      ['Resultados Pre/Post - ComputaQuest'],
      [],
      ['Metrica', 'Pre', 'Post', 'Delta', 'Maximo'],
      ['Promedio general', comparison.preAverage, comparison.postAverage, comparison.averageDelta, comparison.maxScore],
      ['Respuestas', comparison.preCount, comparison.postCount, comparison.pairedCount],
      [],
      ['Dimension', 'Pre', 'Post', 'Delta'],
      ...Object.keys(comparison.preDimensions || {}).concat(
        Object.keys(comparison.postDimensions || {}).filter(d => !(comparison.preDimensions || {})[d])
      ).map(d => [
        dimensionLabel(d),
        comparison.preDimensions?.[d] ?? 0,
        comparison.postDimensions?.[d] ?? 0,
        comparison.dimensionDelta?.[d] ?? 0,
      ]),
      [],
      ['Estudiante', 'Grado', 'Pre', 'Post', 'Delta'],
      ...(comparison.paired || []).map(p => [p.user, p.grade || '', p.pre, p.post, p.delta]),
    ]
    const csv = rows
      .map(r => r.map(c => `"${String(c ?? '').replace(/"/g, '""')}"`).join(','))
      .join('\n')
    const blob = new Blob(['\ufeff' + csv], { type: 'text/csv;charset=utf-8;' })
    const link = document.createElement('a')
    link.href = URL.createObjectURL(blob)
    link.download = 'resultados-pre-post.csv'
    link.click()
    URL.revokeObjectURL(link.href)
  }

  return (
    <div className="admin-container">
      <h1 className="page-title"><i className="fa-solid fa-gear"></i> Panel de Administracion</h1>

      <div className="admin-tabs">
        <button className={`tab-btn ${tab === 'overview' ? 'active' : ''}`} onClick={() => setTab('overview')}>
          <i className="fa-solid fa-chart-line"></i> Resumen
        </button>
        <button className={`tab-btn ${tab === 'results' ? 'active' : ''}`} onClick={() => setTab('results')}>
          <i className="fa-solid fa-flask"></i> Pre / Post
        </button>
        <button className={`tab-btn ${tab === 'users' ? 'active' : ''}`} onClick={() => setTab('users')}>
          <i className="fa-solid fa-users"></i> Estudiantes ({users.length})
        </button>
        <button className={`tab-btn ${tab === 'challenges' ? 'active' : ''}`} onClick={() => setTab('challenges')}>
          <i className="fa-solid fa-gamepad"></i> Retos ({challenges.length})
        </button>
        <button className={`tab-btn ${tab === 'surveys' ? 'active' : ''}`} onClick={() => setTab('surveys')}>
          <i className="fa-solid fa-clipboard-list"></i> Encuestas ({surveys.length})
        </button>
      </div>

      {tab === 'overview' && (
        <div className="kpi-grid">
          <div className="kpi-card glass-panel">
            <i className="fa-solid fa-user-graduate"></i>
            <div className="kpi-value">{stats?.studentsTotal ?? '—'}</div>
            <div className="kpi-label">Estudiantes</div>
            <div className="kpi-sub">{stats?.studentsActive7d ?? 0} activos en los ultimos 7 dias</div>
          </div>
          <div className="kpi-card glass-panel">
            <i className="fa-solid fa-gamepad"></i>
            <div className="kpi-value">{stats?.challengesActive ?? '—'}</div>
            <div className="kpi-label">Retos activos</div>
            <div className="kpi-sub">de {stats?.challengesTotal ?? 0} creados</div>
          </div>
          <div className="kpi-card glass-panel">
            <i className="fa-solid fa-circle-check"></i>
            <div className="kpi-value">{stats?.challengesCompleted ?? '—'}</div>
            <div className="kpi-label">Retos completados</div>
            <div className="kpi-sub">Tasa global: {stats?.completionRate ?? 0}%</div>
          </div>
          <div className="kpi-card glass-panel">
            <i className="fa-solid fa-clipboard-question"></i>
            <div className="kpi-value">{stats?.surveysPre ?? '—'}</div>
            <div className="kpi-label">Pre-test recibidos</div>
            <div className="kpi-sub">Encuestas iniciales</div>
          </div>
          <div className="kpi-card glass-panel">
            <i className="fa-solid fa-clipboard-check"></i>
            <div className="kpi-value">{stats?.surveysPost ?? '—'}</div>
            <div className="kpi-label">Post-test recibidos</div>
            <div className="kpi-sub">Encuestas finales</div>
          </div>
          <div className="kpi-card glass-panel">
            <i className="fa-solid fa-code-compare"></i>
            <div className="kpi-value">{comparison?.pairedCount ?? '—'}</div>
            <div className="kpi-label">Estudiantes comparables</div>
            <div className="kpi-sub">Con pre-test y post-test</div>
          </div>
        </div>
      )}

      {tab === 'overview' && (
        <>
          <div className="admin-table glass-panel">
            <h3><i className="fa-solid fa-chart-column"></i> Rendimiento por reto</h3>
            <table>
              <thead>
                <tr>
                  <th>Reto</th>
                  <th>Completados</th>
                  <th>Intentos</th>
                  <th>Puntaje prom.</th>
                  <th>Tasa de finalizacion</th>
                </tr>
              </thead>
              <tbody>
                {challengeStats.length === 0 && (
                  <tr><td colSpan={5} style={{color: 'var(--text-muted)'}}>Sin datos todavia</td></tr>
                )}
                {challengeStats.map(s => (
                  <tr key={s.challengeId}>
                    <td>{s.title}</td>
                    <td>{s.uniqueCompletions}</td>
                    <td>{s.attempts}</td>
                    <td>{s.avgScore}</td>
                    <td>
                      <div className="rate-bar">
                        <div className="rate-bar-track">
                          <div className="rate-bar-fill" style={{width: `${s.completionRate}%`}}></div>
                        </div>
                        <span>{s.completionRate}%</span>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          <div className="glass-panel activity-panel">
            <h3><i className="fa-solid fa-clock-rotate-left"></i> Actividad reciente</h3>
            {activity.length === 0 ? (
              <p style={{color: 'var(--text-muted)'}}>Sin actividad todavia</p>
            ) : (
              <ul className="activity-list">
                {activity.map((a, i) => (
                  <li key={`${a.at}-${i}`}>
                    <i className={`fa-solid ${a.type === 'SURVEY' ? 'fa-clipboard-list' : 'fa-gamepad'}`}></i>
                    <span className="activity-user">{a.user}</span>
                    <span>{a.detail}</span>
                    <time>{timeAgo(a.at)}</time>
                  </li>
                ))}
              </ul>
            )}
          </div>
        </>
      )}

      {tab === 'results' && (
        <ResultsPanel comparison={comparison} onExport={exportCsv} />
      )}

      {tab === 'users' && (
        <div className="admin-table glass-panel">
          <table>
            <thead>
              <tr>
                <th>Avatar</th>
                <th>Nombre</th>
                <th>Email</th>
                <th>Rol</th>
                <th>Nivel</th>
                <th>Puntos</th>
                <th>Acciones</th>
              </tr>
            </thead>
            <tbody>
              {users.map(user => (
                <tr key={user.id}>
                  <td><img src={`https://api.dicebear.com/7.x/bottts/svg?seed=${user.avatar}`} alt="" className="table-avatar" /></td>
                  <td>{user.name}</td>
                  <td>{user.email}</td>
                  <td><span className={`role-badge ${user.role}`}>{user.role}</span></td>
                  <td>{user.level}</td>
                  <td>{user.points}</td>
                  <td>
                    <button className="btn-danger btn-sm" onClick={() => handleDeleteUser(user.id)}>
                      <i className="fa-solid fa-trash"></i>
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {tab === 'challenges' && (
        <>
          <div className="admin-form glass-panel">
            <h3><i className="fa-solid fa-plus"></i> Crear Nuevo Reto</h3>
            <form onSubmit={handleCreateChallenge}>
              <div className="form-grid">
                <div className="input-group">
                  <label>Titulo</label>
                  <input type="text" value={newChallenge.title} onChange={e => setNewChallenge({...newChallenge, title: e.target.value})} required maxLength={100} />
                </div>
                <div className="input-group">
                  <label>Descripcion</label>
                  <textarea value={newChallenge.description} onChange={e => setNewChallenge({...newChallenge, description: e.target.value})} required maxLength={500} />
                </div>
                <div className="input-group">
                  <label>Tipo</label>
                  <select value={newChallenge.type} onChange={e => setNewChallenge({...newChallenge, type: e.target.value})}>
                    <option value="decomposition">Descomposicion</option>
                    <option value="patterns">Patrones</option>
                    <option value="abstraction">Abstraccion</option>
                    <option value="algorithms">Algoritmos</option>
                  </select>
                </div>
                <div className="input-group">
                  <label>Dificultad (1-5)</label>
                  <input type="number" min="1" max="5" value={newChallenge.difficulty} onChange={e => setNewChallenge({...newChallenge, difficulty: parseInt(e.target.value)})} />
                </div>
                <div className="input-group">
                  <label>Insignia</label>
                  <input type="text" value={newChallenge.badgeName} onChange={e => setNewChallenge({...newChallenge, badgeName: e.target.value})} placeholder="Opcional" />
                </div>
                <div className="input-group" style={{gridColumn: '1 / -1'}}>
                  <label>Contenido (JSON)</label>
                  <textarea
                    value={newChallenge.contentStr}
                    onChange={e => { setNewChallenge({...newChallenge, contentStr: e.target.value}); if (contentError) setContentError('') }}
                    placeholder={'{\n  "type": "drag-drop",\n  "items": [{"id":"a","text":"Paso 1"},{"id":"b","text":"Paso 2"}],\n  "correctOrder": ["a","b"]\n}'}
                    style={{fontFamily: 'monospace', minHeight: '150px', fontSize: '0.85rem'}}
                    className={contentError ? 'input-error' : ''}
                    required
                  />
                  {contentError && <span className="field-error">{contentError}</span>}
                </div>
              </div>
              <button type="submit" className="btn-primary"><i className="fa-solid fa-plus"></i> Crear Reto</button>
            </form>
          </div>

          <div className="admin-table glass-panel">
            <table>
              <thead>
                <tr>
                  <th>Titulo</th>
                  <th>Tipo</th>
                  <th>Dificultad</th>
                  <th>XP</th>
                  <th>Puntos</th>
                  <th>Acciones</th>
                </tr>
              </thead>
              <tbody>
                {challenges.map(ch => (
                  <tr key={ch.id}>
                    <td>{ch.title}</td>
                    <td><span className="type-badge">{ch.type}</span></td>
                    <td>{ch.difficulty}</td>
                    <td>{ch.xpReward}</td>
                    <td>{ch.pointsReward}</td>
                    <td>
                      <button className="btn-danger btn-sm" onClick={() => handleDeleteChallenge(ch.id)}>
                        <i className="fa-solid fa-trash"></i>
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </>
      )}

      {tab === 'surveys' && (
        <div className="admin-table glass-panel">
          {surveys.length === 0 ? (
            <p style={{textAlign: 'center', padding: '2rem', color: 'var(--text-muted)'}}>
              <i className="fa-solid fa-clipboard-list" style={{fontSize: '2rem', display: 'block', marginBottom: '0.5rem'}}></i>
              No hay encuestas registradas aun
            </p>
          ) : (
            <table>
              <thead>
                <tr>
                  <th>Usuario</th>
                  <th>Tipo</th>
                  <th>Puntaje</th>
                  <th>Maximo</th>
                  <th>Porcentaje</th>
                  <th>Fecha</th>
                </tr>
              </thead>
              <tbody>
                {surveys.map(s => {
                  const maxScore = s.maxScore || 75
                  const pct = Math.round((s.totalScore / maxScore) * 100)
                  return (
                    <tr key={s.id}>
                      <td>{s.user}</td>
                      <td><span className={`role-badge ${s.type === 'pre' ? 'student' : 'admin'}`}>{s.type === 'pre' ? 'Pre' : 'Post'}</span></td>
                      <td><strong>{s.totalScore}</strong></td>
                      <td>{maxScore}</td>
                      <td>
                        <div style={{display: 'flex', alignItems: 'center', gap: '0.5rem'}}>
                          <div style={{flex: 1, height: '6px', background: 'var(--bg-card)', borderRadius: '3px', overflow: 'hidden'}}>
                            <div style={{width: `${pct}%`, height: '100%', background: pct >= 70 ? 'var(--secondary)' : 'var(--warning)', borderRadius: '3px'}}></div>
                          </div>
                          <span style={{fontSize: '0.8rem', color: 'var(--text-muted)'}}>{pct}%</span>
                        </div>
                      </td>
                      <td style={{fontSize: '0.8rem', color: 'var(--text-muted)'}}>{s.createdAt ? new Date(s.createdAt).toLocaleDateString() : '-'}</td>
                    </tr>
                  )
                })}
              </tbody>
            </table>
          )}
        </div>
      )}
    </div>
  )
}

function ResultsPanel({ comparison, onExport }) {
  if (!comparison) {
    return (
      <div className="admin-table glass-panel">
        <p style={{ textAlign: 'center', padding: '2rem', color: 'var(--text-muted)' }}>
          <i className="fa-solid fa-flask" style={{ fontSize: '2rem', display: 'block', marginBottom: '0.5rem' }}></i>
          Aun no hay datos de encuestas pre/post
        </p>
      </div>
    )
  }

  const label = (key) => key.replace(/_/g, ' ').replace(/\b\w/g, c => c.toUpperCase())
  const max = comparison.maxScore || 75
  const dimensions = Object.keys({
    ...comparison.preDimensions,
    ...comparison.postDimensions,
  })

  return (
    <>
      <div className="kpi-grid">
        <div className="kpi-card glass-panel">
          <i className="fa-solid fa-clipboard-question"></i>
          <div className="kpi-value">{comparison.preAverage} <span style={{ fontSize: '0.9rem', color: 'var(--text-muted)' }}>/ {max}</span></div>
          <div className="kpi-label">Promedio pre-test</div>
          <div className="kpi-sub">{comparison.preCount} respuestas</div>
        </div>
        <div className="kpi-card glass-panel">
          <i className="fa-solid fa-clipboard-check"></i>
          <div className="kpi-value">{comparison.postAverage} <span style={{ fontSize: '0.9rem', color: 'var(--text-muted)' }}>/ {max}</span></div>
          <div className="kpi-label">Promedio post-test</div>
          <div className="kpi-sub">{comparison.postCount} respuestas</div>
        </div>
        <div className={`kpi-card glass-panel ${comparison.averageDelta > 0 ? 'positive' : comparison.averageDelta < 0 ? 'negative' : ''}`}>
          <i className="fa-solid fa-arrow-trend-up"></i>
          <div className="kpi-value">{comparison.averageDelta > 0 ? '+' : ''}{comparison.averageDelta}</div>
          <div className="kpi-label">Mejora promedio</div>
          <div className="kpi-sub">Post menos pre</div>
        </div>
        <div className="kpi-card glass-panel">
          <i className="fa-solid fa-users"></i>
          <div className="kpi-value">{comparison.pairedCount}</div>
          <div className="kpi-label">Pareados</div>
          <div className="kpi-sub">Estudiantes con ambas encuestas</div>
        </div>
      </div>

      <div className="admin-toolbar">
        <button className="btn-secondary" onClick={onExport}>
          <i className="fa-solid fa-file-csv"></i> Exportar CSV
        </button>
      </div>

      <div className="admin-table glass-panel">
        <h3 style={{ padding: '1rem 0.75rem 0', fontSize: '0.95rem' }}>
          <i className="fa-solid fa-layer-group"></i> Resultados por dimension
        </h3>
        {dimensions.length === 0 ? (
          <p style={{ textAlign: 'center', padding: '1.5rem', color: 'var(--text-muted)' }}>Sin dimensiones registradas</p>
        ) : (
          <table>
            <thead>
              <tr>
                <th>Dimension</th>
                <th>Pre</th>
                <th>Post</th>
                <th>Delta</th>
                <th style={{ width: '30%' }}>Comparacion</th>
              </tr>
            </thead>
            <tbody>
              {dimensions.map(d => {
                const pre = comparison.preDimensions?.[d] ?? 0
                const post = comparison.postDimensions?.[d] ?? 0
                const delta = comparison.dimensionDelta?.[d] ?? 0
                return (
                  <tr key={d}>
                    <td>{label(d)}</td>
                    <td>{pre}</td>
                    <td>{post}</td>
                    <td className={delta > 0 ? 'delta-positive' : delta < 0 ? 'delta-negative' : ''}>
                      {delta > 0 ? '+' : ''}{delta}
                    </td>
                    <td>
                      <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
                        <div className="dimension-bar"><div style={{ width: `${Math.min(100, (pre / 5) * 100)}%` }}></div></div>
                        <div className="dimension-bar post"><div style={{ width: `${Math.min(100, (post / 5) * 100)}%` }}></div></div>
                      </div>
                    </td>
                  </tr>
                )
              })}
            </tbody>
          </table>
        )}
      </div>

      <div className="admin-table glass-panel">
        <h3 style={{ padding: '1rem 0.75rem 0', fontSize: '0.95rem' }}>
          <i className="fa-solid fa-user-graduate"></i> Resultado por estudiante
        </h3>
        {comparison.paired.length === 0 ? (
          <p style={{ textAlign: 'center', padding: '1.5rem', color: 'var(--text-muted)' }}>
            Ningun estudiante tiene pre-test y post-test a la vez
          </p>
        ) : (
          <table>
            <thead>
              <tr>
                <th>Estudiante</th>
                <th>Grado</th>
                <th>Pre</th>
                <th>Post</th>
                <th>Delta</th>
                <th>Progreso</th>
              </tr>
            </thead>
            <tbody>
              {comparison.paired.map(p => {
                const pct = Math.round(((p.post - p.pre) / max) * 100)
                return (
                  <tr key={p.user}>
                    <td>{p.user}</td>
                    <td>{p.grade || '-'}</td>
                    <td>{p.pre}</td>
                    <td><strong>{p.post}</strong></td>
                    <td className={p.delta > 0 ? 'delta-positive' : p.delta < 0 ? 'delta-negative' : ''}>
                      {p.delta > 0 ? '+' : ''}{p.delta}
                    </td>
                    <td style={{ fontSize: '0.8rem' }}>
                      {pct > 0 ? `+${pct}%` : `${pct}%`}
                    </td>
                  </tr>
                )
              })}
            </tbody>
          </table>
        )}
      </div>
    </>
  )
}
