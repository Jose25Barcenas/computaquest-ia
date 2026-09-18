# ComputaQuest IA

Plataforma educativa de gamificación para enseñar pensamiento computacional a estudiantes de octavo y noveno grado, utilizando Inteligencia Artificial generativa.

## Estructura del Proyecto

```
App_Proyecto_grado/
├── backend/                          # API REST Spring Boot (Java 17)
│   ├── src/main/java/com/computaquest/
│   │   ├── config/                   # Security, CORS, OpenAI config
│   │   ├── model/                    # Entidades MongoDB
│   │   ├── enums/                    # Role, ChallengeType, MessageRole
│   │   ├── repository/               # Spring Data MongoDB
│   │   ├── dto/                      # Objetos de transferencia
│   │   ├── security/                 # JWT token, filter, UserDetailsService
│   │   ├── service/                  # Lógica de negocio
│   │   ├── controller/               # Endpoints REST
│   │   ├── exception/                # Manejo centralizado de errores
│   │   └── seed/                     # Datos iniciales
│   ├── src/main/resources/
│   │   └── application.yml           # Configuración
│   ├── src/test/java/com/computaquest/
│   ├── pom.xml
│   └── Dockerfile
├── frontend-react/                   # Frontend React 18 + Vite
│   ├── src/
│   │   ├── components/               # Componentes reutilizables
│   │   │   ├── Navbar/               # Navegación
│   │   │   ├── Chat/                 # Tutor IA
│   │   │   ├── Leaderboard/          # Tabla de clasificación
│   │   │   ├── RadarChart/           # Gráfico de habilidades
│   │   │   ├── Toast/                # Notificaciones
│   │   │   └── ChallengeTypes/       # DragDrop, Quiz, MultipleSelect
│   │   ├── pages/                    # Páginas principales
│   │   ├── context/                  # Auth, Theme, Toast
│   │   ├── services/                 # API service
│   │   └── styles/                   # CSS con Glassmorphism
│   ├── package.json
│   ├── vite.config.js
│   └── Dockerfile
├── docker-compose.yml                # Orquestación completa
└── README.md
```

## Requisitos Previos

- Java 17+
- Maven 3.8+
- Node.js 18+
- MongoDB 6+ (local o Atlas)
- Docker (opcional)

## Instalación

### Opción 1: Docker (Recomendado)

```bash
# Configurar variables de entorno
export OPENAI_API_KEY=tu-api-key

# Ejecutar todo
docker-compose up --build
```

- Frontend: http://localhost:3000
- Backend: http://localhost:4000
- MongoDB: localhost:27017

### Opción 2: Desarrollo Manual

```bash
# Terminal 1 - Backend
cd backend
mvn spring-boot:run

# Terminal 2 - Frontend
cd frontend-react
npm install
npm run dev
```

## Variables de Entorno

### Backend (application.yml o variables de entorno)

| Variable | Descripción | Default |
|----------|-------------|---------|
| `MONGODB_URI` | URI de conexión MongoDB | `mongodb://localhost:27017/computaquest` |
| `JWT_SECRET` | Secreto para tokens JWT | (configurar en producción) |
| `JWT_EXPIRATION_MS` | Expiración del token (ms) | `604800000` (7 días) |
| `OPENAI_API_KEY` | API Key de OpenAI | - |
| `FRONTEND_URL` | URL del frontend (CORS) | `http://localhost:3000` |

## Credenciales de Prueba

- **Admin:** admin@computaquest.com / admin123

## Funcionalidades Implementadas

### Backend (Spring Boot)
- ✅ Autenticación JWT con Spring Security
- ✅ CRUD de retos educativos con permisos por rol
- ✅ Sistema de progreso y gamificación (XP, niveles, puntos)
- ✅ Chat IA con OpenAI API (Tutor educativo)
- ✅ Tabla de clasificación (leaderboard)
- ✅ Validación de datos con Jakarta Validation
- ✅ Manejo centralizado de errores
- ✅ Seed data automático
- ✅ Tests con JUnit 5 + MockMvc

### Frontend (React)
- ✅ React 18 con React Router v6
- ✅ Diseño responsive con Glassmorphism
- ✅ Modo oscuro/claro con persistencia
- ✅ Context API (Auth, Theme, Toast)
- ✅ Dashboard con gráfico radar (Chart.js)
- ✅ 4 retos interactivos (drag-drop, quiz, selección múltiple)
- ✅ Chat con Tutor IA en tiempo real
- ✅ Tabla de clasificación en vivo
- ✅ Panel de administración CRUD

### Retos Educativos (4 Pilares)
1. **Descomposición:** Ordenar pasos para preparar un sándwich
2. **Reconocimiento de Patrones:** Quiz de 10 preguntas de series lógicas
3. **Abstracción:** Identificar detalles irrelevantes en un mapa GPS
4. **Algoritmos:** Construir la estructura de un programa lógico

## API Endpoints

### Auth
| Método | Ruta | Auth | Descripción |
|--------|------|------|-------------|
| POST | `/api/auth/register` | No | Registrar usuario |
| POST | `/api/auth/login` | No | Iniciar sesión |
| GET | `/api/auth/me` | JWT | Obtener perfil |
| PUT | `/api/auth/profile` | JWT | Actualizar perfil |

### Challenges
| Método | Ruta | Auth | Descripción |
|--------|------|------|-------------|
| GET | `/api/challenges` | No | Listar retos |
| GET | `/api/challenges/:id` | No | Obtener reto |
| POST | `/api/challenges` | Admin | Crear reto |
| PUT | `/api/challenges/:id` | Admin | Actualizar reto |
| DELETE | `/api/challenges/:id` | Admin | Eliminar reto |

### Progress
| Método | Ruta | Auth | Descripción |
|--------|------|------|-------------|
| GET | `/api/progress` | JWT | Progreso del usuario |
| POST | `/api/progress/complete` | JWT | Completar reto |
| GET | `/api/progress/leaderboard` | JWT | Tabla de clasificación |

### Chat
| Método | Ruta | Auth | Descripción |
|--------|------|------|-------------|
| POST | `/api/chat/send` | JWT | Enviar mensaje al Tutor IA |
| GET | `/api/chat/history/:chatId` | JWT | Historial de chat |
| GET | `/api/chat/chats` | JWT | Listar chats del usuario |

### Users (Admin)
| Método | Ruta | Auth | Descripción |
|--------|------|------|-------------|
| GET | `/api/users` | Admin | Listar usuarios |
| DELETE | `/api/users/:id` | Admin | Eliminar usuario |

## Tecnologías

- **Backend:** Java 17, Spring Boot 3.2, Spring Data MongoDB, Spring Security, JWT (jjwt), Lombok
- **Frontend:** React 18, Vite, React Router, Chart.js, Font Awesome
- **IA:** OpenAI API (GPT-3.5-turbo)
- **BD:** MongoDB 6
- **Infra:** Docker, Docker Compose
- **Pruebas:** JUnit 5, MockMvc, TestContainers

## Autor

- Jose Armando Barcenas González
- Miguel Enrique Martínez
- Jose Luis Cerda Lora

**Universidad:** Corporación Universitaria Adventista
**Programa:** Ingeniería de Sistemas
