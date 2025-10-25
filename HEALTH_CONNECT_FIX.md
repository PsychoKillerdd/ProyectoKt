# Solución al problema de permisos de Health Connect

## Problema
La aplicación no muestra el diálogo de permisos de Health Connect y los logs muestran que los permisos están siendo denegados inmediatamente.

## Causa
Android cachea las decisiones de permisos denegados. Cuando cambias el `applicationId` o el manejo de permisos, necesitas limpiar este caché.

## Solución: Desinstalar e Instalar de Nuevo

### Opción 1: Desde Android Studio
1. En Android Studio, ve a: **Run** → **Stop 'app'**
2. En el dispositivo/emulador: **Settings** → **Apps** → **ProyectoTitulo** → **Uninstall**
3. En Android Studio: **Build** → **Clean Project**
4. Luego: **Build** → **Rebuild Project**
5. Finalmente: **Run** → **Run 'app'**

### Opción 2: Desde ADB (Terminal/PowerShell)
```powershell
# Desinstalar la app antigua
adb uninstall com.example.proyectotitulo.healthapp

# Limpiar el proyecto
cd "c:\Users\vicen\AndroidStudioProjects\ProyectoTitulo - Copy"
.\gradlew clean

# Compilar e instalar de nuevo
.\gradlew installDebug

# Iniciar la app
adb shell am start -n com.example.proyectotitulo.healthapp/.LoginActivity
```

### Opción 3: Desde el dispositivo Android
1. **Settings** → **Apps** → **ProyectoTitulo**
2. Toca **Uninstall** o **Desinstalar**
3. Confirma la desinstalación
4. Vuelve a Android Studio y ejecuta la app con **Run** → **Run 'app'**

## Verificar que Health Connect está instalado

1. Abre **Play Store** en tu dispositivo
2. Busca **"Health Connect"**
3. Si no está instalado, instálalo
4. Abre **Health Connect** y completa la configuración inicial
5. En Health Connect, ve a **App permissions** para verificar que tu app aparezca después de solicitar permisos

## Verificación post-instalación

Después de reinstalar:
1. Abre la app
2. Inicia sesión
3. En el Dashboard, presiona **"Actualizar"**
4. Deberías ver un diálogo de Health Connect solicitando permisos
5. Concede los permisos (Steps, Weight, Height)
6. Los datos deberían cargarse correctamente

## Cambios realizados en el código

### 1. AndroidManifest.xml
- Agregado `<queries>` para declarar el uso de Health Connect
- Agregado `<meta-data>` para vincular el archivo de permisos
- Configurado correctamente `HealthConnectActivity` con `activity-alias`

### 2. health_permissions.xml
- Actualizado para usar el formato correcto de Health Connect
- Incluye permisos para Steps, Weight y Height

### 3. DashboardActivity.kt
- Usar `HealthPermission.getReadPermission()` en lugar de strings
- Mejorado el manejo de errores y logging
- Agregados más logs para debugging

### 4. build.gradle.kts
- Cambiado `applicationId` a `com.example.proyectotitulo.healthapp` para forzar una nueva identidad de app

## Si el problema persiste

1. **Verifica los logs de Logcat:**
   ```
   Filtra por "HealthConnectApp" en Logcat
   ```

2. **Verifica que Health Connect esté actualizado:**
   - Play Store → Health Connect → Update (si está disponible)

3. **Prueba en un dispositivo físico:**
   - Los emuladores a veces tienen problemas con Health Connect
   - Un dispositivo real con Android 14+ es lo ideal

4. **Revisa los permisos manualmente:**
   - Settings → Apps → ProyectoTitulo → Permissions
   - Deberías ver una sección de "Health Connect" o similar

## Notas importantes

- **minSdk = 29** (Android 10+): Health Connect requiere Android 10 o superior
- **targetSdk = 36**: Estás usando la última versión de Android
- El cambio de `applicationId` es temporal. Una vez que funcione, puedes volver al ID original, pero deberás desinstalar de nuevo.

## Logs esperados después de la solución

```
D/HealthConnectApp: Health Connect SDK Status: 3
D/HealthConnectApp: Health Connect is available and ready.
D/HealthConnectApp: Update button clicked. Starting permission check...
D/HealthConnectApp: --- CHECKING PERMISSIONS ---
D/HealthConnectApp: Current granted permissions: []
D/HealthConnectApp: Required permissions: [android.permission.health.READ_STEPS, ...]
D/HealthConnectApp: Missing permissions found: [...]
D/HealthConnectApp: Attempting to request 3 permissions
D/HealthConnectApp: Permission request launched successfully

// Usuario concede permisos en el diálogo

D/HealthConnectApp: --- PERMISSION RESULT ---
D/HealthConnectApp: Permissions returned by dialog: [android.permission.health.READ_STEPS, ...]
D/HealthConnectApp: SUCCESS: All permissions were granted in this request.
```

## Contacto

Si después de seguir todos estos pasos el problema persiste, comparte:
1. Los logs completos de Logcat
2. La versión de Android de tu dispositivo
3. La versión de Health Connect instalada
