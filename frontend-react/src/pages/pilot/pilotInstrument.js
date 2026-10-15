export const LIKERT_OPTIONS = [
  { value: 1, label: 'Totalmente en desacuerdo' },
  { value: 2, label: 'En desacuerdo' },
  { value: 3, label: 'Neutral' },
  { value: 4, label: 'De acuerdo' },
  { value: 5, label: 'Totalmente de acuerdo' },
]

export const PILLAR_LABELS = {
  descomposicion: 'Descomposición',
  reconocimiento_patrones: 'Reconocimiento de patrones',
  algoritmos: 'Pensamiento algorítmico',
  abstraccion: 'Abstracción',
}

export const TEST_QUESTIONS = [
  { id: 1, pillar: 'descomposicion', text: 'Divido un problema grande en partes más pequeñas antes de resolverlo.' },
  { id: 2, pillar: 'descomposicion', text: 'Ante una tarea compleja, identifico sus componentes principales.' },
  { id: 3, pillar: 'descomposicion', text: 'Prefiero resolver un problema paso por paso que intentarlo todo de una vez.' },
  { id: 4, pillar: 'reconocimiento_patrones', text: 'Detecto similitudes entre problemas que he resuelto antes.' },
  { id: 5, pillar: 'reconocimiento_patrones', text: 'Reconozco regularidades en una serie de datos o de pasos.' },
  { id: 6, pillar: 'reconocimiento_patrones', text: 'Cuando algo se repite, intento encontrar la regla que lo explica.' },
  { id: 7, pillar: 'algoritmos', text: 'Sé definir una secuencia de pasos para resolver un problema.' },
  { id: 8, pillar: 'algoritmos', text: 'Ante un problema, pienso o escribo el procedimiento antes de ejecutarlo.' },
  { id: 9, pillar: 'algoritmos', text: 'Puedo explicar con claridad el orden de los pasos que seguí.' },
  { id: 10, pillar: 'abstraccion', text: 'Identifico la información esencial e ignoro los detalles irrelevantes.' },
  { id: 11, pillar: 'abstraccion', text: 'Puedo representar un problema con un esquema o diagrama simple.' },
  { id: 12, pillar: 'abstraccion', text: 'Simplifico un problema para concentrarme en lo importante.' },
]

export const MOTIVATION_ITEMS = [
  { id: 1, text: 'Me pareció interesante aprender con misiones, puntos y niveles.' },
  { id: 2, text: 'Me motivó seguir avanzando de nivel.' },
  { id: 3, text: 'La retroalimentación del asistente de IA me ayudó a corregir mis errores.' },
  { id: 4, text: 'Me sentí curioso por las respuestas de la IA.' },
  { id: 5, text: 'Creo que esta forma de aprender me ayudaría a entender mejor la lógica y la programación.' },
  { id: 6, text: 'Me gustaría seguir usando la aplicación.' },
]

export const EXPERT_CRITERIA = [
  { key: 'pertinencia', label: 'Pertinencia del contenido para 8.° y 9.°' },
  { key: 'claridad', label: 'Claridad del lenguaje' },
  { key: 'dificultad', label: 'Nivel de dificultad' },
  { key: 'retroalimentacion_ia', label: 'Calidad de la retroalimentación de la IA' },
  { key: 'diseno_gamificado', label: 'Diseño gamificado' },
  { key: 'facilidad_uso', label: 'Facilidad de uso' },
  { key: 'aplicabilidad_aula', label: 'Aplicabilidad en el aula' },
]

export const EXPERT_VERDICTS = [
  { value: 'aprobada', label: 'Aprobada' },
  { value: 'aprobada_con_ajustes', label: 'Aprobada con ajustes' },
  { value: 'requiere_cambios', label: 'Requiere cambios' },
]
