# Art Backend

Java backend for the Art Design Pro frontend.

## Modules

```text
art-admin      Spring Boot entry and web controllers
art-common     Common response, paging, exceptions and base classes
art-framework  Sa-Token, CORS, Swagger and global web configuration
art-system     System domain services: users, roles, menus, departments
```

## Stack

- JDK 17
- Spring Boot 3
- Maven multi-module
- Sa-Token
- MyBatis-Plus
- MySQL 8.0.12, phpstudy_pro

## Database

Default local database:

```text
host: 127.0.0.1
port: 3306
database: art_design
username: root
password: root
```

The application uses `createDatabaseIfNotExist=true` and runs `classpath:sql/art_system.sql` on startup. The SQL is idempotent: it creates missing tables and inserts initial records with `INSERT IGNORE`.

The SQL file used at startup is maintained at:

```text
art-admin/src/main/resources/sql/art_system.sql
```

## First Run

```bash
cd backend
mvn -f pom.xml -pl art-admin -am package -DskipTests
java -jar art-admin/target/art-admin-0.1.0-SNAPSHOT.jar
```

Default server:

```text
http://localhost:8080
```

Test accounts:

```text
Super / 123456
Admin / 123456
User  / 123456
```

## Frontend API Contract

Implemented endpoints:

```text
POST /api/auth/login
POST /api/auth/logout
GET  /api/user/info

GET  /api/user/list
GET  /api/user/{id}
POST /api/user
PUT  /api/user
PUT  /api/user/status
PUT  /api/user/reset-password
DEL  /api/user/{ids}

GET  /api/role/list
GET  /api/role/{id}
POST /api/role
PUT  /api/role
PUT  /api/role/status
GET  /api/role/menu-tree
GET  /api/role/{id}/menu-ids
PUT  /api/role/menu
DEL  /api/role/{ids}

GET  /api/v3/system/menus
GET  /api/menu/list
GET  /api/menu/{id}
POST /api/menu
PUT  /api/menu
PUT  /api/menu/status
DEL  /api/menu/{ids}

GET  /api/dept/list
GET  /api/dept/{id}
POST /api/dept
PUT  /api/dept
PUT  /api/dept/status
DEL  /api/dept/{ids}

GET  /api/health
```

Unified response:

```json
{
  "code": 200,
  "msg": "success",
  "data": {}
}
```

MySQL table structure is in `art-admin/src/main/resources/sql/art_system.sql`.
