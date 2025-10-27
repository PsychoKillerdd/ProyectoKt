# 🔧 Guía: Activar Samsung Health Developer Mode

## ⚠️ IMPORTANTE
**El Developer Mode es SOLO para pruebas durante el desarrollo.**
**NO debe usarse en producción ni compartirse con usuarios finales.**

## 📱 Pasos para Activar Developer Mode

### 1. Abrir Samsung Health
- Abre la app Samsung Health en tu dispositivo

### 2. Acceder a Settings
- Toca el botón '⋮' (tres puntos) en la esquina superior derecha
- Selecciona **Settings** (Configuración)

### 3. Ir a About Samsung Health
- En Settings, busca y toca **About Samsung Health**

### 4. Activar Developer Mode
- Toca **rápidamente 10 veces o más** en la línea de versión
- Si lo haces correctamente, aparecerá el botón:
  ```
  Developer mode (Samsung Health Data SDK)
  ```

### 5. Habilitar el Modo
- Toca **Developer mode (Samsung Health Data SDK)**
- **Acepta** el Notice of usage of the Developer mode
- Activa **Developer Mode for Data Read** ON

### 6. (Opcional) Para Escritura de Datos
Si quieres ESCRIBIR datos (requiere Access Code de Samsung):
- Necesitas el **Access Code** que recibirás tras la aprobación
- Ingresa:
  - **Client ID**: `com.example.proyectotitulo.healthapp` (tu package name)
  - **Access Code**: El código que Samsung te envíe

## ✅ Verificación

Después de activar:
1. Deberías ver "Developer Mode: ON" en Settings
2. Tu app podrá leer datos de Samsung Health
3. Verás logs exitosos cuando la app se conecte

## 🚀 Qué Puedes Hacer Ahora

### Con Developer Mode Activado:
✅ **Leer datos reales:**
- Pasos
- Frecuencia cardíaca
- Sueño
- Peso/Altura
- Ejercicio

❌ **Escribir datos:**
- Necesitas Access Code (tras aprobación de Samsung)

## 📝 Notas Importantes

1. **Solo para Testing:**
   - Este modo es para desarrollo
   - Desactívalo antes de distribución

2. **Requiere SDK Real:**
   - Necesitas el Samsung Health SDK oficial
   - El archivo `.aar` actual no es suficiente

3. **Alternativa Temporal:**
   - Usa Health Connect mientras esperas
   - No requiere aprobación
   - Lee los mismos datos de Samsung Health

## 🔗 Enlaces Útiles

- **Download SDK**: https://developer.samsung.com/health/android/data/guide/getting-started.html
- **Partnership Request**: https://developer.samsung.com/health/android/data/partnership-request.html
- **Developer Support**: https://forum.developer.samsung.com/c/samsung-health/69

---

## ⏳ Mientras Esperas la Aprobación

### Opción A: Health Connect (Recomendado)
```kotlin
// Usa Health Connect API
// Lee datos reales AHORA
// Sin esperar aprobación
```

### Opción B: Mock Data con UI Completa
```kotlin
// Desarrolla toda la UI
// Sistema de alertas
// Gráficas y visualizaciones
// Reemplaza datos cuando tengas SDK
```

### Opción C: Desarrollar con Emulador
```kotlin
// Prueba toda la lógica
// Valida flujos de usuario
// Prepara para datos reales
```
