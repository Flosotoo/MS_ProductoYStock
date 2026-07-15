# Microservicio Productos y Stock

## Descripción

Microservicio núcleo del catálogo de productos y la gestión de inventario para Perfulandia SPA. Administra el catálogo de productos, el stock por sucursal, las reservas de stock para pedidos web y el cálculo del estado de inventario (normal, bajo, crítico). Es el microservicio más consumido del sistema: lo usan Ventas, Proveedores, Envíos, Atención al Cliente y Reportes.

- Historias de usuario: HU-10 a HU-15, HU-49 y HU-51.
- Swagger/OpenAPI disponible en: <http://localhost:8082/swagger-ui.html>

## Estudiante

Florencia Soto

## Tecnologías

- Java 25, Spring Boot 4.x, JPA/Hibernate, Bean Validation
- MySQL 8.x (para Duoc/XAMPP)
- Comunicación entre microservicios vía RestTemplate (consume MS Sucursales)
- Maven, Swagger/OpenAPI (springdoc)

## Microservicios que consume

| MS destino | Puerto | Para qué |
| ---------- | ------ | -------- |
| MS Sucursales y Logística | 8087 | Validar que la sucursal exista al registrar inventario |

La validación de sucursal usa **degradación elegante**: si MS Sucursales está caído (timeout, `ResourceAccessException`), el registro de inventario continúa con una advertencia en el log; pero si el MS responde y confirma que la sucursal no existe, sí se bloquea la operación (404). El `RestTemplate` se configura con timeouts de conexión y lectura de 3 segundos (`RestTemplateConfig`).

## Microservicios que lo consumen

Este es el MS central del sistema. Otros microservicios lo llaman para:

| MS origen | Qué consume |
| --------- | ----------- |
| MS Ventas | Validar productos, consultar disponibilidad y descontar/reingresar stock; confirmar reservas de pedidos web |
| MS Proveedores | Validar productos e ingresar stock al recibir órdenes de compra |
| MS Envíos | Validar productos de los pedidos |
| MS Atención al Cliente | Validar productos para reseñas |
| MS Reportes | Consultar inventario para reportes |

## Endpoints

### Productos

| Método | Ruta | HU | Descripción |
| ------ | ---- | -- | ----------- |
| POST | `/api/productos` | HU-11 | Registrar producto (SKU autogenerado) |
| GET | `/api/productos` | — | Listar todos los productos |
| GET | `/api/productos/{id}` | — | Obtener un producto por id |
| PUT | `/api/productos/{id}` | HU-49 | Editar producto (el SKU no se puede cambiar) |
| GET | `/api/productos/buscar?nombre=` | HU-10 | Buscar productos por nombre (parcial) |
| GET | `/api/productos/categoria/{categoria}` | HU-10 | Buscar productos por categoría |
| POST | `/api/productos/lote-parcial` | — | Crear productos en lote (éxito parcial) |
| DELETE | `/api/productos/{id}` | HU-14 | Eliminar un producto |

### Inventario

| Método | Ruta | HU | Descripción |
| ------ | ---- | -- | ----------- |
| POST | `/api/inventario` | HU-11 | Registrar stock de un producto en una sucursal |
| GET | `/api/inventario` | HU-12 | Listar todo el inventario |
| GET | `/api/inventario/{id}` | — | Obtener un inventario por id |
| GET | `/api/inventario/disponibilidad?idProducto=&idSucursal=` | HU-15 | Verificar disponibilidad (cantidad − reservada) |
| GET | `/api/inventario/consulta?idProducto=&idSucursal=` | — | Consultar stock con datos del producto |
| GET | `/api/inventario/consulta-busqueda?sku=&nombre=&idSucursal=` | HU-51 | Consultar stock por SKU o nombre |
| PUT | `/api/inventario/{id}` | — | Actualizar un inventario |
| PUT | `/api/inventario/ajustar` | HU-13 | Ajustar stock físico (delta positivo o negativo) |
| PUT | `/api/inventario/apartar` | — | Reservar stock (sube cantidad reservada) |
| PUT | `/api/inventario/cancelar-reserva` | — | Liberar una reserva |
| PUT | `/api/inventario/confirmar-reserva` | — | Confirmar reserva (salida física real) |
| POST | `/api/inventario/lote-parcial` | — | Registrar inventario en lote (éxito parcial) |
| DELETE | `/api/inventario/{id}` | — | Eliminar un registro de inventario |

## Ejecución

```
./mvnw spring-boot:run
```

El servidor corre en **<http://localhost:8082>**.

Requiere que MySQL esté corriendo (XAMPP). La base de datos `db_productos_stock` se crea automáticamente (`createDatabaseIfNotExist=true`) y las tablas vía Hibernate (`ddl-auto=update`).

## Pruebas automatizadas

### Tests unitarios y de integración (JUnit + Mockito)

```
./mvnw test
```

El MS incluye varios niveles de prueba:

- **`InventarioServiceTest`** (unitario, Mockito): valida las reglas de negocio del inventario — apartar, cancelar y confirmar reserva, ajuste de stock (incluyendo idempotencia por idOperacion y rechazo cuando quedaría bajo lo reservado), registro de inventario con validación de producto y sucursal, verificación de disponibilidad, consulta de stock (por producto y por SKU/nombre) y carga en lote con éxito parcial.
- **`ProductoServiceTest`** (unitario, Mockito): valida la generación automática de SKU, el rechazo por SKU duplicado, la asignación de estado ACTIVO por defecto, búsquedas por nombre/categoría y la carga de productos en lote con éxito parcial.
- **`InventarioControllerTest`** (`@WebMvcTest`): valida la capa web del inventario aislada — códigos HTTP correctos (200/204/404/409) con el service mockeado, cubriendo disponibilidad (200 y 404), listado (200/204), apartado con stock insuficiente (409) y ajuste de stock (200).
- **`ProductoControllerTest`** (`@WebMvcTest`): valida la capa web de productos aislada — códigos HTTP correctos (200/201/204/404/409) con el service mockeado.
- **`InventarioControllerIT`** (`@SpringBootTest` + `@ActiveProfiles("test")`): valida la cadena completa controller → service → base de datos (H2 en memoria), mockeando la llamada a MS Sucursales. Verifica la creación de inventario (201, con cálculo de estado de stock) y el 404 por inventario inexistente.
- **`MscatalogoApplicationTests`** (`@SpringBootTest`): prueba de humo que verifica que el contexto de Spring levanta correctamente (`contextLoads`).

## Conceptos clave

### Stock disponible vs reservado

El inventario distingue entre el stock total y el comprometido:

```
disponible = cantidad − cantidadReservada
```

- **cantidad:** unidades físicas en la sucursal
- **cantidadReservada:** unidades apartadas para pedidos web en curso (aún no retiradas)
- **disponible:** lo que realmente se puede vender

El flujo de reservas tiene tres operaciones: **apartar** (sube reservada), **cancelar reserva** (baja reservada) y **confirmar reserva** (salida física: baja cantidad Y reservada).

### Estado de stock

Cada inventario calcula su estado automáticamente sobre el **disponible**:

| Estado | Condición |
| ------ | --------- |
| CRITICO | disponible ≤ 0 |
| BAJO | disponible ≤ umbral mínimo |
| NORMAL | disponible > umbral mínimo |

(Excepción: un producto DESCONTINUADO nunca se marca CRITICO, solo BAJO.)

## Estructura de requests y respuestas

### POST /api/productos — Registrar producto

```
// Request (el SKU se genera solo, no se envía)
{
  "nombre": "Perfume Eros Versace 100ml",
  "precio": 45000,
  "descripcion": "Fragancia masculina amaderada",
  "categoria": "PERFUME"
}

// Response: 201 Created
{
  "idProducto": 1,
  "sku": "PERA1B2C",
  "nombre": "Perfume Eros Versace 100ml",
  "precio": 45000,
  "descripcion": "Fragancia masculina amaderada",
  "categoria": "PERFUME",
  "estado": "ACTIVO"
}
```

**Reglas de negocio:**

- El SKU se genera automáticamente: prefijo de 3 letras de la categoría + 5 caracteres aleatorios (8 caracteres en total)
- Si se envía un SKU que ya existe, se rechaza (409 Conflict)
- El estado se asigna ACTIVO por defecto
- Categorías válidas: `PERFUME`, `COLONIA`, `CUIDADO_PERSONAL`, `BODY_SPLASH`, `SET_REGALO`

### POST /api/inventario — Registrar stock

```
// Request
{
  "producto": { "idProducto": 1 },
  "idSucursal": 1,
  "cantidad": 50,
  "umbralMinimo": 10
}

// Response: 201 Created
{
  "idInventario": 1,
  "producto": { "idProducto": 1, "sku": "PERA1B2C", "nombre": "Perfume Eros Versace 100ml", ... },
  "idSucursal": 1,
  "cantidad": 50,
  "cantidadReservada": 0,
  "umbralMinimo": 10,
  "estadoStock": "NORMAL"
}
```

**Reglas de negocio:**

- El producto debe existir (404 si no)
- La sucursal se valida contra MS Sucursales (con degradación elegante)
- Un producto solo puede tener un registro de inventario por sucursal (restricción única `uk_inventario_producto_sucursal`)
- El estado de stock se calcula automáticamente; no se envía

### GET /api/inventario/disponibilidad — Verificar disponibilidad

```
GET /api/inventario/disponibilidad?idProducto=1&idSucursal=1

// Response: 200 OK
35
```

Devuelve un número entero: la cantidad disponible (cantidad − reservada). Lo usa MS Ventas antes de descontar stock. 404 si no existe inventario para ese producto en esa sucursal.

### PUT /api/inventario/ajustar — Ajustar stock

```
// Request (delta positivo suma, negativo resta)
{
  "idProducto": 1,
  "idSucursal": 1,
  "cantidad": 20
}

// Response: 200 OK → inventario actualizado
```

**Reglas de negocio:**

- El delta puede ser positivo (recepción de mercadería) o negativo (venta)
- No se permite dejar la cantidad por debajo de lo reservado (409 Conflict): protege el stock ya comprometido
- Lo usan MS Ventas (descuento por venta) y MS Proveedores (ingreso por recepción de orden)

> **Nota:** el endpoint `/ajustar` recibe un `idOperacion` en el cuerpo, pero actualmente el controlador no lo propaga al servicio (ver la sección *Idempotencia del ajuste de stock*).

### PUT /api/inventario/apartar — Reservar stock

```
// Request
{
  "idProducto": 1,
  "idSucursal": 1,
  "cantidad": 5
}

// Response: 200 OK → inventario con cantidadReservada aumentada
```

**Regla:** Solo se puede apartar lo disponible (409 Conflict si se intenta apartar más, o si la cantidad no es positiva). La cantidad física no cambia, solo sube la reservada.

### GET /api/inventario/consulta-busqueda — Consultar stock por SKU o nombre (HU-51)

```
GET /api/inventario/consulta-busqueda?sku=PERA1B2C&idSucursal=1
GET /api/inventario/consulta-busqueda?nombre=perfume&idSucursal=1

// Response: 200 OK
[
  {
    "sku": "PERA1B2C",
    "nombre": "Perfume Eros Versace 100ml",
    "cantidadDisponible": 35,
    "umbralMinimo": 10,
    "estadoStock": "NORMAL"
  }
]
```

Busca por SKU exacto o por nombre parcial y devuelve la disponibilidad y el estado de stock en la sucursal indicada. 404 si el producto no existe, o si existe pero no tiene inventario en esa sucursal.

### Endpoints de lote (éxito parcial)

Los endpoints `/lote-parcial` (productos e inventario) procesan varios elementos y devuelven los exitosos y los errores por separado, con códigos según el resultado:

- **201 Created:** todos se guardaron correctamente
- **207 Multi-Status:** algunos se guardaron y otros fallaron (éxito parcial)
- **400 Bad Request:** ninguno se guardó

Cada elemento se valida individualmente (Bean Validation por ítem) y captura por separado los errores de regla de negocio (SKU duplicado, producto/sucursal inexistente) y de integridad de la base de datos, reportándolos con su posición en el lote.

## Manejo de errores

El MS usa un `GlobalExceptionHandler` que traduce las excepciones a códigos HTTP coherentes:

| Excepción | Código | Cuándo |
| --------- | ------ | ------ |
| `RecursoNoEncontradoException` | 404 Not Found | Producto, inventario o sucursal inexistente |
| `RecursoDuplicadoException` | 409 Conflict | SKU de producto duplicado |
| `StockInsuficienteException` | 409 Conflict | Apartar/ajustar más de lo disponible o por debajo de lo reservado |
| `DataIntegrityViolationException` | 409 Conflict | El recurso ya existe o viola una restricción de la base de datos (ej. SKU o par producto-sucursal duplicado a nivel de BD) |
| `MethodArgumentNotValidException` | 400 Bad Request | Validación de campos fallida |
| `HttpMessageNotReadableException` | 400 Bad Request | JSON mal formado |
| `RestClientException` | 502 Bad Gateway | Error al comunicarse con MS Sucursales |

## Idempotencia del ajuste de stock

El servicio de inventario implementa idempotencia a nivel de método: `ajustarStock` acepta un `idOperacion` opcional y, si ese `idOperacion` ya fue procesado (registrado en la tabla `operacion_stock`, con restricción única `uk_operacion`), no vuelve a ajustar el stock. El objetivo es proteger contra reintentos de red que duplicarían el descuento (Ventas) o el ingreso (Proveedores) de stock.

> **Nota importante (estado actual):** el endpoint REST `PUT /api/inventario/ajustar` invoca al servicio pasando `idOperacion = null`, es decir, **ignora el `idOperacion` que venga en el cuerpo de la petición**. Aunque MS Ventas y MS Proveedores sí envían un `idOperacion` (con prefijos como `venta-…`, `orden-…`, `devolucion-…`), este no se propaga, por lo que la idempotencia **no queda activa a través de la API** y la tabla `operacion_stock` no se llena en uso normal. Para activarla, el controlador debería pasar `ajuste.getIdOperacion()` en lugar de `null`. El mecanismo está cubierto por pruebas unitarias que llaman al servicio directamente con un `idOperacion`.

## Configuración de base de datos

La aplicación usa MySQL. La base de datos `db_productos_stock` se crea automáticamente (`createDatabaseIfNotExist=true`). Las tablas se crean vía Hibernate (`ddl-auto=update`).

Credenciales por defecto en `application.properties`:

- Usuario: `root`
- Contraseña: *(vacía, como en XAMPP por defecto)*

URL del microservicio que consume (en `application.properties`):

```
ms.sucursales.url=http://localhost:8087/api/v1/sucursales/
```

## Swagger / OpenAPI

Documentación interactiva disponible en:

- Swagger UI: <http://localhost:8082/swagger-ui.html>
- API Docs (JSON): <http://localhost:8082/v3/api-docs>

Cada endpoint está documentado con su Historia de Usuario correspondiente para trazabilidad.