# CHECKLIST DE VALIDACIÓN UI/UX Y TEMA MULTIEMPRESA

Utilizar esta lista de comprobación antes de finalizar cualquier tarea de rediseño, creación o modificación visual en Formas Perfectas Android.

---

## 1. Verificación de Protección Multiempresa

- [ ] **Cero HEX Hardcodeados**: ¿Se verificó que no existan instancias de `Color(0xFF...)` para colores primarios, secundarios o de marca en la pantalla?
- [ ] **Tokens de MaterialTheme**: ¿Todos los componentes leen los colores desde `MaterialTheme.colorScheme` (`primary`, `surface`, `onPrimaryContainer`, `outlineVariant`, etc.)?
- [ ] **Respeto a AppDynamicTheme**: ¿La pantalla se compila dentro del árbol envuelto por `AppDynamicTheme` sin definir `MaterialTheme` locales alternativos?
- [ ] **Aislamiento de Sesión**: ¿Los componentes reutilizables evitan guardar variables estáticas de colores fuera de Compose?
- [ ] **Archivos Protegidos Intactos**: ¿Se verificó que `TemaStore.kt`, `AppTheme.kt`, `TemaEmpresa.kt`, `TokenStorage.kt` y `AuthInterceptor.kt` no hayan sido modificados?

---

## 2. Verificación de Sistema de Diseño Material 3

- [ ] **Jerarquía Tipográfica**: ¿Se utilizan estilos de `MaterialTheme.typography` (`titleMedium`, `bodyLarge`, `labelSmall`) en lugar de `fontSize` aislados sin estilo?
- [ ] **Escala de Espaciado**: ¿Los paddings y margins respetan la escala de 4dp / 8dp / 12dp / 16dp / 24dp?
- [ ] **Formas y Bordes**: ¿Las tarjetas e inputs utilizan `RoundedCornerShape` (12.dp / 14.dp / 16.dp) y bordes sutiles `outlineVariant` (0.5.dp a 1.dp)?
- [ ] **Elevación Plana**: ¿Se evita el uso excesivo de sombras pesadas (`elevation > 4.dp`), favoreciendo superficies planas M3?

---

## 3. Verificación de Estados de UI y Usabilidad

- [ ] **Estado Cargando (`Loading`)**: ¿Se muestra un `CircularProgressIndicator` centrado o dentro del botón mientras se realiza una operación en segundo plano?
- [ ] **Estado Vacío (`Empty`)**: ¿Si la lista o datos están vacíos, se muestra una ilustración/icono con mensaje aclaratorio en lugar de una pantalla en blanco?
- [ ] **Estado Error (`Error`)**: ¿Si la API falla, se presenta una tarjeta/banner de error con opción para reintentar o volver?
- [ ] **Estado Éxito (`Success`)**: ¿El contenido principal se renderiza de forma limpia y legible?

---

## 4. Verificación de Accesibilidad y Adaptabilidad

- [ ] **Área Táctica Ergónoma**: ¿Todos los botones, checkboxes y elementos clickeables miden al menos 48.dp de ancho/alto?
- [ ] **Modo Claro / Modo Oscuro**: ¿La pantalla se visualiza correctamente al cambiar la preferencia de tema oscuro en el dispositivo/preview?
- [ ] **Gestión de Teclado**: ¿Los campos de texto cierran o mueven el foco al presionar la acción del teclado (`ImeAction.Next`, `ImeAction.Done`)?

---

## 5. Verificación Arquitectónica y No Interferencia

- [ ] **Sin Lógica HTTP en UI**: ¿La vista está completamente desprovista de llamadas Retrofit o instancias de repositorios?
- [ ] **Sin Cambios No Autorizados**: ¿Se verificó que no se hayan editado ViewModels, Repositorios, APIs o Navigation de manera innecesaria?
- [ ] **Compilación Exitosa**: ¿El proyecto compila sin advertencias o errores en la capa de Compose?
