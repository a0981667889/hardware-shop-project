# 🚀 Spring Boot JWT 起手專案（課程模板）

> **這個模板幫你把最麻煩的都打通了**：JWT 註冊／登入／refresh／登出、Spring Security 設定、Flyway、PostgreSQL 連線。
> **你要做的只有一件事：專注在你自己的業務功能** —— 設計 Schema、寫 Entity / Repository / Service / Controller，最後把 API 路徑加進 SecurityConfig。

技術棧（跟課程一致，不要自己改版本）：**Java 25 · Spring Boot 4.0.6 · PostgreSQL 15 · Flyway · Spring Security 7 · jjwt 0.12.6 · Lombok**

---

## ⚡ 第一次啟動（5 分鐘）

### 1. 準備資料庫（PostgreSQL 容器）

```bash
# 已經有課程的 my_postgres 容器就跳過這步
docker run -d --name my_postgres \
  -e POSTGRES_PASSWORD=my_secret_password \
  -p 5433:5432 postgres:15

# 建立這個專案用的資料庫
docker exec my_postgres psql -U postgres -c "CREATE DATABASE starter_db;"
```

### 2. 啟動專案

```bash
./mvnw spring-boot:run
```

看到 `Started StarterApplication` 就成功了。Flyway 會自動建好認證相關的表（users / roles / permissions / refresh_tokens）。

### 3. 驗證 JWT 有通（照順序執行）

```bash
# ① 註冊 → 201
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"test1","email":"test1@example.com","password":"password123"}'

# ② 登入 → 200，拿到 accessToken 和 refreshToken
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"test1","password":"password123"}'

# ③ 帶 token 打受保護 API → 200 回你的使用者資訊
#    （把 <TOKEN> 換成上一步的 accessToken）
curl http://localhost:8080/api/example/protected \
  -H "Authorization: Bearer <TOKEN>"

# ④ 不帶 token 打同一支 → 401/403（證明保護有效）
curl -i http://localhost:8080/api/example/protected
```

四步全過 = 模板正常，開始做你的功能。

---

## 📁 專案結構（✅ 已打通不用動；✏️ 你要寫的）

```
src/main/java/com/example/starter/
├── StarterApplication.java        ✅ 進入點
├── config/
│   └── SecurityConfig.java        ✏️ 只改一個地方：把你的 API 路徑規則加進標記區
├── security/                      ✅ JWT 全套（JwtUtils / Filter / UserPrincipal / UserDetailsService）
├── entity/
│   ├── User / Role / Permission / RefreshToken   ✅ 認證用
│   └── （你的 Entity 加在這裡）    ✏️
├── repository/
│   ├── User / Role / RefreshToken Repository     ✅
│   └── （你的 Repository 加在這裡）✏️
├── service/
│   ├── RefreshTokenService.java   ✅
│   └── （你的 Service 加在這裡）   ✏️
├── controller/
│   ├── AuthController.java        ✅ 註冊/登入/refresh/登出
│   ├── ExampleController.java     🎓 示範用，看懂後可刪
│   └── （你的 Controller 加在這裡）✏️
├── dto/                           ✏️ 你的 Request/Response DTO（已有登入註冊的可參考）
└── exception/                     ✏️ 你的自訂例外（已有兩個 Token 例外可參考）

src/main/resources/
├── application.yaml               ✅（改 DB 名稱可以，其他別動）
└── db/migration/
    ├── V1__auth_schema.sql        ✅ 認證表，不要改這支！
    └── V2__your_schema.sql        ✏️ 你的業務表從 V2 開始
```

---

## ✏️ 你的開發流程（每個功能都走這五步）

以「商品 Product」為例：

### Step 1：設計 Schema → 開新的 migration

建 `src/main/resources/db/migration/V2__create_products.sql`（**永遠開新檔，不改舊的 V1**）：

```sql
CREATE TABLE products (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name        VARCHAR(200)   NOT NULL,
    price       DECIMAL(10, 2) NOT NULL,
    stock       INT            NOT NULL DEFAULT 0,
    created_at  TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

> PostgreSQL 語法注意：主鍵用 `GENERATED ALWAYS AS IDENTITY`（不是 MySQL 的 `AUTO_INCREMENT`）；索引要獨立 `CREATE INDEX`。

### Step 2：寫 Entity

```java
@Entity
@Table(name = "products")
@Getter @Setter
@NoArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)   // 一定用 IDENTITY，不要用 AUTO
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer stock = 0;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
```

### Step 3：寫 Repository

```java
public interface ProductRepository extends JpaRepository<Product, Long> {
    // 衍生查詢的回傳型別用 Optional<T> / List<T> / Page<T>
    Optional<Product> findByName(String name);
}
```

### Step 4：寫 Service（業務邏輯放這裡，不放 Controller）＋ Controller

參考 `RefreshTokenService` / `AuthController` 的寫法。拿目前登入者：

```java
@GetMapping("/mine")
public List<OrderResponse> myOrders(@AuthenticationPrincipal UserPrincipal user) {
    return orderService.findByUserId(user.getId());
}
```

### Step 5：把 API 路徑加進 SecurityConfig 👈 **別忘了這步！**

打開 `config/SecurityConfig.java`，找到 `👇👇👇 你的 API 權限規則加在這裡 👇👇👇` 標記區：

```java
// 商品查詢公開、修改要登入（沒寫的路徑預設「要登入」）
.requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()
// 管理端只給 ADMIN
.requestMatchers("/api/admin/**").hasRole("ADMIN")
```

規則由上而下比對：**具體的寫前面**，最後一條 `anyRequest().authenticated()` 不要動。
新 API 忘了設定也不會裸奔——預設就是要登入。

---

## 💣 高頻踩坑（都是真實案例，先看再寫）

| # | 坑 | 症狀 | 解法 |
|---|----|------|------|
| 1 | 新 Entity 沒寫 migration | 啟動就爆 `missing table` | `ddl-auto` 是 `validate`，每張新表都要有 `V<n>__*.sql` |
| 2 | `GenerationType.AUTO` | 啟動爆 `missing sequence` | 一律用 `IDENTITY` |
| 3 | Service 有 `deleteByXxx` 沒加 `@Transactional` | **第一次能動、第二次才爆** | Service 類別加 `@Transactional` |
| 4 | Entity 用 `@Builder` + 欄位初始值 | builder 建出來欄位是 null，INSERT 撞 NOT NULL | 初始值欄位加 `@Builder.Default` |
| 5 | Enum 欄位沒 `@Enumerated(EnumType.STRING)` | schema 驗證型別不符 | 加註解，DB 用 VARCHAR |
| 6 | 抄到 Boot 3 教學的 Security 寫法 | `new DaoAuthenticationProvider()` 編譯錯誤 | 本模板已是 Boot 4 寫法，照模板 |
| 7 | 忘了在 SecurityConfig 放行公開 API | 前端一直 401/403 | 回去走 Step 5 |
| 8 | 問 AI 不講版本 | 拿到 Boot 3 + MySQL 程式碼 | 開頭聲明「Java 25、Spring Boot 4.0.6、PostgreSQL 15、Flyway」 |

---

## 🔄 常用指令

```bash
./mvnw spring-boot:run          # 啟動
./mvnw clean test-compile       # 編譯檢查（改完一批程式先跑這個）
./mvnw clean package            # 打包

# 資料庫整個重來（會清光資料！Flyway 會重新從 V1 跑）
docker exec my_postgres psql -U postgres -c "DROP DATABASE starter_db;" -c "CREATE DATABASE starter_db;"

# 進資料庫看表
docker exec -it my_postgres psql -U postgres -d starter_db
```

---

## ❓ FAQ

**Q：想改專案名稱／套件名？**
可以但不急。要改的話 IDE 對 `com.example.starter` 按 Refactor → Rename，pom 的 `artifactId` 順手改，別手動搬檔案。

**Q：怎麼弄一個 ADMIN 帳號測 `/api/example/admin`？**
先註冊一個帳號，再進資料庫把 ROLE_ADMIN 綁給他：
```bash
docker exec my_postgres psql -U postgres -d starter_db -c \
  "INSERT INTO user_roles (user_id, role_id) SELECT u.id, r.id FROM users u, roles r WHERE u.username='test1' AND r.name='ROLE_ADMIN';"
```
重新登入拿新 token（角色寫在 token 裡，舊 token 不會自動更新）。

**Q：全域例外處理、Swagger、Redis 什麼時候加？**
之後課程會教（36 之後），到時候直接往這個專案上加即可。現在先把業務功能做好。
