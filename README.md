# 🖥️ 電腦組裝商城（含相容性檢查）

期末專題 —— 一個支援零件瀏覽、下單、CPU/主機板相容性驗證、管理者後台的電商後端系統。

技術棧：**Java 25 · Spring Boot 4.0.6 · PostgreSQL 15 · Flyway · Spring Security 7（JWT）· jjwt 0.12.6 · Lombok · springdoc-openapi 3.0.1**

---

## 📌 核心功能

- **會員系統**：JWT 註冊 / 登入 / refresh token 輪換 / 登出
- **零件管理**：CPU、主機板、GPU、PSU 等零件的 CRUD，可依分類查詢
- **下單系統**：下單時檢查庫存、計算總金額，整個流程包在 `@Transactional` 內，確保資料一致性
- **相容性檢查（本專題技術亮點）**：下單時自動比對 CPU 與主機板的 socket 是否相符，不相容的組合會被攔截並回傳明確錯誤訊息，不會建立無效訂單
- **權限分級**：一般使用者（USER）與管理者（ADMIN）分開的 API 權限——管理者可查看所有訂單、更新訂單狀態，一般使用者只能查看自己的訂單
- **統一錯誤格式**：所有例外（驗證失敗、找不到資源、庫存不足、相容性錯誤等）都經過 `@RestControllerAdvice` 統一處理，回傳一致的 JSON 格式，不會有裸露的 500 stack trace

---

## ⚡ 本機啟動步驟

### 1. 準備資料庫（PostgreSQL 容器）

```bash
docker run -d --name my_postgres \
  -e POSTGRES_PASSWORD=my_secret_password \
  -p 5433:5432 postgres:15

docker exec my_postgres psql -U postgres -c "CREATE DATABASE starter_db;"
```

### 2. 啟動專案

```bash
./mvnw spring-boot:run
```

看到 `Started StarterApplication` 即成功。Flyway 會依序執行以下 migration，自動建好所有資料表：

- `V1__auth_schema.sql`：會員、角色、權限、refresh token 相關表
- `V2__create_components.sql`：零件表
- `V3__create_orders.sql`：訂單、訂單明細表

### 3. 開啟 Swagger UI 測試 API

```
http://localhost:8080/swagger-ui/index.html
```

測試流程：
1. 展開 `POST /api/auth/login`，填入帳密送出，複製回應裡的 `accessToken`
2. 點右上角 **Authorize** 按鈕，貼上 token
3. 之後即可直接在頁面上測試所有需要登入的 API

---

## 📋 API 清單

| 方法 | 路徑 | 說明 | 權限 |
|---|---|---|---|
| POST | `/api/auth/register` | 註冊 | 公開 |
| POST | `/api/auth/login` | 登入 | 公開 |
| POST | `/api/auth/refresh` | 刷新 token（含輪換機制） | 公開 |
| GET | `/api/components` | 查詢零件（可用 `?category=` 篩選） | 公開 |
| GET | `/api/components/{id}` | 查詢單一零件 | 公開 |
| POST | `/api/components` | 新增零件 | 需登入 |
| PUT | `/api/components/{id}` | 更新零件 | 需登入 |
| DELETE | `/api/components/{id}` | 刪除零件 | 需登入 |
| POST | `/api/orders` | 下單（含相容性檢查、庫存扣減） | 需登入 |
| GET | `/api/orders/mine` | 查詢自己的訂單 | 需登入 |
| GET | `/api/admin/orders` | 查詢所有訂單 | ADMIN |
| PATCH | `/api/admin/orders/{id}/status` | 更新訂單狀態 | ADMIN |

---

## 🧪 測試帳號設定

先註冊一般帳號：

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"test1","email":"test1@example.com","password":"password123"}'
```

若要測試管理者權限，進資料庫手動綁定角色：

```sql
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE u.username = 'test1' AND r.name = 'ROLE_ADMIN';
```

角色資訊寫在 JWT 裡，綁定後要**重新登入**才會拿到含 `ROLE_ADMIN` 的新 token。

---

## 🗂️ 資料庫 ERD

- `users` 與 `roles` 是多對多關係(透過 `user_roles`)
- `roles` 與 `permissions` 是多對多關係(透過 `role_permissions`)
- 一個 `user` 可以有多筆 `refresh_tokens`、多筆 `orders`
- 一筆 `order` 可以包含多筆 `order_items`,每筆 `order_item` 對應一個 `component`

```mermaid
erDiagram
    USERS ||--o{ USER_ROLES : has
    ROLES ||--o{ USER_ROLES : has
    ROLES ||--o{ ROLE_PERMISSIONS : has
    PERMISSIONS ||--o{ ROLE_PERMISSIONS : has
    USERS ||--o{ REFRESH_TOKENS : has
    USERS ||--o{ ORDERS : places
    ORDERS ||--o{ ORDER_ITEMS : contains
    COMPONENTS ||--o{ ORDER_ITEMS : "referenced by"

    USERS {
        bigint id PK
        varchar username
        varchar email
        varchar password
        boolean enabled
        timestamp created_at
    }

    ROLES {
        bigint id PK
        varchar name
    }

    PERMISSIONS {
        bigint id PK
        varchar name
    }

    REFRESH_TOKENS {
        bigint id PK
        bigint user_id FK
        varchar token
        timestamp expires_at
    }

    COMPONENTS {
        bigint id PK
        varchar name
        varchar category
        varchar brand
        decimal price
        int stock
        varchar socket
        int power_watt
        timestamp created_at
    }

    ORDERS {
        bigint id PK
        bigint user_id FK
        varchar status
        decimal total_amount
        timestamp created_at
    }

    ORDER_ITEMS {
        bigint id PK
        bigint order_id FK
        bigint component_id FK
        int quantity
        decimal unit_price
    }
```

> GitHub 網頁會自動把上面的 Mermaid 語法渲染成圖表,不需要額外匯出圖片。也可以先貼到 [mermaid.live](https://mermaid.live) 預覽。

## ⚠️ 已知限制 / 未實作項目

- **Redis 快取、Docker Compose 一鍵部署**：受限於開發時間，本次未實作，是後續可優化方向
- **相容性檢查**目前只涵蓋「CPU 與主機板 socket 是否相符」這一條規則，可擴充方向：RAM 是否對應主機板記憶體類型、PSU 瓦數是否足夠支撐整套零件

---

## 💣 開發踩坑紀錄（附錄）

| # | 坑 | 症狀 | 解法 |
|---|----|------|------|
| 1 | 新 Entity 沒寫 migration | 啟動就爆 `missing table` | `ddl-auto` 是 `validate`，每張新表都要有 `V<n>__*.sql` |
| 2 | `GenerationType.AUTO` | 啟動爆 `missing sequence` | 一律用 `IDENTITY` |
| 3 | Service 有更新/刪除邏輯沒加 `@Transactional` | 第一次能動、第二次才爆 | 涉及多步驟資料庫操作的方法加 `@Transactional` |
| 4 | Entity 用 `@Builder` + 欄位初始值 | builder 建出來欄位是 null，INSERT 撞 NOT NULL | 初始值欄位加 `@Builder.Default` |
| 5 | Enum 欄位沒 `@Enumerated(EnumType.STRING)` | schema 驗證型別不符 | 加註解，DB 用 VARCHAR |
| 6 | 忘了在 SecurityConfig 放行公開 API | 前端一直 401/403 | 檢查 API 清單跟 SecurityConfig 規則是否一一對應 |
| 7 | Spring Security 過濾器層擋下的 403（例如 `hasRole` 不符）| 回應 body 是空的，不是自訂 JSON 格式 | 這類 403 發生在 filter chain，不會進到 `@RestControllerAdvice`；如需統一格式要另外設定 `AccessDeniedHandler` |

---

## 🔄 常用指令

```bash
./mvnw spring-boot:run          # 啟動
./mvnw clean test-compile       # 編譯檢查
./mvnw clean package            # 打包

# 資料庫整個重來（會清光資料！Flyway 會重新從 V1 跑）
docker exec my_postgres psql -U postgres -c "DROP DATABASE starter_db;" -c "CREATE DATABASE starter_db;"

# 進資料庫看表
docker exec -it my_postgres psql -U postgres -d starter_db
```
