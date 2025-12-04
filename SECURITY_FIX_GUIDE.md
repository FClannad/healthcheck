# 安全修复实施指南

本文档提供了修复 HealthCheck 项目中识别的安全漏洞的详细步骤和代码示例。

---

## 1. JWT 安全修复（优先级：🔴 最高）

### 1.1 问题分析

**当前实现的严重问题**:
```java
// ❌ 当前代码 - 使用用户密码作为JWT签名密钥
Algorithm.HMAC256(account.getPassword())
```

**风险**:
1. 用户修改密码后，旧token依然有效
2. 密码泄露等同于JWT密钥泄露
3. 无法统一吊销用户token
4. 违反安全最佳实践

### 1.2 解决方案

#### Step 1: 创建 JWT 配置类

```java
package com.example.config;

import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * JWT 配置
 */
@Configuration
public class JwtConfig {
    
    @Value("${jwt.secret:change-this-to-a-secure-random-string-in-production}")
    private String secret;
    
    @Value("${jwt.expiration:86400000}") // 默认 24 小时
    private Long expiration;
    
    @Value("${jwt.refresh-expiration:604800000}") // 默认 7 天
    private Long refreshExpiration;
    
    @Bean
    public Algorithm jwtAlgorithm() {
        return Algorithm.HMAC256(secret);
    }
    
    public Long getExpiration() {
        return expiration;
    }
    
    public Long getRefreshExpiration() {
        return refreshExpiration;
    }
}
```

#### Step 2: 重构 TokenUtils

```java
package com.example.utils;

import cn.hutool.core.date.DateUtil;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.common.Constants;
import com.example.common.enums.RoleEnum;
import com.example.config.JwtConfig;
import com.example.entity.Account;
import com.example.exception.CustomException;
import com.example.service.AdminService;
import com.example.service.DoctorService;
import com.example.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Date;

/**
 * Token工具类 - 安全版本
 */
@Component
public class TokenUtils {
    private static final Logger log = LoggerFactory.getLogger(TokenUtils.class);
    
    @Autowired
    private JwtConfig jwtConfig;
    
    @Resource
    private AdminService adminService;
    
    @Resource
    private DoctorService doctorService;
    
    @Resource
    private UserService userService;
    
    /**
     * 生成访问令牌
     * 
     * @param account 用户账户
     * @return JWT token
     */
    public String createToken(Account account) {
        Date now = new Date();
        Date expireTime = new Date(now.getTime() + jwtConfig.getExpiration());
        
        return JWT.create()
                .withSubject(account.getId().toString())
                .withClaim("userId", account.getId())
                .withClaim("username", account.getUsername())
                .withClaim("role", account.getRole())
                .withIssuedAt(now)
                .withExpiresAt(expireTime)
                .sign(jwtConfig.jwtAlgorithm());
    }
    
    /**
     * 生成刷新令牌（可选）
     */
    public String createRefreshToken(Account account) {
        Date now = new Date();
        Date expireTime = new Date(now.getTime() + jwtConfig.getRefreshExpiration());
        
        return JWT.create()
                .withSubject(account.getId().toString())
                .withClaim("type", "refresh")
                .withIssuedAt(now)
                .withExpiresAt(expireTime)
                .sign(jwtConfig.jwtAlgorithm());
    }
    
    /**
     * 验证并解析token
     * 
     * @param token JWT token
     * @return 解码后的JWT
     */
    public DecodedJWT verifyToken(String token) {
        try {
            return JWT.require(jwtConfig.jwtAlgorithm())
                    .build()
                    .verify(token);
        } catch (Exception e) {
            log.error("Token验证失败: {}", e.getMessage());
            throw new CustomException("401", "Token无效或已过期");
        }
    }
    
    /**
     * 从token获取用户ID
     */
    public Integer getUserIdFromToken(String token) {
        DecodedJWT jwt = verifyToken(token);
        return jwt.getClaim("userId").asInt();
    }
    
    /**
     * 从token获取角色
     */
    public String getRoleFromToken(String token) {
        DecodedJWT jwt = verifyToken(token);
        return jwt.getClaim("role").asString();
    }
    
    /**
     * 获取当前登录的用户
     */
    public Account getCurrentUser() {
        try {
            HttpServletRequest request = ((ServletRequestAttributes) 
                    RequestContextHolder.getRequestAttributes()).getRequest();
            String token = request.getHeader(Constants.TOKEN);
            
            if (token == null || token.isEmpty()) {
                return null;
            }
            
            DecodedJWT jwt = verifyToken(token);
            Integer userId = jwt.getClaim("userId").asInt();
            String role = jwt.getClaim("role").asString();
            
            if (RoleEnum.ADMIN.name().equals(role)) {
                return adminService.selectById(userId);
            } else if (RoleEnum.DOCTOR.name().equals(role)) {
                return doctorService.selectById(userId);
            } else if (RoleEnum.USER.name().equals(role)) {
                return userService.selectById(userId);
            }
        } catch (Exception e) {
            log.error("获取当前登录用户出错", e);
        }
        return null;
    }
    
    /**
     * 检查token是否即将过期（1小时内）
     */
    public boolean isTokenExpiringSoon(String token) {
        try {
            DecodedJWT jwt = verifyToken(token);
            Date expiresAt = jwt.getExpiresAt();
            long timeLeft = expiresAt.getTime() - System.currentTimeMillis();
            return timeLeft < 3600000; // 1小时
        } catch (Exception e) {
            return true;
        }
    }
}
```

#### Step 3: 重构 JWTInterceptor

```java
package com.example.common.config;

import cn.hutool.core.util.ObjectUtil;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.common.Constants;
import com.example.common.enums.ResultCodeEnum;
import com.example.exception.CustomException;
import com.example.utils.TokenUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * JWT拦截器 - 安全版本
 */
@Component
public class JWTInterceptor implements HandlerInterceptor {
    
    private static final Logger log = LoggerFactory.getLogger(JWTInterceptor.class);
    
    @Resource
    private TokenUtils tokenUtils;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, 
                             Object handler) throws Exception {
        // 1. 从请求头获取token
        String token = request.getHeader(Constants.TOKEN);
        
        // 2. 如果请求头没有，尝试从参数获取
        if (ObjectUtil.isNull(token) || token.isEmpty()) {
            token = request.getParameter(Constants.TOKEN);
        }
        
        // 3. token为空，拒绝访问
        if (ObjectUtil.isNull(token) || token.isEmpty()) {
            log.warn("请求无token: {}", request.getRequestURI());
            throw new CustomException(ResultCodeEnum.TOKEN_INVALID_ERROR);
        }
        
        // 4. 验证token
        try {
            DecodedJWT jwt = tokenUtils.verifyToken(token);
            
            // 5. 将用户信息存入request属性，供后续使用
            request.setAttribute("userId", jwt.getClaim("userId").asInt());
            request.setAttribute("username", jwt.getClaim("username").asString());
            request.setAttribute("role", jwt.getClaim("role").asString());
            
            log.debug("Token验证成功: user={}, role={}", 
                    jwt.getClaim("username").asString(), 
                    jwt.getClaim("role").asString());
            
            return true;
            
        } catch (Exception e) {
            log.error("Token验证失败: {}", e.getMessage());
            throw new CustomException(ResultCodeEnum.TOKEN_CHECK_ERROR);
        }
    }
}
```

#### Step 4: 更新登录方法

```java
// 在各个Service的login方法中更新
@Service
public class UserService extends BaseAccountService {
    
    @Resource
    private TokenUtils tokenUtils;
    
    @Override
    public Account login(Account account) {
        Account dbUser = userMapper.selectByUsername(account.getUsername());
        if (ObjectUtil.isNull(dbUser)) {
            throw new CustomException(ResultCodeEnum.USER_NOT_EXIST_ERROR);
        }
        if (!account.getPassword().equals(dbUser.getPassword())) {
            throw new CustomException(ResultCodeEnum.USER_ACCOUNT_ERROR);
        }
        // ✅ 使用新的token生成方法
        String token = tokenUtils.createToken(dbUser);
        dbUser.setToken(token);
        return dbUser;
    }
}
```

#### Step 5: 配置文件更新

```yaml
# application.yml
jwt:
  # ⚠️ 生产环境必须使用环境变量覆盖
  secret: ${JWT_SECRET:change-this-to-a-secure-random-string-in-production}
  expiration: 86400000  # 24小时（毫秒）
  refresh-expiration: 604800000  # 7天（毫秒）
```

#### Step 6: 环境变量配置

创建 `.env.example` 文件：
```bash
# JWT配置
JWT_SECRET=your-256-bit-secret-key-here-change-in-production

# 数据库配置
DB_HOST=localhost
DB_PORT=3306
DB_NAME=xm_health_check
DB_USERNAME=root
DB_PASSWORD=your-secure-password

# Redis配置
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=

# API密钥
API_KEY=your-api-key-here
```

更新 `.gitignore`：
```
.env
application-local.yml
```

### 1.3 测试方案

创建测试类：
```java
package com.example.utils;

import com.example.config.JwtConfig;
import com.example.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class TokenUtilsTest {
    
    @Autowired
    private TokenUtils tokenUtils;
    
    private User testUser;
    
    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1);
        testUser.setUsername("testuser");
        testUser.setRole("USER");
    }
    
    @Test
    void testCreateToken() {
        String token = tokenUtils.createToken(testUser);
        assertNotNull(token);
        assertTrue(token.split("\\.").length == 3); // JWT格式验证
    }
    
    @Test
    void testVerifyToken() {
        String token = tokenUtils.createToken(testUser);
        assertDoesNotThrow(() -> tokenUtils.verifyToken(token));
    }
    
    @Test
    void testGetUserIdFromToken() {
        String token = tokenUtils.createToken(testUser);
        Integer userId = tokenUtils.getUserIdFromToken(token);
        assertEquals(testUser.getId(), userId);
    }
    
    @Test
    void testGetRoleFromToken() {
        String token = tokenUtils.createToken(testUser);
        String role = tokenUtils.getRoleFromToken(token);
        assertEquals(testUser.getRole(), role);
    }
    
    @Test
    void testInvalidToken() {
        assertThrows(Exception.class, () -> {
            tokenUtils.verifyToken("invalid.token.here");
        });
    }
}
```

---

## 2. 输入验证修复（优先级：🔴 高）

### 2.1 添加 Bean Validation 依赖

```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

### 2.2 创建请求DTO

```java
package com.example.entity.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * 登录请求DTO
 */
@Data
public class LoginRequest {
    
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名长度必须在3-20个字符之间")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字和下划线")
    private String username;
    
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6-20个字符之间")
    private String password;
    
    @NotBlank(message = "角色不能为空")
    @Pattern(regexp = "^(ADMIN|USER|DOCTOR)$", message = "角色必须是ADMIN、USER或DOCTOR")
    private String role;
}

/**
 * 用户注册请求DTO
 */
@Data
public class UserRegisterRequest {
    
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名长度必须在3-20个字符之间")
    private String username;
    
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6-20个字符之间")
    private String password;
    
    @NotBlank(message = "姓名不能为空")
    @Size(max = 50, message = "姓名长度不能超过50个字符")
    private String name;
    
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
    
    @Email(message = "邮箱格式不正确")
    private String email;
}

/**
 * 医疗文献添加请求DTO
 */
@Data
public class LiteratureAddRequest {
    
    @NotBlank(message = "标题不能为空")
    @Size(max = 500, message = "标题长度不能超过500个字符")
    private String title;
    
    @Size(max = 500, message = "作者长度不能超过500个字符")
    private String authors;
    
    @NotBlank(message = "来源不能为空")
    @Size(max = 100, message = "来源长度不能超过100个字符")
    private String source;
    
    @Size(max = 100, message = "分类长度不能超过100个字符")
    private String category;
    
    @Size(max = 5000, message = "摘要长度不能超过5000个字符")
    private String summary;
    
    @URL(message = "链接格式不正确")
    private String link;
}
```

### 2.3 更新 Controller

```java
package com.example.controller;

import com.example.common.Result;
import com.example.entity.dto.LoginRequest;
import com.example.entity.dto.UserRegisterRequest;
import com.example.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Web控制器 - 添加输入验证
 */
@RestController
@Validated
public class WebController {
    
    @Autowired
    private UserService userService;
    
    /**
     * 登录 - 添加输入验证
     */
    @PostMapping("/login")
    public Result login(@Valid @RequestBody LoginRequest request) {
        Account account = new Account();
        account.setUsername(request.getUsername());
        account.setPassword(request.getPassword());
        account.setRole(request.getRole());
        
        // ... 原有登录逻辑
    }
    
    /**
     * 注册 - 添加输入验证
     */
    @PostMapping("/register")
    public Result register(@Valid @RequestBody UserRegisterRequest request) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        user.setName(request.getName());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        
        userService.add(user);
        return Result.success();
    }
}
```

### 2.4 全局验证异常处理

```java
package com.example.exception;

import com.example.common.Result;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.stream.Collectors;

/**
 * 全局异常处理器 - 增强版
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    /**
     * 处理 @Valid 验证异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result handleValidationException(MethodArgumentNotValidException e) {
        String errorMsg = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        
        return Result.error("400", "参数验证失败: " + errorMsg);
    }
    
    /**
     * 处理 @Validated 验证异常
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public Result handleConstraintViolationException(ConstraintViolationException e) {
        String errorMsg = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));
        
        return Result.error("400", "参数验证失败: " + errorMsg);
    }
    
    /**
     * 处理自定义业务异常
     */
    @ExceptionHandler(CustomException.class)
    public Result handleCustomException(CustomException e) {
        return Result.error(e.getCode(), e.getMsg());
    }
    
    /**
     * 处理其他未知异常
     */
    @ExceptionHandler(Exception.class)
    public Result handleException(Exception e) {
        log.error("未知异常:", e);
        return Result.error("500", "系统内部错误");
    }
}
```

---

## 3. 敏感信息保护（优先级：🔴 高）

### 3.1 环境变量配置

**步骤1**: 创建环境特定配置文件

```yaml
# application-dev.yml（开发环境）
spring:
  datasource:
    url: jdbc:mysql://${DB_HOST:localhost}:${DB_PORT:3306}/${DB_NAME:xm_health_check}
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:root}
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD:}

api:
  key: ${API_KEY:your-api-key}

jwt:
  secret: ${JWT_SECRET:dev-secret-key-do-not-use-in-production}
```

```yaml
# application-prod.yml（生产环境）
spring:
  datasource:
    url: jdbc:mysql://${DB_HOST}:${DB_PORT}/${DB_NAME}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
  data:
    redis:
      host: ${REDIS_HOST}
      port: ${REDIS_PORT}
      password: ${REDIS_PASSWORD}

api:
  key: ${API_KEY}

jwt:
  secret: ${JWT_SECRET}

# 生产环境必须显式指定环境变量，无默认值
```

**步骤2**: 更新 `.gitignore`

```
# 环境配置
.env
.env.local
application-local.yml

# 敏感信息
**/application-prod.yml
```

**步骤3**: Docker部署配置

```dockerfile
# Dockerfile
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/springboot-0.0.1-SNAPSHOT.jar app.jar

# 使用环境变量
ENV JWT_SECRET=${JWT_SECRET}
ENV DB_PASSWORD=${DB_PASSWORD}
ENV API_KEY=${API_KEY}

EXPOSE 9090
ENTRYPOINT ["java", "-jar", "app.jar"]
```

```yaml
# docker-compose.yml
version: '3.8'
services:
  backend:
    build: .
    ports:
      - "9090:9090"
    environment:
      - JWT_SECRET=${JWT_SECRET}
      - DB_HOST=mysql
      - DB_PORT=3306
      - DB_NAME=xm_health_check
      - DB_USERNAME=root
      - DB_PASSWORD=${DB_PASSWORD}
      - REDIS_HOST=redis
      - REDIS_PORT=6379
      - API_KEY=${API_KEY}
    depends_on:
      - mysql
      - redis
  
  mysql:
    image: mysql:8.0
    environment:
      - MYSQL_ROOT_PASSWORD=${DB_PASSWORD}
      - MYSQL_DATABASE=xm_health_check
    volumes:
      - mysql_data:/var/lib/mysql
  
  redis:
    image: redis:7-alpine
    volumes:
      - redis_data:/data

volumes:
  mysql_data:
  redis_data:
```

---

## 4. 验证清单

在完成所有安全修复后，使用以下清单验证：

- [ ] JWT不再使用用户密码作为签名密钥
- [ ] 所有敏感配置已迁移到环境变量
- [ ] `.env` 文件已添加到 `.gitignore`
- [ ] 所有 Controller 的输入参数都添加了 `@Valid`
- [ ] 全局异常处理器能正确处理验证异常
- [ ] 生产环境配置文件不包含明文密码
- [ ] JWT token 包含用户ID、角色等必要信息
- [ ] Token 过期时间设置合理（建议24小时）
- [ ] 实现了 token 刷新机制（可选）
- [ ] 所有安全相关的单元测试都通过

---

## 5. 部署建议

### 5.1 生产环境部署前检查

```bash
# 1. 生成强随机密钥
openssl rand -base64 32

# 2. 设置环境变量
export JWT_SECRET="your-generated-secret-key"
export DB_PASSWORD="your-secure-db-password"
export API_KEY="your-api-key"

# 3. 验证配置
java -jar app.jar --spring.profiles.active=prod --spring.config.additional-location=file:./config/
```

### 5.2 安全审计日志

添加安全事件日志：

```java
@Component
@Slf4j
public class SecurityAuditLogger {
    
    public void logLoginSuccess(String username, String ip) {
        log.info("LOGIN_SUCCESS - User: {}, IP: {}", username, ip);
    }
    
    public void logLoginFailure(String username, String ip, String reason) {
        log.warn("LOGIN_FAILURE - User: {}, IP: {}, Reason: {}", username, ip, reason);
    }
    
    public void logTokenValidationFailure(String token, String ip) {
        log.warn("TOKEN_VALIDATION_FAILURE - Token: {}..., IP: {}", 
                token.substring(0, Math.min(10, token.length())), ip);
    }
}
```

---

**下一步**: 完成安全修复后，继续执行[CODE_REVIEW_REPORT.md](CODE_REVIEW_REPORT.md)中的其他优化项。
