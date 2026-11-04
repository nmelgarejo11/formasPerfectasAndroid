# AGENTS.md — Reglas Generales del Proyecto Formas Perfectas

## 1. Propósito y Prioridad
Estas son las reglas generales del proyecto y deben respetarse salvo instrucción explícita del usuario o una instrucción de mayor prioridad del entorno. Su objetivo es garantizar la coherencia técnica y la estabilidad del sistema Android.

## 2. Principios Fundamentales
*   **Analizar antes de modificar**: Es obligatorio inspeccionar el código, interfaces y dependencias relacionadas antes de proponer cambios.
*   **PREGUNTAR PRIMERO**: Ante cambios en arquitectura, contratos de API, seguridad o base de datos, explicar el impacto y esperar aprobación.
*   **Reutilizar > Modificar > Crear**: La prioridad es usar lo existente. No duplicar lógica de negocio, componentes de UI o utilidades.
*   **Cero Omisiones**: Las soluciones deben ser integrales (UI, ViewModel, Repository, API, DI y estados).
*   **Cambios Mínimos**: Realizar ediciones quirúrgicas. No refactorizar código no relacionado ni eliminar código sin autorización.
*   **No inventar**: Prohibido crear endpoints, modelos o parámetros que no existan en el proyecto o contrato del backend.

## 3. Arquitectura del Proyecto
El proyecto utiliza **MVVM** con separación de responsabilidades:
1.  **UI / Compose**: Screens que observan estados y lanzan eventos.
2.  **ViewModel**: Orquestación y gestión de `StateFlow`. Inyectado con Hilt.
3.  **Repository**: Centralización de datos. Inyecta el `ApiService`.
4.  **Retrofit / Data**: Servicios API y DTOs serializados con Moshi.
5.  **Backend**: API ASP.NET Core independiente (SQL Server).

## 4. Capa de Presentación (Jetpack Compose)
*   **Ubicación**: Screens en `ui/[feature]/[Name]Screen.kt`.
*   **Manejo de Estados**: Uso de `StateFlow` o `mutableStateOf`. Implementar estados de `Loading`, `Empty`, `Success` y `Error`.
*   **Descomposición**: Extraer componentes o dividir responsabilidades cuando la complejidad, reutilización o responsabilidad del código lo justifique, evitando refactorizaciones innecesarias.
*   **Temas**: Uso obligatorio de `AppDynamicTheme` y el singleton `TemaStore`.

## 5. ViewModels
*   Uso de `@HiltViewModel` y `viewModelScope` para corrutinas.
*   Manejo controlado de errores para evitar pantallas en blanco.
*   Los ViewModels no deben realizar llamadas HTTP directas; deben usar Repositorios.

## 6. Repositories
*   **Consistencia**: Antes de crear o modificar un Repository, analizar el patrón utilizado por la feature correspondiente (Interface + Impl vs Clase Directa) y mantener la consistencia existente.
*   Los Repositorios deben centralizar el acceso a los datos y ser inyectables mediante Hilt.

## 7. Retrofit y API
*   Reutilizar `ApiService` para endpoints generales y `AuthApiService` para autenticación.
*   Mantener serialización **Moshi** con `@JsonClass(generateAdapter = true)`.
*   Respetar los contratos de Request/Response existentes.

## 8. Inyección de Dependencias (Hilt)
*   Registrar dependencias en `AppModule`, `RepositoryModule` o `AdminModule` según corresponda.
*   Utilizar `@Inject` para constructores y `@Provides` / `@Binds` en módulos.

## 9. Autenticación y Seguridad
*   **Flujo**: Login -> TokenStorage -> AuthInterceptor -> TokenAuthenticator (Refresh).
*   **Headers**: La aplicación móvil utiliza obligatoriamente `X-Platform: 3` (Referencia: `AuthInterceptor.kt`).
*   **Privacidad**: Prohibido imprimir o loguear tokens, contraseñas o información sensible del JWT.
*   **Almacenamiento**: Uso exclusivo de `TokenStorage` (EncryptedSharedPreferences).

## 10. Navegación
*   Rutas centralizadas en `com.spa.appointments.core.utils.Constants.Routes`.
*   Grafo de navegación en `com.spa.appointments.navigation.AppNav.kt`.

## 11. Modelado de Datos
*   **Ubicación**: `domain/model`.
*   **Estructura**: Mantener la estructura actual donde conviven DTOs y modelos de dominio hasta que se decida una refactorización explícita.
*   **Nombres**: `[Accion][Entidad]Request` / `[Entidad]Response`.

## 12. Diseño Multiempresa
*   El proyecto es genérico. No hardcodear datos específicos de un cliente (ej: nombres de SPAs, IDs fijos). 
*   Utilizar el contexto de `idEmpresa` y `UsuarioId` del JWT.

## 13. Protocolo de Diagnóstico y Cambio
1.  Leer el error completo e identificar archivo y línea.
2.  Explicar la causa y la solución propuesta.
3.  Listar archivos afectados y riesgos potenciales.
4.  Esperar autorización para cambios que afecten a la arquitectura o seguridad.

## 14. Deuda Técnica Conocida
*   Todo el código reside en el módulo `:app` (falta de modularización).
*   Ausencia de capa de UseCases.
*   Mezcla de modelos en `domain/model`.
*   Archivos de UI extensos.
*   *Nota*: No modificar estos puntos salvo instrucción específica.
