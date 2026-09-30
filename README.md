# PharmaMobil · Guía práctica 08

**Estudiante:** Rony Alonso Ancajima Tripul  
**Docente:** Benjamín David Reyna Barreto  
**Universidad:** Universidad Peruana Unión  
**Carrera:** Ingeniería de Sistemas  
**Curso:** Desarrollo de Aplicaciones Móviles  
**Modalidad:** Trabajo individual

## Objetivo

Implementar y comprobar la gestión de productos mediante una API REST
de PharmaSoft, utilizando Kotlin Multiplatform, Compose, Ktor y Koin.

La ejecución y las evidencias de este trabajo corresponden a Android.

## Abrir y ejecutar

1. Abrir la carpeta PharmaMobil en Android Studio.
2. Utilizar JDK 17 y tener instalado Android SDK 35.
3. Esperar la sincronización de Gradle.
4. Iniciar Oracle y el backend PharmaBackend.
5. Seleccionar la configuración shared y el emulador Android.
6. Ejecutar la aplicación con el botón Run.

URL base utilizada en el emulador:

```text
http://10.0.2.2:8080/api/v1/
```

Comprobación del backend desde el navegador de la PC:

```text
http://localhost:8080/api/health
http://localhost:8080/swagger-ui.html
```

Para un teléfono físico se debe utilizar la dirección IP de la PC
en la red local. La dirección 10.0.2.2 corresponde al emulador Android.

Antes de crear productos debe existir una categoría en el backend.
En la comprobación realizada se utilizó la categoría Medicamentos,
con ID 1.

## Implementación

- DTO de solicitud y respuesta de productos.
- Respuesta paginada mediante PaginaResponseDto<T>.
- Operaciones GET, POST, PUT y DELETE.
- Tratamiento de DELETE 204 sin deserializar un cuerpo de respuesta.
- ProductoRepository con cinco operaciones e implementación REST.
- Inyección de dependencias mediante Koin.
- Casos de uso para listar, obtener, registrar, actualizar y eliminar.
- Traducción centralizada de errores mediante ErrorApi.
- Manejo de validación 400 por campo, 404, 409, errores de servidor,
  conexión, timeout y contrato JSON inválido.
- Propagación de cancelaciones de corrutinas.
- ProductosViewModel con StateFlow y viewModelScope.
- Separación del estado del listado y del estado de la operación.
- Formulario con nombre, precio, stock, categoría y estado.
- Consulta por ID antes de editar.
- Confirmación antes de eliminar.
- Actualización del listado después de las modificaciones.
- Paginación mediante botones Anterior y Siguiente.
- Registro de solicitudes y respuestas de Ktor en Logcat.

Durante una operación se mantiene visible el listado y se bloquean
los controles para evitar solicitudes simultáneas. Esta versión
todavía requiere ajustar el bloqueo para cumplir literalmente el
criterio de la guía de deshabilitar únicamente el botón correspondiente.

## Contrato REST

Base: /api/v1/

| Método | Endpoint | Respuesta de éxito |
|---|---|---|
| GET | productos?pagina=0&tamanio=20 | 200 |
| GET | productos/{id} | 200 |
| POST | productos | 201 |
| PUT | productos/{id} | 200 |
| DELETE | productos/{id} | 204 |

Campos enviados:

- nombre
- precio
- stock
- estado
- categoriaId

Campos de la respuesta paginada:

- contenido
- pagina
- tamanio
- totalElementos
- totalPaginas
- ultima

## Comprobaciones realizadas en Android

Se compiló el proyecto y se ejecutó en un emulador Pixel 6.

Se comprobó la conexión con el backend, su endpoint de salud y el
listado paginado de productos.

Se realizaron las siguientes operaciones:

| Operación | Resultado observado |
|---|---|
| Listar | Respuesta GET 200 y producto visible |
| Crear | Paracetamol 500 mg, precio 5 y stock 20; POST 201 |
| Obtener y actualizar | Precio cambiado a 6 y stock a 25; PUT 200 |
| Eliminar | DELETE 204 y producto actualizado a inactivo |
| Repetir eliminación | Respuesta 409 por producto ya inactivo |
| Validar nombre | Nombre ab rechazado con 400 y mensaje bajo el campo |

La eliminación del backend es lógica: el producto permanece en el
listado con estado inactivo. Por ello, un DELETE exitoso no implica
que desaparezca del listado.

Los registros de Logcat muestran respuestas 200, 201, 204, 400 y 409.

## Pruebas automatizadas

Comando de compilación:

```powershell
.\gradlew.bat :shared:assembleDebug
```

Resultado observado:

```text
BUILD SUCCESSFUL
```

Comando de pruebas:

```powershell
.\gradlew.bat :shared:testDebugUnitTest
```

Resultado del informe de Gradle:

| Grupo | Pruebas | Fallos |
|---|---:|---:|
| Validación de productos | 7 | 0 |
| API REST con MockEngine | 7 | 0 |
| Transiciones de ProductosViewModel | 4 | 0 |
| Total | 14 | 0 |

El informe registró 0 pruebas ignoradas y 100 % de éxito.

Las pruebas REST utilizan respuestas simuladas mediante MockEngine.
Las comprobaciones manuales de Android se realizaron contra el
backend real; ambas evidencias se presentan por separado.

Para abrir el informe en Windows:

```powershell
start .\shared\build\reports\tests\testDebugUnitTest\index.html
```

## Correcciones realizadas

- Sustitución de GlobalContext por KoinPlatform en ProductosScreen.kt.
- Corrección de las funciones de prueba para que devuelvan Unit.
- Adaptación de la configuración del backend a Oracle XE y XEPDB1.
- Sustitución del tipo SQL BOOLEAN por NUMBER(1,0) con restricciones
  para la versión de Oracle utilizada.

## Control de versiones

Rama de trabajo:

```text
feature/crud-productos-ancajima
```

Historial confirmado antes de actualizar este README:

| Commit | Descripción |
|---|---|
| 96cff49 | Importar proyecto PharmaMobil de sesión 8 |
| e931ea9 | Corregir acceso a Koin en pantalla de productos |
| 19eee56 | Corregir retorno Unit en pruebas de validación |

main y develop apuntaban al commit de importación.
Los dos commits de correcciones se realizaron en la rama feature.

El commit inicial no cuenta como uno de los tres commits de la rama
feature. Tampoco se presenta este historial como evidencia de que
la implementación original del CRUD se desarrolló por etapas.

## Evidencias disponibles

Se dispone de capturas reales de:

- Backend y endpoint de salud.
- Respuesta JSON paginada.
- Creación y actualización de productos.
- Producto inactivo tras su eliminación lógica.
- Error 400 de validación del nombre.
- Registros HTTP de Ktor en Logcat.
- Compilación correcta de Android.
- Informe de las 18 pruebas aprobadas.

Estas evidencias se incorporarán al informe PDF del trabajo.

## Alcance y limitaciones

- iOS queda fuera del alcance de esta entrega y no se acredita su ejecución.
- El backend y Oracle se ejecutan por separado y no están incluidos
  en el proyecto Android.
- La URL modificada en pantalla no se conserva al reiniciar la app.
- Se permite HTTP para el backend local de laboratorio.
- No se incorporó autenticación JWT.
- Las pruebas aprobadas no demuestran por sí solas el funcionamiento
  en iOS ni todos los escenarios posibles de red.
- Queda pendiente ajustar el bloqueo de botones indicado en la guía.

## Manejo de errores

- HTTP 400: `ErrorApi.Validacion`. El mensaje aparece bajo el campo afectado. Se comprobó con nombre `ab` y precio `0`.
- HTTP 404: `ErrorApi.NoEncontrado`. El PUT al ID 999999 devolvió 404 en Swagger; este caso no se ejecutó desde la pantalla Android.
- HTTP 409: `ErrorApi.Conflicto`. Se observó al repetir DELETE y al crear un producto con nombre existente.
- Backend detenido: `ErrorApi.SinConexion`, sin respuesta HTTP.
- Tiempo agotado: `ErrorApi.TiempoAgotado`. Se probó con 1 ms y luego se restauró `15_000` ms.
- Cancelación: `CancellationException` se propaga sin convertirse en `ErrorApi`. Lo comprueba una prueba de `commonTest`; falta captura manual durante una operación en curso.