import { useState, useEffect } from 'react'
import api from '../services/api'
import { useToast } from '../context/ToastContext'

export default function AdminPage() {
  const [tab, setTab] = useState('users')
  const [users, setUsers] = useState([])
  const [challenges, setChallenges] = useState([])
  const [surveys, setSurveys] = useState([])
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

  useEffect(() => {
    loadData()
  }, [])

  const loadData = async () => {
    try {
      const [usersData, challengesData, surveysData] = await Promise.all([
        api.getUsers(),
        api.getChallenges(),
        api.getAllSurveys(),
      ])
      setUsers(usersData.users || [])
      setChallenges(challengesData)
      setSurveys(surveysData || [])
    } catch (error) {
      toast.error('Error al cargar datos')
    } finally {
      setLoading(false)
    }
  }

  const handleDeleteUser = async (id) => {
    if (!confirm('¿Eliminar este usuario?')) return
    try {
      await api.deleteUser(id)
      setUsers(prev => prev.filter(u => u.id !== id))
      toast.success('Usuario eliminado')
    } catch (error) {
      toast.error('Error al eliminar usuario')
    }
  }

  const handleDeleteChallenge = async (id) => {
    if (!confirm('¿Eliminar este reto?')) return
    try {
      await api.deleteChallenge(id)
      setChallenges(prev => prev.filter(c => c.id !== id))
      toast.success('Reto eliminado')
    } catch (error) {
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
      const { contentStr, ...rest } = newChallenge
      const data = await api.createChallenge({ ...rest, content: parsedContent })
      setChallenges(prev => [...prev, data])
      setNewChallenge({
        title: '', description: '', type: 'decomposition',
        difficulty: 1, xpReward: 100, pointsReward: 10, badgeName: '', contentStr: '{}',
      })
      toast.success('Reto creado exitosamente')
    } catch (error) {
      toast.error('Error al crear reto')
    }
  }

  if (loading) return <div className="loading-screen"><div className="spinner"></div></div>

  return (
    <div className="admin-container">
      <h1 className="page-title"><i className="fa-solid fa-gear"></i> Panel de Administracion</h1>

      <div className="admin-tabs">
        <button className={`tab-btn ${tab === 'users' ? 'active' : ''}`} onClick={() => setTab('users')}>
          <i className="fa-solid fa-users"></i> Usuarios ({users.length})
        </button>
        <button className={`tab-btn ${tab === 'challenges' ? 'active' : ''}`} onClick={() => setTab('challenges')}>
          <i className="fa-solid fa-gamepad"></i> Retos ({challenges.length})
        </button>
        <button className={`tab-btn ${tab === 'surveys' ? 'active' : ''}`} onClick={() => setTab('surveys')}>
          <i className="fa-solid fa-clipboard-list"></i> Encuestas ({surveys.length})
        </button>
      </div>

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
                  <input type="text" value={newChallenge.title} onChange={e => setNewChallenge({...newChallenge, title: e.target.value})} required />
                </div>
                <div className="input-group">
                  <label>Descripcion</label>
                  <textarea value={newChallenge.description} onChange={e => setNewChallenge({...newChallenge, description: e.target.value})} required />
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
                  const maxScore = 50
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
