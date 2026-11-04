# Skill: api-contract-maintenance
**Versión**: 1.0
**Contexto**: Integración Android ↔ Backend (ASP.NET Core + SQL Server)

## 1. Propósito
Garantizar la integridad, sincronización y protección de los contratos de datos que permiten la comunicación entre la aplicación Android y el Backend, minimizando errores de serialización y fallos de integración.

## 2. Activación de la Skill
Esta Skill debe activarse en los siguientes escenarios:
*   Al agregar o modificar un endpoint en `ApiService.kt` o `AuthApiService.kt`.
*   Al crear o actualizar modelos DTO (Request/Response) en `domain/model`.
*   Ante errores de serialización/deserialización de Moshi.
*   Cuando se detecte una inconsistencia entre los datos enviados por Android y los esperados por el Backend.
*   Antes de implementar cualquier funcionalidad que requiera consumo de datos remotos.

## 3. Fase de Inspección Obligatoria (Sin cambios)
Antes de proponer una modificación, el agente debe inspeccionar la ruta completa del dato:

### Lado Android:
1.  **Definición de API**: Localizar el endpoint en `ApiService` y verificar método (GET, POST, etc.), URL y anotaciones (`@Body`, `@Query`, `@Path`).
2.  **Patrón de Repository**: Identificar si la feature usa `Interface + Implementación` o `Clase Directa`. Se debe mantener el patrón existente.
3.  **Modelado DTO**: Revisar las clases en `domain/model`, verificando tipos de datos y la presencia de `@JsonClass(generateAdapter = true)`.
4.  **Anotaciones Moshi**: Comprobar si los nombres de variables coinciden con el JSON o requieren `@Json(name = "...")`.

### Lado Backend (Confirmación Técnica):
1.  **Controller**: Identificar el método en el Controller ASP.NET Core que recibe la petición.
2.  **Acceso a Datos**: Inspeccionar cómo está implementado actualmente el acceso a datos en el Backend, respetando la arquitectura existente de Controllers + Data y sin introducir una capa de Service adicional.
3.  **Stored Procedure**: Si existe un Stored Procedure involucrado, identificar los parámetros enviados y el esquema de resultados que devuelve SQL Server.

## 4. Reglas de Oro del Contrato
*   **X-Platform**: El header `X-Platform: 3` es obligatorio para la app móvil. Verificar su envío en `AuthInterceptor.kt`.
*   **Autenticación**: Las peticiones protegidas dependen de `TokenStorage`, `AuthInterceptor` y `TokenAuthenticator`. No alterar este flujo sin autorización.
*   **Nulabilidad**: La nulabilidad debe reflejar el contrato real. No convertir campos en nullable de forma preventiva. Si existe incertidumbre, primero verificar el JSON real, el DTO del Backend y el comportamiento de la consulta o fuente de datos correspondiente.
*   **Genericidad**: El contrato debe ser multiempresa. Prohibido hardcodear IDs de empresa o nombres específicos en los DTOs.
*   **Inalterabilidad**: No modificar el contrato de la API para "solucionar" un problema de Android si existe una alternativa compatible con el contrato actual.

## 5. Procedimiento Operativo
1.  **Detectar Inconsistencia**: Comparar el JSON real del Backend con la definición del DTO en Android.
2.  **Identificar Causa**: Determinar si el error es de nombre de campo, tipo de dato (ej: String vs Int) o nulabilidad.
3.  **Proponer Solución**: Explicar el cambio quirúrgico necesario (ej: "Agregar @Json(name = '...') al campo X").
4.  **PREGUNTAR PRIMERO**: Solicitar aprobación explícita antes de ejecutar el cambio, incluso si es solo cambiar un tipo de dato o agregar un parámetro opcional.

## 6. Cambios Prohibidos sin Autorización Expresa
Queda terminantemente prohibido realizar de forma automática:
*   Modificación de URLs base o contratos de API existentes.
*   Cambios en la lógica de Controllers o acceso a datos en el Backend.
*   Modificación de Stored Procedures o esquemas de Base de Datos.
*   Cambios en los headers de seguridad o autenticación JWT.
*   Actualización de versiones de Retrofit, OkHttp o Moshi.

## 7. Validación Post-Modificación
Tras una edición autorizada, se debe confirmar:
1.  **Compilación**: El proyecto debe compilar sin errores de tipos.
2.  **Deserialización**: Verificar mediante herramientas de diagnóstico que el JSON recibido se mapea correctamente al objeto DTO.
3.  **Respuesta**: Confirmar que el flujo visual (UI) reacciona adecuadamente al nuevo contrato (Loading -> Success/Error).
