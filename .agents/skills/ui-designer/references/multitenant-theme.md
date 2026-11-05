# PROTECCIÓN Y ARQUITECTURA DEL TEMA MULTIEMPRESA

## 1. Mapeo de la Arquitectura de Temas

La personalización visual en Formas Perfectas Android es **100% dinámica y gobernada por el Backend**. La aplicación no posee paletas corporativas hardcodeadas por cliente; en su lugar, recibe la configuración desde el servidor en función de la empresa del usuario autenticado.

```
+-----------------------------------------------------------------------------------+
| FLUJO DE PERSONALIZACIÓN MULTIEMPRESA                                            |
+-----------------------------------------------------------------------------------+
| 1. JWT (Claim IdEmpresa) -> TokenStorage EncryptedSharedPreferences               |
| 2. AuthInterceptor -> Adjunta Header 'Authorization: Bearer <token>' + 'X-Platform: 3' |
| 3. ApiService: GET /api/Tema -> Retorna DTO TemaEmpresa                           |
| 4. Repository / ViewModel -> Invoca TemaStore.setTema(tema)                        |
| 5. TemaStore (object Singleton StateFlow<TemaEmpresa?>)                          |
| 6. AppDynamicTheme (@Composable Wrapper en MainActivity.kt)                       |
| 7. MaterialTheme(colorScheme = if (darkTheme) darkColors else lightColors)        |
+-----------------------------------------------------------------------------------+
```

---

## 2. Componentes Clave del Sistema de Temas

### A. [TemaEmpresa.kt](file:///D:/Personal/Git-Proyectos/formasPerfectasAndroid/app/src/main/java/com/spa/appointments/domain/model/TemaEmpresa.kt) (DTO de Dominio)
Representa la configuración visual remota de la empresa:
```kotlin
@JsonClass(generateAdapter = true)
data class TemaEmpresa(
    @Json(name = "colorPrimario")   val colorPrimario: String,   // HEX (ej: "6650A4")
    @Json(name = "colorSecundario") val colorSecundario: String, // HEX (ej: "625B71")
    @Json(name = "colorTerciario")  val colorTerciario: String,  // HEX (ej: "7D5260")
    @Json(name = "logoUrl")         val logoUrl: String?,
    @Json(name = "nombreApp")       val nombreApp: String,
    @Json(name = "slogan")          val slogan: String?,
    @Json(name = "nombreEmpresa")   val nombreEmpresa: String,
    @Json(name = "mensajeWhatsApp") val mensajeWhatsApp: String?
)
```

### B. [TemaStore.kt](file:///D:/Personal/Git-Proyectos/formasPerfectasAndroid/app/src/main/java/com/spa/appointments/core/theme/TemaStore.kt) (Singleton en Memoria)
Guarda el estado reactivo del tema de la empresa activa:
```kotlin
object TemaStore {
    private val _tema = MutableStateFlow<TemaEmpresa?>(null)
    val tema: StateFlow<TemaEmpresa?> = _tema

    fun setTema(tema: TemaEmpresa) { _tema.value = tema }
    fun getTema(): TemaEmpresa? = _tema.value
    fun limpiar() { _tema.value = null }
}
```

### C. [ColorUtils.kt](file:///D:/Personal/Git-Proyectos/formasPerfectasAndroid/app/src/main/java/com/spa/appointments/core/theme/ColorUtils.kt) (Parsea HEX a Color Compose)
Convierte la cadena recibida en la API a `androidx.compose.ui.graphics.Color` con fallback seguro:
```kotlin
fun hexToColor(hex: String): Color {
    return try {
        val cleanHex = hex.trimStart('#').padStart(6, '0')
        Color(android.graphics.Color.parseColor("#$cleanHex"))
    } catch (e: Exception) {
        Color(0xFF6650A4) // Morado Material por defecto
    }
}
```

### D. [AppTheme.kt](file:///D:/Personal/Git-Proyectos/formasPerfectasAndroid/app/src/main/java/com/spa/appointments/ui/theme/AppTheme.kt) (AppDynamicTheme Composable)
Envoltorio principal que construye el `ColorScheme` de Material 3 dinámicamente:
- **Primary**: Derivado de `colorPrimario` (o `#6650A4`).
- **Secondary**: Derivado de `colorSecundario` (o `#625B71`).
- **Tertiary**: Derivado de `colorTerciario` (o `#7D5260`).
- **Containers**: Se calculan aplicando opacidad alfa al color correspondiente (`alpha = 0.12f` en Light, `alpha = 0.2f` en Dark).
- **Surface / Background**: Colores neutros estándar M3 (`#FFFFFBFE` en Light / `#1C1B1F` en Dark).

---

## 3. Reglas de Oro para Evitar Rupturas Multitenant

1. **PROHIBICIÓN TOTAL DE HEX LITERALEZ EN UI**:
   - ❌ **INCORRECTO**: `Button(colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6650A4)))`
   - ✅ **CORRECTO**: `Button(colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary))`

2. **USO DE CONTENEDORES DERIVADOS**:
   - Para fondos suaves o badges de iconos, utilizar los tokens de contenedor del tema:
   - ✅ **CORRECTO**: `MaterialTheme.colorScheme.primaryContainer` o `MaterialTheme.colorScheme.secondaryContainer`.
   - Para textos sobre dichos contenedores, utilizar siempre `onPrimaryContainer` o `onSecondaryContainer`.

3. **AISLAMIENTO Y LIMPIEZA**:
   - Al cerrar sesión (`logout`), es imperativo invocar `TemaStore.limpiar()`.
   - Al iniciar sesión, la app debe cargar el nuevo tema mediante `temaRepository.getTema()` e inyectarlo en `TemaStore.setTema(...)`.
   - Los composables reutilizables nunca deben almacenar variables `var colorEmpresa = ...` estáticas fuera del árbol de composición.

4. **ARCHIVOS PROTEGIDOS (RESTRICCIÓN DE MODIFICACIÓN)**:
   - [TemaStore.kt](file:///D:/Personal/Git-Proyectos/formasPerfectasAndroid/app/src/main/java/com/spa/appointments/core/theme/TemaStore.kt)
   - [AppTheme.kt](file:///D:/Personal/Git-Proyectos/formasPerfectasAndroid/app/src/main/java/com/spa/appointments/ui/theme/AppTheme.kt)
   - [TemaEmpresa.kt](file:///D:/Personal/Git-Proyectos/formasPerfectasAndroid/app/src/main/java/com/spa/appointments/domain/model/TemaEmpresa.kt)
   - [AuthInterceptor.kt](file:///D:/Personal/Git-Proyectos/formasPerfectasAndroid/app/src/main/java/com/spa/appointments/data/remote/AuthInterceptor.kt)
   - [TokenStorage.kt](file:///D:/Personal/Git-Proyectos/formasPerfectasAndroid/app/src/main/java/com/spa/appointments/core/security/TokenStorage.kt)
