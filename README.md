# PharmaMobil · Guía práctica 08

Ancajima Tripul Rony Alonso · UPeU · Desarrollo de Aplicaciones Móviles.

## Abrir y ejecutar en Android Studio

1. Descomprime el ZIP. Abre la carpeta **PharmaMobil**, donde está `settings.gradle.kts`.
2. Usa JDK 17 y SDK 35. Espera la sincronización de Gradle.
3. Selecciona **shared** (no All Tests), el emulador y el triángulo verde Run.
4. La app conecta con **PharmaSoft**, no con el catálogo público de sesión 7. Debes arrancar el backend y su base Oracle antes de ejecutar operaciones.
5. En la pantalla puedes editar **URL base** y pulsar **Conectar / Recargar**. La dirección propuesta para el emulador Android es `http://10.0.2.2:8080/api/v1/`. Ajusta el puerto según tu backend. En un teléfono físico usa la IP de tu PC en la red local; `10.0.2.2` solo corresponde al emulador Android.
6. Antes de probar, verifica en el navegador de la PC `http://localhost:8080/api/health` y `/swagger-ui.html` (ajusta el puerto).

Si PharmaSoft no está encendido aparecerá un mensaje de conexión: no se cargan productos ficticios. El ID de categoría debe pertenecer a una categoría existente en Oracle; consúltalo en Swagger o en el catálogo del backend.

## Implementación

- DTO de producto para envío y respuesta, más `PaginaResponseDto<T>` con los seis campos de la guía.
- GET listado paginado y GET por ID, POST, PUT y DELETE sin deserializar el 204.
- `ProductoRepository` con cinco operaciones, implementación REST registrada en Koin y alternativa en memoria para pruebas (no se inyecta en la app).
- Casos de uso para listar, obtener, registrar, actualizar y eliminar.
- `ErrorApi` y un punto de traducción: validación 400 por campo, 404, conflicto 409, servidor, red, timeout y contrato JSON inválido. Las cancelaciones se relanzan.
- `ProductosViewModel` hereda de AndroidX ViewModel y utiliza viewModelScope. La instancia se crea para la pantalla y cancela su scope al salir; no se retiene a través de la recreación de Activity.
- `ProductoUiState` separa fase de listado y operación. Al modificar datos mantiene visible la lista y deshabilita acciones de mutación para impedir solicitudes simultáneas. Tras el éxito consulta nuevamente el servidor sin mostrar una carga inicial a pantalla completa.
- Formulario con nombre, precio, stock, estado e ID categoría; errores del servidor debajo de sus campos. Editar consulta GET por ID; eliminar pide confirmación. Paginación anterior/siguiente.
- Logger explícito `KTOR:` en Logcat. Filtro: `package:mine message:KTOR`.
- HTTP sin cifrar permitido para el backend local de laboratorio en Android. Para despliegue usa HTTPS y elimina el permiso de tráfico HTTP.

La validación local evita texto no numérico y valores de precio no finitos. Las restricciones de negocio, por ejemplo nombre de dos caracteres, se envían al servidor para comprobar el error 400 solicitado por la guía.

## Contrato esperado

Base: `/api/v1/`.

| Método | Endpoint | Éxito según guía |
| --- | --- | --- |
| GET | productos?pagina=0&tamanio=20 | 200 |
| GET | productos/{id} | 200 |
| POST | productos | 201 |
| PUT | productos/{id} | 200 |
| DELETE | productos/{id} | 204 |

Enviar `nombre`, `precio`, `stock`, `estado` y `categoriaId`. El listado contiene `contenido`, `pagina`, `tamanio`, `totalElementos`, `totalPaginas` y `ultima`. El error esperado contiene `message` y `validationErrors`.

Se implementó el contrato del PDF adjunto. No se pudo inspeccionar el backend de dreyna/pharmaSoft con la conexión disponible: confirma en Swagger que estos nombres coincidan con tu versión real. No se añade JWT en esta sesión; si el servidor está protegido necesitarás su configuración de acceso.

## Comprobaciones y límites

El entorno de preparación bloqueó la descarga de Gradle, por lo que **no se afirma compilación, pruebas aprobadas ni CRUD ejecutado contra el servidor**.

Se incluyen siete pruebas MockEngine para verbos, listado paginado, DELETE 204, validación 400 y cancelación, además de las siete validaciones del formulario anterior. Para ejecutar en Windows:

```bat
gradlew.bat :shared:assembleDebug :shared:testDebugUnitTest
```

En Linux/macOS: `./gradlew :shared:assembleDebug :shared:testDebugUnitTest`.

El proyecto conserva targets y motor Darwin, y expone `MainViewController()` para iOS. **No contiene app anfitriona Xcode ni evidencia de ejecución iOS**. Integrar en macOS usando un host SwiftUI, configurando la URL apropiada y las excepciones ATS para el servidor local si usa HTTP. Para HTTPS no habilites excepciones ATS generales.

La URL editada en pantalla dura la sesión de la app; no se guarda al reiniciar. No se incorpora un backend ni una base Oracle dentro del ZIP.

## Prueba de la actividad y capturas

Usa productos de prueba propios para evitar modificar registros ajenos.

1. Listado de PharmaSoft y Logcat GET 200.
2. Crear producto con nombre válido, precio >= 0.01, stock >= 0 y categoría existente; captura confirmación y POST 201.
3. Obtener / Editar el producto recién creado; cambiar precio o stock, guardar y capturar PUT 200 y lista actualizada.
4. Eliminar ese producto con confirmación; capturar DELETE 204 y lista actualizada.
5. Crear con nombre `ab` y demás datos válidos; capturar error del servidor debajo de nombre y Logcat 400.
6. Detener el backend o desactivar red, recargar y capturar error controlado.
7. Repetir CRUD en iOS tras integrar la app anfitriona.

No se adjuntan capturas simuladas. Guardar evidencias reales en `evidencias/`.

## Rama y commits

La guía pide partir de `develop` y al menos tres commits propios distribuidos. Este ZIP no contiene historial Git ni commits publicados. En el repositorio del curso, parte de develop y crea `feature/crud-productos-ancajima`; incorpora y verifica los cambios por etapas (DTO/API, repositorio/errores, pantalla/pruebas) con commits descriptivos durante el trabajo. No se acredita este requisito solo con el ZIP.
