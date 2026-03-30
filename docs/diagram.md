# Arquitectura C4 - Nivel 2: Contenedores

## Escenario de Compra de Productos

```mermaid
graph TD
    User([Usuario / Navegador]) -->|HTTPS / Vue 3| Frontend[Frontend Container: Vue 3 + Vite]
    
    subgraph "Sistema de Microservicios"
        Frontend -->|API REST + JWT| ProductsService[Products Service: Spring Boot]
        Frontend -->|API REST + JWT| InventoryService[Inventory Service: Spring Boot]
        
        InventoryService -->|HTTP + API Key| ProductsService
        
        ProductsService -->|JDBC| ProductsDB[(Products DB: PostgreSQL)]
        InventoryService -->|JDBC| InventoryDB[(Inventory DB: PostgreSQL)]
    end
```

### Componentes:

1. **Frontend**: Aplicación Vue 3 que maneja la interacción del usuario, estado con Pinia y seguridad con JWT.
2. **Products Service**: Gestiona el catálogo de productos. Expone endpoints públicos (JWT) y privados (API Key).
3. **Inventory Service**: Gestiona el stock y las transacciones de compra. Implementa resiliencia y control de concurrencia optimista.
4. **Bases de Datos**: Esquemas separados en PostgreSQL para mantener el desacoplamiento de los microservicios.
