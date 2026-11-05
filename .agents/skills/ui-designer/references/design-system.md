# SISTEMA DE DISEÑO MATERIAL 3 — FORMAS PERFECTAS ANDROID

Este documento establece las especificaciones de diseño visual, componentes M3, jerarquía tipográfica, espaciados y accesibilidad para el proyecto Formas Perfectas Android.

---

## 1. Tokens de Color Semánticos (Material 3)

Todos los componentes deben mapear sus colores a los tokens de `MaterialTheme.colorScheme`:

| Token M3 | Uso Recomendado en Formas Perfectas |
| :--- | :--- |
| `primary` | Botones principales, barras superiores destacadas, elementos seleccionados, FAB. |
| `onPrimary` | Texto o iconos sobre elementos teñidos con `primary` (generalmente blanco `Color.White`). |
| `primaryContainer` | Fondo de avatares, fondo de iconos en tarjetas de módulos, badges activos suaves. |
| `onPrimaryContainer` | Icono o texto sobre `primaryContainer`. |
| `secondary` | Botones secundarios, filtros no seleccionados, indicadores secundarios. |
| `secondaryContainer` | Tarjetas de submódulos, chips de filtro secundarios. |
| `surface` | Fondo de tarjetas (`Card`), diálogos (`Dialog`), hojas inferiores (`BottomSheet`). |
| `onSurface` | Títulos principales y texto de cuerpo primario (oscuro en Light, claro en Dark). |
| `surfaceVariant` | Fondo de secciones secundarias, contenedores de entrada de texto suaves. |
| `onSurfaceVariant` | Subtítulos, labels secundarios, iconos descriptivos. |
| `outline` | Bordes de inputs activos, divisores destacados. |
| `outlineVariant` | Bordes sutiles de tarjetas (`0.5.dp` a `1.dp`), líneas divisorias neutras. |
| `errorContainer` | Banner de alerta de demo/licencia expirada, contenedores de error. |
| `onErrorContainer` | Texto e iconos dentro de alertas de error. |

---

## 2. Escala de Espaciado (Grid de 4dp)

Mantener la consistencia en paddings, margins y arreglos espaciales:

- **4.dp**: Espaciado micro (entre icono y texto interno en un chip/botón).
- **8.dp**: Espaciado compacto (entre elementos dentro de una fila o lista compacta).
- **12.dp**: Espaciado estándar (entre tarjetas en una lista vertical o campos de un formulario).
- **16.dp**: Padding de contenedores principales (padding horizontal de pantalla o tarjeta).
- **20.dp - 24.dp**: Espaciado de sección (separación entre bloques funcionales).
- **32.dp**: Margen superior/inferior para pantallas de bienvenida, Login o Splash.

---

## 3. Tipografía y Jerarquía Visual

Utilizar siempre `MaterialTheme.typography`:

- **headlineLarge**: Títulos de impacto principal (Splash, bienvenida Login, nombres principales). `FontWeight.Bold`, 28-32.sp.
- **titleMedium**: Títulos de pantalla (TopAppBar), encabezados de diálogos o secciones. `FontWeight.Bold` / `Medium`, 18-20.sp.
- **titleSmall**: Títulos de tarjetas de módulo o categorías. `FontWeight.Bold`, 14-16.sp.
- **bodyLarge**: Texto de entrada en campos de texto, descripciones principales. `FontWeight.Normal`, 16.sp.
- **bodyMedium**: Texto secundario de listas, subtítulos descriptivos. `FontWeight.Normal`, 14.sp.
- **bodySmall**: Mensajes de error, notas al pie, banners de alerta. `FontWeight.Normal`, 12.sp.
- **labelSmall**: Chips, tags, headers de secciones en mayúsculas. `FontWeight.Medium`, 10-11.sp.

---

## 4. Estándares de Componentes Visuales

### A. Botones
- **Botón Principal**: `Button` con `shape = RoundedCornerShape(12.dp)` o `16.dp` y altura de `52.dp`. Muestra `CircularProgressIndicator` cuando el estado es `loading`.
- **Botón Secundario**: `OutlinedButton` con borde `BorderStroke(1.dp, MaterialTheme.colorScheme.outline)`.
- **Botón Plano / Cancelar**: `TextButton` para acciones neutras o secundarias en diálogos.

### B. Tarjetas (`Cards`)
- Usar `CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)`.
- Definir bordes limpios con `BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)`.
- `shape = RoundedCornerShape(14.dp)` o `16.dp`.
- Usar `elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)` para diseño plano moderno, o `2.dp` para tarjetas flotantes.

### C. Campos de Entrada (`TextFields`)
- Usar `OutlinedTextField` con `shape = RoundedCornerShape(12.dp)`.
- Incluir `singleLine = true`, `keyboardOptions` e `imeAction` apropiados (`Next`, `Done`).
- Gestionar foco con `LocalFocusManager.current`.

### D. Banners y Alertas de Estado
- Para notificaciones o avisos de expiración, envolver en un `Surface` con `color = MaterialTheme.colorScheme.errorContainer` o `primaryContainer`.

---

## 5. Accesibilidad y Ergonomía

- **Áreas Táctiles Mínimas**: Todo elemento interactivo (`IconButton`, `Button`, filas clickeables) debe tener una dimensión táctil mínima de **48.dp x 48.dp**.
- **Contraste de Texto**: Asegurar que sobre `primary` se use `onPrimary`, y sobre `surface` se use `onSurface`.
- **Soporte Dark Mode**: Verificar que todas las pantallas funcionen correctamente en modo claro y oscuro alternando el parámetro `darkTheme` en los Previews.
