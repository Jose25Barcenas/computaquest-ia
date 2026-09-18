# Despliegue en Railway.app

Guia paso a paso para desplegar ComputaQuest IA en Railway.

## Prerequisitos

1. Cuenta en [Railway.app](https://railway.app)
2. Cuenta en [OpenAI](https://platform.openai.com) (para la API key)
3. Git instalado

## Paso 1: Preparar el repositorio

```bash
# Clonar o copiar el proyecto
git init
git add .
git commit -m "Initial commit for Railway deployment"
```

## Paso 2: Crear proyecto en Railway

1. Ve a [railway.app](https://railway.app) e inicia sesion
2. Haz clic en **"New Project"**
3. Selecciona **"Deploy from GitHub repo"** (conecta tu cuenta de GitHub)
   - O selecciona **"Empty Project"** si quieres subir manualmente

## Paso 3: Agregar MongoDB

1. En tu proyecto de Railway, haz clic en **"+ New"**
2. Selecciona **"Database"** > **"MongoDB"**
3. Railway creara automaticamente las variables:
   - `MONGO_URL` (se usa como `MONGODB_URI`)

## Paso 4: Configurar variables de entorno

En la pestaña **"Variables"** del proyecto, agrega:

```
MONGODB_URI=mongodb://mongo:tu_password@mongo.railway.internal:27017/computaquest?authSource=admin
JWT_SECRET=tu-secreto-super-seguro-aqui
JWT_EXPIRATION_MS=604800000
OPENAI_API_KEY=sk-tu-api-key-de-openai
FRONTEND_URL=https://tu-proyecto.up.railway.app
SPRING_PROFILES_ACTIVE=prod
```

**NOTA:** Las credenciales de MongoDB vienen en la variable `MONGO_URL`. Railway te la da automaticamente.

## Paso 5: Configurar el servicio Backend

1. Haz clic en **"+ New"** > **"GitHub Repo"** o **"Empty Project"**
2. Selecciona tu repositorio
3. Railway detectara automaticamente el `railway.json`
4. En **Settings**:
   - **Root Directory:** `./backend` (si usas monorepo)
   - **Dockerfile Path:** `./backend/Dockerfile`

## Paso 6: Configurar el servicio Frontend (Opcional)

Si quieres servir el frontend desde Railway:

1. **"+ New"** > **"GitHub Repo"**
2. **Root Directory:** `./frontend-react`
3. Railway usara el Dockerfile automaticamente

**Alternativa recomendada:** Usa un servicio separado para el frontend (Vercel o Netlify son gratuitos y mas rapidos para React).

## Paso 7: Dominio personalizado

1. Ve a **Settings** > **Networking**
2. Haz clic en **"Custom Domain"**
3. Agrega tu dominio: `computaquest.tudominio.edu`
4. Railway te proporcionara un CNAME record
5. En tu DNS, agrega:
   ```
   Tipo: CNAME
   Nombre: computaquest
   Valor: tu-proyecto.up.railway.app
   ```

## Paso 8: Generar codigo QR

Una vez desplegado, accede a:
```
https://tu-proyecto.up.railway.app/access
```

Desde ahi puedes:
- Ver el codigo QR
- Copiar la URL
- Descargar el QR como imagen

### Imprimir el QR para los ninos

1. Descarga la imagen del QR
2. Imprime en papel tamaño carta o A4
3. Coloca en un lugar visible
4. Los ninos escanean con la camara del celular

## Estructura de archivos creada

```
App_Proyecto_grado/
├── railway.json                    # Configuracion de Railway
├── docker-compose.prod.yml         # Compose para produccion
├── .railwayignore                  # Archivos ignorados
├── frontend-react/
│   ├── nginx.conf                  # Configuracion Nginx actualizada
│   ├── src/
│   │   ├── components/
│   │   │   └── QRCode/
│   │   │       └── QRCode.jsx      # Componente QR
│   │   └── pages/
│   │       └── QuickAccessPage.jsx # Pagina de acceso rapido
│   └── Dockerfile
└── backend/
    ├── Dockerfile
    └── src/main/resources/
        └── application.yml
```

## Comandos utiles

```bash
# Desplegar localmente con Docker Compose
docker-compose -f docker-compose.prod.yml --env-file .env up -d

# Ver logs de Railway
railway logs

# Variables de entorno
railway variables
```

## Solucion de problemas

### Error de conexion a MongoDB
- Verifica que la variable `MONGODB_URI` este correctamente configurada
- Asegurate de que MongoDB este corriendo en Railway

### Frontend no carga
- Verifica que `FRONTEND_URL` coincida con tu URL de Railway
- Revisa los logs del servicio frontend

### API no responde
- Verifica que el backend este corriendo
- Revisa que `SPRING_PROFILES_ACTIVE=prod` este configurado

## Plan gratuito de Railway

- **500 horas/mes** de uso
- **$5 de creditos** mensuales
- Suficiente para el proyecto y pruebas

## Contacto

Para soporte con el despliegue, contacta al equipo de desarrollo.
