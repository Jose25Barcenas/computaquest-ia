export const CHALLENGE_TYPES = {
  DECOMPOSITION: {
    id: 'DECOMPOSITION',
    key: 'decomposition',
    icon: 'fa-puzzle-piece',
    color: '#6366f1',
    label: 'Descomposición',
  },
  PATTERNS: {
    id: 'PATTERNS',
    key: 'patterns',
    icon: 'fa-magnifying-glass',
    color: '#10b981',
    label: 'Patrones',
  },
  ABSTRACTION: {
    id: 'ABSTRACTION',
    key: 'abstraction',
    icon: 'fa-filter',
    color: '#f59e0b',
    label: 'Abstracción',
  },
  ALGORITHMS: {
    id: 'ALGORITHMS',
    key: 'algorithms',
    icon: 'fa-code',
    color: '#ef4444',
    label: 'Algoritmos',
  },
}

export const TYPE_INFO = {
  decomposition: CHALLENGE_TYPES.DECOMPOSITION,
  patterns: CHALLENGE_TYPES.PATTERNS,
  abstraction: CHALLENGE_TYPES.ABSTRACTION,
  algorithms: CHALLENGE_TYPES.ALGORITHMS,
}

export const MODULES = Object.values(CHALLENGE_TYPES)
