package com.example.starter.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "電商後端 API",
                version = "v1.0.0",
                description = "提供商品、訂單、會員管理的 RESTful API",
                contact = @Contact(name = "後端團隊", email = "backend@example.com")
        ),
        servers = {
                @Server(url = "http://localhost:8080", description = "本地開發環境"),
                @Server(url = "https://api.example.com", description = "正式環境")
        }
)
// 定義 JWT Bearer Token 認證方式
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class OpenApiConfig {
    // 設定都在註解上，這裡不需要 Bean
}
