# BUENAS PRÁCTICAS EN JETPACK COMPOSE — FORMAS PERFECTAS ANDROID

## 1. Arquitectura de UI y Separación MVVM

La capa de presentación en Formas Perfectas se rige por el patrón **MVVM sin UseCases**.

### Principios de Separación:
1. **Composables Tontos (UI Pura)**: Las pantallas reciben el estado del ViewModel (`val uiState by vm.uiState.collectAsState()`) y emiten eventos a través de lambdas (`onLogout: () -> Unit`, `onNavigate: (String) -> Unit`).
2. **Sin Lógica HTTP ni Retrofit en Composables**: Ninguna función `@Composable` debe realizar llamadas directas a repositorios o servicios de red.
3. **Manejo de Estados de UI Explícitos**:
   Toda pantalla que cargue información remota debe representar explícitamente sus estados:

```kotlin
sealed class MiPantallaUiState {
    object Loading : MiPantallaUiState()
    data class Success(val datos: List<MiModelo>) : MiPantallaUiState()
    object Empty : MiPantallaUiState()
    data class Error(val mensaje: String) : MiPantallaUiState()
}
```

---

## 2. Descomposición de Composables y Rendimiento

1. **Funciones Pequeñas y Focalizadas**:
   - Dividir composables extensos en submódulos privados en el mismo archivo (ej: `@Composable private fun ModuloCard(...)`, `@Composable private fun HeaderSeccion(...)`).
   - Evitar archivos de UI monolíticos de más de 600 líneas sin descomponer.

2. **Recomposición Eficiente**:
   - Utilizar `remember` para calcular objetos pesados o estados locales simples (`var texto by remember { mutableStateOf("") }`).
   - Usar `key(...)` o el parámetro `key` en `LazyColumn` e `items(...)` para evitar recomposiciones innecesarias en listas dinámicas:
     ```kotlin
     items(modulos, key = { it.id }) { modulo ->
         ModuloCard(modulo = modulo)
     }
     ```

3. **Sin Asignaciones Pesadas dentro de Composables**:
   - No instanciar formateadores pesados (ej: `SimpleDateFormat`, `DecimalFormat`) dentro del cuerpo principal de un Composable sin `remember`.

---

## 3. Manejo Limpio de Scaffolds, TopBars y Snackbars

1. **Scaffold Estándar M3**:
   ```kotlin
   @OptIn(ExperimentalMaterial3Api::class)
   @Composable
   fun MiPantallaScreen(
       onBack: () -> Unit,
       vm: MiViewModel = hiltViewModel()
   ) {
       val snackbarHostState = remember { SnackbarHostState() }
       
       Scaffold(
           topBar = {
               TopAppBar(
                   title = { Text("Título de Pantalla") },
                   navigationIcon = {
                       IconButton(onClick = onBack) {
                           Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                       }
                   }
               )
           },
           snackbarHost = { SnackbarHost(snackbarHostState) }
       ) { padding ->
           Box(modifier = Modifier.padding(padding)) {
               // Contenido principal de la pantalla
           }
       }
   }
   ```

2. **Diálogos Modales**:
   - Utilizar `AlertDialog` o `Dialog` con `Surface(shape = MaterialTheme.shapes.large)` para mantener coherencia con Material 3.

---

## 4. Estructura de Previews para Desarrollo Visual

Al crear o rediseñar componentes, incluir Previews envueltas en `AppDynamicTheme` para validar el comportamiento en Light Mode y Dark Mode:

```kotlin
@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_NO)
@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun MiComponentePreview() {
    AppDynamicTheme {
        Surface {
            // Invocar composable de prueba
        }
    }
}
```
