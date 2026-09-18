const mongoose = require('mongoose');
const dotenv = require('dotenv');
const Challenge = require('../models/challenge.model');
const User = require('../models/user.model');

dotenv.config();

const challenges = [
  {
    title: 'Descomposición Alimentaria',
    description: 'Ordena secuencialmente los pasos atómicos lógicos para preparar un sándwich perfecto.',
    type: 'decomposition',
    difficulty: 1,
    xpReward: 100,
    pointsReward: 10,
    badgeName: 'Explorador Digital',
    order: 1,
    content: {
      type: 'drag-drop',
      items: [
        { id: 'step1', text: '🛒 1. Sacar dos rebanadas de pan de molde' },
        { id: 'step2', text: '🛒 2. Untar mantequilla o aderezos' },
        { id: 'step3', text: '🛒 3. Colocar los ingredientes sobre la rebanada' },
        { id: 'step4', text: '🛒 4. Cerrar con la otra rodaja y servir' }
      ],
      correctOrder: ['step1', 'step2', 'step3', 'step4']
    }
  },
  {
    title: 'Reconocimiento de Patrones',
    description: 'Encuentra la constante oculta en la serie mostrada.',
    type: 'patterns',
    difficulty: 2,
    xpReward: 100,
    pointsReward: 30,
    badgeName: 'Maestro de Patrones',
    order: 2,
    content: {
      type: 'quiz',
      questions: [
        { q: 'Encuentra el patrón numérico: 2, 4, 6, 8, ?', a: '10', opts: ['9', '10', '12', '14'] },
        { q: 'Identifica la secuencia: 1, 3, 9, 27, ?', a: '81', opts: ['36', '45', '60', '81'] },
        { q: 'Siguiente elemento: A, C, E, G, ?', a: 'I', opts: ['H', 'I', 'J', 'K'] },
        { q: 'Patrón alternado: 10, 5, 20, 10, 40, ?', a: '20', opts: ['15', '20', '50', '80'] },
        { q: 'Secuencia Fibonacci básica: 1, 1, 2, 3, 5, ?', a: '8', opts: ['6', '7', '8', '9'] },
        { q: 'Patrón binario: 1, 2, 4, 8, 16, ?', a: '32', opts: ['24', '30', '32', '64'] },
        { q: 'Resta constante: 50, 45, 40, 35, ?', a: '30', opts: ['25', '30', '32', '34'] },
        { q: 'Símbolos lógicos: [], [][], [][][], ?', a: '[][][][]', opts: ['[]', '[][][][]', '[][][][][]', 'none'] },
        { q: 'Secuencia de saltos: 5, 15, 25, 35, ?', a: '45', opts: ['40', '45', '50', '55'] },
        { q: 'Lógica extrema: 2, 3, 5, 7, 11, ?', a: '13', opts: ['12', '13', '14', '15'] }
      ],
      minCorrect: 7
    }
  },
  {
    title: 'Abstracción de Navegación',
    description: '¿Qué elemento de la realidad debes IGNORAR por completo al aplicar abstracción?',
    type: 'abstraction',
    difficulty: 2,
    xpReward: 100,
    pointsReward: 10,
    badgeName: 'Experto en Abstracción',
    order: 3,
    content: {
      type: 'multiple-select',
      scenario: 'Simulador de Rutas Urbanas',
      scenarioIcon: 'fa-car-side',
      options: [
        'El color de los carros en la vía',
        'Las coordenadas de las calles principales',
        'Los nombres de los edificios públicos',
        'La cantidad de árboles en los jardines de los vecinos'
      ],
      correctAnswers: ['El color de los carros en la vía', 'La cantidad de árboles en los jardines de los vecinos']
    }
  },
  {
    title: 'Arquitectura de Algoritmos',
    description: 'Construye la secuencia estándar de un programa informático de procesamiento lógico.',
    type: 'algorithms',
    difficulty: 2,
    xpReward: 100,
    pointsReward: 10,
    badgeName: 'Arquitecto de Algoritmos',
    order: 4,
    content: {
      type: 'drag-drop',
      items: [
        { id: 'alg1', text: '⚙️ Inicio' },
        { id: 'alg2', text: '⚙️ Leer datos de entrada' },
        { id: 'alg3', text: '⚙️ Procesar variables lógicas' },
        { id: 'alg4', text: '⚙️ Mostrar resultado en pantalla' },
        { id: 'alg5', text: '⚙️ Fin del programa' }
      ],
      correctOrder: ['alg1', 'alg2', 'alg3', 'alg4', 'alg5']
    }
  }
];

const seedAdmin = {
  name: 'Administrador',
  email: 'admin@computaquest.com',
  password: 'admin123',
  role: 'admin',
  avatar: 'avatar1'
};

async function seedDB() {
  try {
    await mongoose.connect(process.env.MONGODB_URI);
    console.log('Conectado a MongoDB');

    await Challenge.deleteMany({});
    await User.deleteMany({ role: 'admin' });
    
    await Challenge.insertMany(challenges);
    console.log('Retos insertados correctamente');

    const adminExists = await User.findOne({ email: seedAdmin.email });
    if (!adminExists) {
      await User.create(seedAdmin);
      console.log('Admin creado: admin@computaquest.com / admin123');
    }

    console.log('Seed completado exitosamente');
    process.exit(0);
  } catch (error) {
    console.error('Error en seed:', error);
    process.exit(1);
  }
}

seedDB();
