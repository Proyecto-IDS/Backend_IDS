# GitHub Secrets Setup Guide

Para desplegar esta aplicación, solo necesitas configurar estos **GitHub Secrets**:

## 🔑 Secretos OBLIGATORIOS:

- `GEMINI_API_KEY` - Tu API key de Google Gemini AI
- `DB_PASSWORD` - Password de la base de datos PostgreSQL

## 🌐 Variables de Database (cuando despliegues la DB):

- `DB_HOST` - Host de tu base de datos (ej: tu-app.railway.app)  
- `DB_USERNAME` - Usuario de la DB (ej: postgres)
- `DB_NAME` - Nombre de la database (ej: ids_database)

## ✅ Variables que YA tienen defaults (opcionales):

- `GOOGLE_CLIENT_ID` - Ya tiene el valor actual
- `INITIAL_ADMINS` - Ya tiene vuestros emails  
- `ML_ENDPOINT` - Ya apunta al endpoint actual
- `ADMIN_PASSWORD` - Ya tiene un default
- `SERVER_PORT` - Ya es 8080

¡Solo necesitas 2 secretos obligatorios! 🎉