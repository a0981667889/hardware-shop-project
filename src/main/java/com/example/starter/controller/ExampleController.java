package com.example.starter.controller;

import com.example.starter.security.UserPrincipal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 🎓 示範用 Controller —— 用來驗證 JWT 有沒有接通，也是你寫自己 Controller 的參考。
 * 確認一切正常後，這個檔案可以刪掉。
 *
 * 測試順序：
 *   1. GET /api/example/public     → 不用登入，直接 200
 *   2. GET /api/example/protected  → 沒帶 token 會 401/403；帶了 token 回你的使用者資訊
 *   3. GET /api/example/admin      → 一般使用者 403（需要 ROLE_ADMIN）
 */
@RestController
@RequestMapping("/api/example")
public class ExampleController {

    /** 公開端點（SecurityConfig 有 permitAll） */
    @GetMapping("/public")
    public Map<String, String> publicEndpoint() {
        return Map.of("message", "這是公開 API，不用登入就看得到");
    }

    /** 需要登入：用 @AuthenticationPrincipal 拿到目前登入者 */
    @GetMapping("/protected")
    public Map<String, Object> protectedEndpoint(@AuthenticationPrincipal UserPrincipal user) {
        return Map.of(
                "message", "你已成功通過 JWT 認證！",
                "userId", user.getId(),
                "username", user.getUsername(),
                "roles", user.getRoleNames()
        );
    }

    /** 需要 ADMIN 角色：@PreAuthorize 示範（@EnableMethodSecurity 已開） */
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, String> adminEndpoint() {
        return Map.of("message", "只有 ROLE_ADMIN 看得到這裡");
    }
}
