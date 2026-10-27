# GEMINI.md — FormasPerfectas Android

## 1. Propósito

Este archivo contiene las reglas que deben seguirse al trabajar en el proyecto Android de FormasPerfectas.

Gemini debe utilizar estas instrucciones como reglas de trabajo permanentes durante el análisis, desarrollo, modificación, depuración y refactorización del proyecto.

El objetivo principal es mantener la arquitectura existente, evitar cambios innecesarios y producir código coherente con el proyecto actual.

---

# 2. Regla principal: analizar antes de modificar

ANTES de proponer o realizar cambios:

1. Revisar el código existente.
2. Identificar las clases, interfaces, ViewModels, repositorios, APIs y modelos relacionados.
3. Entender cómo funciona actualmente el flujo.
4. No asumir que una clase, método, endpoint o tabla existe.
5. No inventar nombres de archivos, clases, métodos, endpoints, parámetros o propiedades.
6. Reutilizar primero lo que ya existe.
7. Evitar crear código duplicado.

Si falta información necesaria para realizar un cambio correctamente, DETENERSE y preguntar antes de modificar.

---

# 3. Regla "PREGUNTAR PRIMERO"

Esta es una regla fundamental del proyecto.

Cuando una solicitud implique modificar arquitectura, crear nuevas clases, cambiar APIs, modificar contratos, cambiar base de datos o realizar cambios importantes:

NO realizar inmediatamente todos los cambios.

Primero:

* explicar qué se encontró;
* explicar qué se propone cambiar;
* indicar qué archivos serían afectados;
* identificar cualquier dependencia o impacto;
* preguntar si se puede continuar cuando exista una decisión relevante que el usuario deba aprobar.

No tomar decisiones arquitectónicas importantes por cuenta propia.

---

# 4. Cero omisiones

Cuando se solicite implementar una funcionalidad:

* No entregar solamente una parte de la solución.
* Identificar todos los archivos que necesitan modificación o creación.
* Considerar modelos, DTO, API, Repository, ViewModel, UI, navegación, DI y estados cuando correspondan.
* Verificar imports necesarios.
* Verificar dependencias necesarias.
* Verificar nombres de paquetes.
* Verificar referencias entre clases.

Si una implementación requiere varios archivos, indicar TODOS los archivos involucrados.

No asumir que el usuario conoce qué archivo debe modificar.

---

# 5. Formato obligatorio para código

Cada bloque de código debe indicar claramente:

ARCHIVO:
ruta/completa/del/archivo

Ejemplo:

`app/src/main/java/com/spa/appointments/data/api/AuthApi.kt`

Después colocar el código.

Cuando sea posible, entregar el archivo completo y listo para copiar.

No entregar fragmentos incompletos cuando el usuario necesite reemplazar un archivo completo.

Si solamente se necesita modificar una sección pequeña, indicar exactamente dónde realizar el cambio.

---

# 6. Arquitectura Android

La aplicación utiliza:

* Kotlin
* Jetpack Compose
* MVVM
* Hilt
* Retrofit
* Moshi
* EncryptedSharedPreferences
* JWT
* Android Navigation

Mantener esta arquitectura.

No introducir frameworks o arquitecturas adicionales sin justificar previamente su necesidad.

No cambiar de MVVM a otra arquitectura.

No introducir Clean Architecture, MVI, Redux u otra arquitectura simplemente por preferencia personal.

---

# 7. Organización general

Mantener una separación clara entre:

UI
↓
ViewModel
↓
Repository
↓
API / Data
↓
Backend

La UI no debe realizar directamente llamadas HTTP.

Los ViewModel no deben contener lógica de acceso directo a Retrofit.

Los Repository deben centralizar el acceso a los datos cuando corresponda.

---

# 8. Hilt / Inyección de dependencias

Utilizar Hilt para la inyección de dependencias.

Antes de crear una nueva dependencia:

1. Revisar si ya existe un módulo Hilt adecuado.
2. Revisar si ya existe un Repository.
3. Revisar si ya existe una interfaz.
4. Reutilizar la infraestructura existente cuando sea posible.

No crear módulos Hilt duplicados.

Cuando se cree una nueva clase que necesite inyección:

* revisar su constructor;
* revisar el módulo correspondiente;
* revisar el alcance;
* revisar que Hilt pueda resolver todas sus dependencias.

Si aparece un error de Hilt, analizar primero el grafo de dependencias antes de crear nuevas clases.

---

# 9. Retrofit y API

La aplicación consume una API ASP.NET existente.

Antes de crear un endpoint:

1. Revisar si ya existe.
2. Revisar su URL.
3. Revisar método HTTP.
4. Revisar parámetros.
5. Revisar request.
6. Revisar response.
7. Revisar autenticación.
8. Revisar headers requeridos.

No inventar endpoints.

No cambiar contratos de API sin autorización.

No modificar la API backend solamente para solucionar un problema del Android si existe una solución compatible con el contrato actual.

---

# 10. Autenticación

La aplicación utiliza autenticación mediante JWT.

Consideraciones:

* Access Token: duración aproximada de 1 hora.
* Refresh Token: duración aproximada de 7 días.
* Utilizar almacenamiento seguro para las credenciales/token.
* Actualmente se utiliza EncryptedSharedPreferences.

No almacenar tokens en texto plano en SharedPreferences normales.

No imprimir tokens en logs.

No incluir tokens, contraseñas ni credenciales en código fuente.

Cuando se implemente renovación de token:

1. Detectar expiración o respuesta correspondiente.
2. Utilizar Refresh Token.
3. Obtener nuevo Access Token.
4. Actualizar almacenamiento seguro.
5. Reintentar la solicitud original cuando corresponda.
6. Si el refresh falla, cerrar sesión de manera controlada.

---

# 11. Seguridad

Nunca:

* escribir contraseñas en logs;
* escribir JWT en logs;
* incluir API keys reales en código fuente;
* incluir secretos directamente en Git;
* exponer información sensible en mensajes de error.

Si se requiere una clave o secreto:

* explicar dónde debe configurarse;
* utilizar variables/local properties/secrets según corresponda;
* nunca inventar una clave.

---

# 12. Estados de la aplicación

No utilizar Strings arbitrarios para representar estados cuando el backend ya proporciona identificadores o catálogos.

Preferir IDs y modelos tipados.

Para estados de citas actualmente definidos:

1. Programada
2. Confirmada
3. Cancelada
4. Pendiente cambio
5. Reagendada
6. Rechazada

No cambiar estos identificadores ni nombres sin verificar primero el contrato real de la API.

---

# 13. Proyecto multiempresa

El sistema debe mantenerse genérico y multiempresa.

El proyecto NO debe diseñarse exclusivamente para un SPA.

Aunque el primer cliente sea FormasPerfectas, la solución debe poder utilizarse para diferentes tipos de negocios, por ejemplo:

* spas;
* barberías;
* clínicas;
* consultorios;
* centros de servicios;
* otros negocios basados en citas.

No introducir nombres específicos de un negocio cuando una entidad genérica sea suficiente.

---

# 14. Backend

El backend está desarrollado en ASP.NET Core.

La ruta local del repositorio del backend API es:
`D:\Personal\Git-Proyectos\gestionServiciosAPI`
Gemini tiene autorización para leer y explorar esta ruta para consultar controladores, modelos y datos cuando necesite contexto sobre el contrato o la base de datos.

La arquitectura backend utiliza:

Controllers
+
Data

No utilizar una capa Service adicional.

NO crear automáticamente:

* Services;
* ServiceInterfaces;
* managers;
* handlers;

si no son necesarios para la arquitectura existente.

Antes de modificar backend, revisar primero la implementación existente.

---

# 15. Base de datos

La base de datos utiliza SQL Server.

El nombre de la base de datos debe mantenerse genérico.

No utilizar "SPA" como nombre de la base de datos.

Se utilizan esquemas funcionales, entre ellos:

* GEN
* SEG
* CAT
* PER
* AGE
* CLI
* FIN
* NFT

Cuando se trabaje con base de datos:

* respetar los esquemas existentes;
* respetar nombres existentes;
* no crear tablas duplicadas;
* no cambiar tipos de datos sin justificar;
* no eliminar columnas sin autorización.

---

# 16. Stored Procedures

La lógica de acceso/modificación de datos que actualmente utiliza Stored Procedures debe mantenerse.

Cuando corresponda realizar INSERT/UPDATE/DELETE mediante Stored Procedures, respetar el patrón existente.

Antes de crear un nuevo Stored Procedure:

1. Buscar si ya existe uno equivalente.
2. Revisar los Stored Procedures relacionados.
3. Mantener nombres y esquemas consistentes.
4. No duplicar lógica existente.

No reemplazar Stored Procedures por consultas directas simplemente por preferencia.

---

# 17. Empresa y usuario

La API utiliza contexto de empresa y usuario.

Existe un concepto equivalente a:

* EmpresaId
* UsuarioId

Respetar el contexto autenticado.

No asumir una empresa fija.

No colocar IDs de empresa o usuario directamente en código.

---

# 18. Headers y plataforma

La aplicación móvil utiliza el identificador de plataforma:

`X-Platform: 3`

La constante actual es `PLATFORM_MOBILE = "3"` y la referencia real se encuentra en `AuthInterceptor.kt`.

No cambiarlo a `MOBILE` ni inventar otro valor sin verificar el contrato actual de la API.

---

# 19. UI / Jetpack Compose

Utilizar Jetpack Compose para la interfaz.

Mantener:

* componentes reutilizables;
* estados controlados desde ViewModel cuando corresponda;
* previews cuando sean útiles;
* separación entre UI y lógica de negocio.

Evitar colocar lógica compleja dentro de Composables.

No duplicar componentes visuales si ya existe un componente reutilizable.

---

# 20. Estados de UI

Las pantallas deben manejar claramente estados como:

* Loading
* Success
* Empty
* Error

Cuando una pantalla consuma información remota, considerar:

* indicador de carga;
* mensaje de error;
* posibilidad de reintentar;
* estado vacío;
* datos exitosos.

No dejar pantallas en blanco ante errores.

---

# 21. Navegación

Antes de crear una nueva ruta:

1. Revisar el NavHost existente.
2. Revisar las rutas existentes.
3. Revisar cómo se pasan argumentos.
4. Mantener el patrón actual.

No duplicar rutas.

No cambiar nombres de rutas existentes sin revisar sus referencias.

---

# 22. Manejo de errores

Los errores provenientes de API, red o validación deben manejarse de forma controlada.

No utilizar:

`try/catch` gigantes que oculten todos los errores.

No ignorar excepciones.

No mostrar mensajes técnicos innecesarios al usuario final.

Cuando corresponda:

* registrar información técnica no sensible;
* transformar el error a un estado entendible por la UI;
* permitir al usuario reintentar.

---

# 23. Dependencias Gradle

No actualizar versiones de:

* Gradle;
* Android Gradle Plugin;
* Kotlin;
* Compose;
* Hilt;
* Retrofit;
* Moshi;

sin consultar primero.

Una actualización de dependencias puede provocar incompatibilidades.

Antes de recomendar una actualización:

1. identificar la versión actual;
2. explicar por qué se necesita;
3. identificar posibles impactos;
4. solicitar autorización cuando sea un cambio relevante.

---

# 24. Java / Gradle

La configuración actual utiliza:

* Gradle 8.7
* JDK 21 para ejecutar Gradle
* JDK 17 como toolchain de compilación del proyecto

No cambiar esta configuración para solucionar problemas menores.

Si aparece un error relacionado con Java/Gradle:

1. identificar si corresponde al JDK que ejecuta Gradle;
2. identificar si corresponde al Java Toolchain;
3. revisar la configuración actual;
4. no actualizar Gradle automáticamente.

---

# 25. Gemini debe revisar antes de crear

Antes de crear cualquier:

* Activity
* Fragment
* Composable
* ViewModel
* Repository
* API interface
* DTO
* Model
* Hilt Module
* utilidad
* clase de navegación

buscar primero si ya existe algo equivalente.

La prioridad es:

REUTILIZAR > MODIFICAR > CREAR

No:

CREAR > DUPLICAR > DEJAR CÓDIGO SIN USAR

---

# 26. No eliminar código sin autorización

No eliminar:

* clases;
* métodos;
* endpoints;
* recursos;
* layouts;
* composables;
* dependencias;

solo porque aparentemente no se utilizan.

Antes de eliminar algo, comprobar sus referencias y explicar el impacto.

Para eliminar código importante, preguntar primero.

---

# 27. Cambios mínimos

Cuando una solución pueda realizarse modificando un archivo, no modificar cinco archivos innecesariamente.

Preferir cambios pequeños, controlados y fáciles de revertir.

No realizar refactorizaciones generales mientras se implementa una funcionalidad específica, salvo que sean necesarias.

---

# 28. Diagnóstico de errores

Cuando el usuario informe un error:

1. Leer el error completo.
2. Identificar archivo y línea.
3. Identificar la causa probable.
4. Revisar las clases relacionadas.
5. Explicar la causa en lenguaje sencillo.
6. Proponer la solución.
7. Indicar exactamente qué archivos cambiar.
8. No modificar código no relacionado.

No solucionar un error eliminando funcionalidad simplemente para que compile.

---

# 29. Comunicación con el usuario

El usuario no es experto en Kotlin/Android.

Por lo tanto:

* explicar en lenguaje sencillo;
* evitar asumir conocimientos avanzados;
* indicar exactamente dónde hacer clic cuando sea necesario;
* indicar rutas completas de archivos;
* explicar qué hace cada cambio;
* evitar respuestas ambiguas.

Cuando existan varias opciones, recomendar una y explicar brevemente por qué.

---

# 30. Formato de implementación

Cuando se solicite implementar una funcionalidad, utilizar este orden:

## 1. Qué vamos a hacer

Explicar brevemente la funcionalidad.

## 2. Qué encontramos

Indicar qué partes existentes se reutilizarán.

## 3. Archivos involucrados

Enumerar:

* archivos existentes que se modificarán;
* archivos nuevos que se crearán.

## 4. Cambios

Entregar el código indicando siempre la ruta.

## 5. Configuración adicional

Indicar si se requieren:

* dependencias;
* permisos;
* Gradle;
* navegación;
* variables;
* configuración de API.

## 6. Prueba

Indicar cómo comprobar que funciona.

## 7. Posibles errores

Indicar errores comunes y cómo identificarlos.

---

# 31. Regla para modificaciones importantes

Si la solicitud puede afectar:

* arquitectura;
* autenticación;
* base de datos;
* API;
* navegación global;
* estructura de paquetes;
* dependencias;
* seguridad;

primero explicar el impacto y pedir aprobación antes de realizar el cambio.

---

# 32. Regla para código generado por IA

Todo código generado debe:

* ser coherente con el código existente;
* compilar conceptualmente;
* utilizar nombres reales encontrados en el proyecto;
* respetar paquetes existentes;
* respetar tipos existentes;
* evitar referencias inexistentes.

Nunca presentar código inventado como si ya existiera en el proyecto.

Si no se tiene suficiente contexto para generar código correcto, pedir al usuario que proporcione el archivo o indicar qué archivo se necesita revisar.

---

# 33. Acceso a Base de Datos Local (SQL Server)

Gemini tiene autorización para conectarse a la base de datos local para leer la estructura de tablas y el código fuente de los Stored Procedures.

Para inspeccionar la base de datos desde la terminal, Gemini DEBE utilizar la herramienta `sqlcmd` con los siguientes parámetros exactos:

`sqlcmd -S NMELGAREJO -E -d GESTION_SERVICIOS -y 0 -Q "TU_CONSULTA_AQUI"`

Parámetros explicados:
* `-S NMELGAREJO`: Nombre del servidor local.
* `-E`: Autenticación de Windows (Trusted Connection), ya que el usuario SQL falla.
* `-d GESTION_SERVICIOS`: Nombre exacto de la base de datos.
* `-y 0`: Para evitar que el output de texto largo (como la definición de un SP) se trunque.

**Restricciones de Base de Datos para Gemini:**
1. Solo se permite ejecutar sentencias de lectura (`SELECT`, `sp_helptext`, `OBJECT_DEFINITION`).
2. PROHIBIDO ejecutar sentencias `INSERT`, `UPDATE`, `DELETE`, `ALTER`, `DROP` o `CREATE` directamente desde la terminal.
3. Para extraer el código de un Stored Procedure, usar: `SELECT OBJECT_DEFINITION(OBJECT_ID('[ESQUEMA].[NombreSP]'))`

---

# 34. Regla final

La prioridad de Gemini en este proyecto es:

1. Mantener funcionando lo existente.
2. Entender antes de modificar.
3. Preguntar antes de tomar decisiones importantes.
4. Reutilizar código existente.
5. Evitar duplicación.
6. Realizar cambios mínimos.
7. Entregar soluciones completas.
8. Indicar siempre archivos y rutas.
9. Respetar la arquitectura existente.
10. No inventar información.

Si existe conflicto entre una nueva solicitud y estas reglas, explicar el conflicto antes de realizar cambios.

FIN DE LAS INSTRUCCIONES.
