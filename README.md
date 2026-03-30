📦 Products & Inventory Microservices

Sistema end-to-end de gestión de productos e inventario construido con Spring Boot, enfocado en:
- Arquitectura de microservicios
- Resiliencia
- Consistencia de datos
- Manejo de concurrencia

🧠 Arquitectura

Microservicios:

Products Service (8080)
- CRUD completo
- Búsqueda
- Filtros
- Paginación

Inventory Service (8081)
- Consulta de stock
- Compras seguras
- Control de concurrencia

Comunicación:
- HTTP (WebClient)
- API Key (X-API-KEY)
- Resilience4j (Retry + Circuit Breaker + Fallback)

⚙️ Stack
- Java 21
- Spring Boot 3.5
- Spring Data JPA
- PostgreSQL (H2 for tests)
- Resilience4j
- Maven
- Docker + Docker Compose
- Vue 3 + Pinia + Router

🔐 Seguridad

Interna:
X-API-KEY: secret123

Externa:
JWT para autenticación. Usuario: `admin`, Password: `admin`. Inventory acepta JWT para endpoints expuestos al frontend y mantiene API Key para comunicación interna con Products.

📊 Features

Products Service:
- Crear producto
- Actualizar producto (PUT)
- Eliminar producto (DELETE)
- Listar productos con paginación, filtro por status, búsqueda y ordenamiento
- Consultar producto por id
- SKU único (409)
- sortBy soportado: `createdAt,asc|desc` o `price,asc|desc`
- Timestamps automáticos
- Rate Limiting (60 req/min)
- Swagger UI: `http://localhost:8080/swagger-ui.html`

Inventory Service:
- Consulta inventario
- Compra productos (Segura ante concurrencia)
- Idempotencia real (Verificación previa en DB)
- Validación contra products-service
- Resilience4j (Retry + Circuit Breaker + TimeLimiter). Si products-service está caído, se propaga 503 (Service Unavailable) de forma consistente.
- Swagger UI: `http://localhost:8081/swagger-ui.html`

🎨 Frontend (Vue 3)
- Manejo de estado con Pinia
- Enrutamiento con Vue Router
- Interceptores para JWT
- Vistas de Login, Listado y Detalle

🧠 Decisiones Técnicas
- **Concurrencia**: Se utiliza Optimistic Locking (@Version) para asegurar que dos compras no dejen stock negativo sin bloquear la base de datos de forma pesada.
- **Idempotencia**: Se verifica la existencia de la `Idempotency-Key` en la tabla `purchase_transaction` antes de procesar cualquier descuento.
- **Resiliencia**: Se implementó un `ProductClient` con Retry y Circuit Breaker para manejar caídas del servicio de productos.

🧪 Ejecución

```bash
docker compose up -d --build
```

Acceso:
- Frontend: `http://localhost:3000`
- Products Service: `http://localhost:8080`
- Inventory Service: `http://localhost:8081`

Variables (Docker Compose):
- `APP_API_KEY` (products-service): API key esperada en `X-API-KEY`
- `PRODUCTS_URL` (inventory-service): URL base de products-service
- `PRODUCTS_API_KEY` (inventory-service): API key enviada a products-service en `X-API-KEY`

🧪 Pruebas
Backend: se implementaron pruebas unitarias y una integración con Testcontainers en products-service.
```bash
# products-service
./mvnw test
```

```bash
# inventory-service
./mvnw test
```
*Nota: En algunos entornos de CI/CD puede requerir configuraciones adicionales para Mockito Agent.*

- Compra
- Idempotencia
- Concurrencia
- Resiliencia

E2E (bash):
- Requiere `bash`, `curl`, `jq` y `docker`
- Ejecuta:

```bash
bash test_endpoints.sh
```

📬 Ejemplos

Login (obtener JWT):

```bash
curl -s -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin"}'
```

Listar productos (paginación/filtros/búsqueda/orden):

```bash
curl -s "http://localhost:8080/products?page=0&size=10&status=ACTIVE&search=sku&sortBy=createdAt,desc" \
  -H "Authorization: Bearer <TOKEN>"
```

Crear producto:

```bash
curl -s -X POST http://localhost:8080/products \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <TOKEN>" \
  -d '{"sku":"SKU-001","name":"Producto","price":100,"status":"ACTIVE"}'
```

Actualizar producto:

```bash
curl -s -X PUT http://localhost:8080/products/<PRODUCT_ID> \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <TOKEN>" \
  -d '{"sku":"SKU-001","name":"Producto editado","price":110,"status":"ACTIVE"}'
```

Eliminar producto:

```bash
curl -s -X DELETE http://localhost:8080/products/<PRODUCT_ID> \
  -H "Authorization: Bearer <TOKEN>"
```

Crear inventario:

```bash
curl -s -X POST "http://localhost:8081/inventory?productId=<PRODUCT_ID>&quantity=10" \
  -H "Authorization: Bearer <TOKEN>"
```

Consultar inventario:

```bash
curl -s "http://localhost:8081/inventory/<PRODUCT_ID>" \
  -H "Authorization: Bearer <TOKEN>"
```

Comprar:

```bash
curl -s -X POST "http://localhost:8081/inventory/purchase" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Idempotency-Key: purchase-1" \
  -d '{"productId":"<PRODUCT_ID>","quantity":2}'
```

🧱 Decisiones técnicas

- Idempotencia: tabla dedicada
- Concurrencia: optimistic locking
- Resiliencia: Resilience4j
- Comunicación: WebClient + API Key
- Arquitectura: microservicios
- Errores: `{ "errors": [...] }` (estilo JSON:API)

📈 Observabilidad

- Correlation ID
- Actuator (/health, /info)

📄 API Contract

- 404 Not Found
- 409 Conflict
- 422 Validation Error
- 500 Internal Server Error
- 503 Service Unavailable

🚧 Estado actual / Pendientes

- Ajustado: CRUD completo en Products (incluye PUT/DELETE).
- Ajustado: Inventory acepta JWT para frontend y mantiene API Key para llamadas internas.
- Ajustado: Inventario propaga 503 cuando Products no está disponible.
- Nota: Las pruebas de frontend requieren entorno con Node/npm y Playwright.

📊 Estado

✔ Docker Compose (end-to-end)
✔ Concurrencia (optimistic locking)
✔ Idempotencia (Idempotency-Key)
✔ Filtros/paginación/ordenamiento en listado de productos
✔ Frontend (login + catálogo + detalle + compra)
