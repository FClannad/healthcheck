# 测试策略与实施计划

本文档提供了 HealthCheck 项目从零测试覆盖到 70% 覆盖率的详细测试策略。

---

## 1. 测试现状分析

**当前情况**:
- ✅ 项目包含 `spring-boot-starter-test` 依赖
- ❌ 无任何测试类（0%覆盖率）
- ❌ 前端无测试框架
- ❌ 无集成测试
- ❌ 无E2E测试

**目标**:
- 🎯 30天内达到 40% 单元测试覆盖率
- 🎯 90天内达到 70% 单元测试覆盖率
- 🎯 关键业务流程 100% 集成测试覆盖

---

## 2. 后端测试框架配置

### 2.1 Maven 依赖

```xml
<!-- pom.xml -->
<dependencies>
    <!-- Spring Boot Test -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
        <exclusions>
            <exclusion>
                <groupId>org.junit.vintage</groupId>
                <artifactId>junit-vintage-engine</artifactId>
            </exclusion>
        </exclusions>
    </dependency>
    
    <!-- H2 内存数据库（测试用） -->
    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <scope>test</scope>
    </dependency>
    
    <!-- Mockito -->
    <dependency>
        <groupId>org.mockito</groupId>
        <artifactId>mockito-core</artifactId>
        <scope>test</scope>
    </dependency>
    
    <!-- AssertJ（可选，更流畅的断言） -->
    <dependency>
        <groupId>org.assertj</groupId>
        <artifactId>assertj-core</artifactId>
        <scope>test</scope>
    </dependency>
    
    <!-- Testcontainers（可选，用于集成测试） -->
    <dependency>
        <groupId>org.testcontainers</groupId>
        <artifactId>testcontainers</artifactId>
        <version>1.19.3</version>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.testcontainers</groupId>
        <artifactId>mysql</artifactId>
        <version>1.19.3</version>
        <scope>test</scope>
    </dependency>
    
    <!-- JaCoCo（代码覆盖率） -->
    <dependency>
        <groupId>org.jacoco</groupId>
        <artifactId>jacoco-maven-plugin</artifactId>
        <version>0.8.11</version>
    </dependency>
</dependencies>

<build>
    <plugins>
        <!-- JaCoCo Plugin -->
        <plugin>
            <groupId>org.jacoco</groupId>
            <artifactId>jacoco-maven-plugin</artifactId>
            <version>0.8.11</version>
            <executions>
                <execution>
                    <goals>
                        <goal>prepare-agent</goal>
                    </goals>
                </execution>
                <execution>
                    <id>report</id>
                    <phase>test</phase>
                    <goals>
                        <goal>report</goal>
                    </goals>
                </execution>
                <execution>
                    <id>jacoco-check</id>
                    <goals>
                        <goal>check</goal>
                    </goals>
                    <configuration>
                        <rules>
                            <rule>
                                <element>PACKAGE</element>
                                <limits>
                                    <limit>
                                        <counter>LINE</counter>
                                        <value>COVEREDRATIO</value>
                                        <minimum>0.40</minimum>
                                    </limit>
                                </limits>
                            </rule>
                        </rules>
                    </configuration>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

### 2.2 测试配置文件

创建 `src/test/resources/application-test.yml`:

```yaml
spring:
  datasource:
    driver-class-name: org.h2.Driver
    url: jdbc:h2:mem:testdb;MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
    username: sa
    password: 
  
  jpa:
    show-sql: true
    hibernate:
      ddl-auto: create-drop
  
  data:
    redis:
      host: localhost
      port: 6379
  
  cache:
    type: none  # 测试时禁用缓存

mybatis:
  configuration:
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
    map-underscore-to-camel-case: true

# JWT测试配置
jwt:
  secret: test-secret-key-for-unit-tests-only
  expiration: 86400000

# 爬虫测试配置
crawler:
  enabled: false  # 测试时禁用爬虫

logging:
  level:
    com.example: DEBUG
```

---

## 3. 单元测试实施

### 3.1 Utils 层测试（优先级最高）

#### TokenUtils 测试

```java
package com.example.utils;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.config.JwtConfig;
import com.example.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("TokenUtils 单元测试")
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
        testUser.setName("Test User");
    }
    
    @Test
    @DisplayName("应该成功创建token")
    void shouldCreateTokenSuccessfully() {
        // When
        String token = tokenUtils.createToken(testUser);
        
        // Then
        assertNotNull(token);
        String[] parts = token.split("\\.");
        assertEquals(3, parts.length, "JWT应包含3部分（header.payload.signature）");
    }
    
    @Test
    @DisplayName("应该从token中正确提取用户ID")
    void shouldExtractUserIdFromToken() {
        // Given
        String token = tokenUtils.createToken(testUser);
        
        // When
        Integer userId = tokenUtils.getUserIdFromToken(token);
        
        // Then
        assertEquals(testUser.getId(), userId);
    }
    
    @Test
    @DisplayName("应该从token中正确提取角色")
    void shouldExtractRoleFromToken() {
        // Given
        String token = tokenUtils.createToken(testUser);
        
        // When
        String role = tokenUtils.getRoleFromToken(token);
        
        // Then
        assertEquals(testUser.getRole(), role);
    }
    
    @Test
    @DisplayName("应该成功验证有效的token")
    void shouldVerifyValidToken() {
        // Given
        String token = tokenUtils.createToken(testUser);
        
        // When & Then
        assertDoesNotThrow(() -> {
            DecodedJWT jwt = tokenUtils.verifyToken(token);
            assertNotNull(jwt);
        });
    }
    
    @Test
    @DisplayName("应该拒绝无效的token")
    void shouldRejectInvalidToken() {
        // Given
        String invalidToken = "invalid.token.here";
        
        // When & Then
        assertThrows(Exception.class, () -> {
            tokenUtils.verifyToken(invalidToken);
        });
    }
    
    @Test
    @DisplayName("应该拒绝空token")
    void shouldRejectEmptyToken() {
        assertThrows(Exception.class, () -> {
            tokenUtils.verifyToken("");
        });
    }
    
    @Test
    @DisplayName("不同用户应该生成不同的token")
    void shouldGenerateDifferentTokensForDifferentUsers() {
        // Given
        User user1 = new User();
        user1.setId(1);
        user1.setUsername("user1");
        user1.setRole("USER");
        
        User user2 = new User();
        user2.setId(2);
        user2.setUsername("user2");
        user2.setRole("ADMIN");
        
        // When
        String token1 = tokenUtils.createToken(user1);
        String token2 = tokenUtils.createToken(user2);
        
        // Then
        assertNotEquals(token1, token2);
    }
}
```

#### SimilarityUtil 测试

```java
package com.example.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.*;

@DisplayName("SimilarityUtil 单元测试")
class SimilarityUtilTest {
    
    private final SimilarityUtil similarityUtil = new SimilarityUtil();
    
    @Test
    @DisplayName("相同字符串的相似度应该为1.0")
    void shouldReturnOneForIdenticalStrings() {
        double similarity = similarityUtil.calculateSimilarity("hello", "hello");
        assertThat(similarity).isEqualTo(1.0);
    }
    
    @Test
    @DisplayName("完全不同的字符串相似度应该为0.0")
    void shouldReturnZeroForCompletelyDifferentStrings() {
        double similarity = similarityUtil.calculateSimilarity("abc", "xyz");
        assertThat(similarity).isCloseTo(0.0, within(0.1));
    }
    
    @ParameterizedTest
    @CsvSource({
        "'hello', 'hallo', 0.8",
        "'kitten', 'sitting', 0.6",
        "'medical', 'medicle', 0.9"
    })
    @DisplayName("应该正确计算相似字符串的相似度")
    void shouldCalculateSimilarityCorrectly(String s1, String s2, double expected) {
        double similarity = similarityUtil.calculateSimilarity(s1, s2);
        assertThat(similarity).isCloseTo(expected, within(0.2));
    }
    
    @Test
    @DisplayName("空字符串应该返回0相似度")
    void shouldHandleEmptyStrings() {
        double similarity = similarityUtil.calculateSimilarity("", "hello");
        assertThat(similarity).isEqualTo(0.0);
    }
    
    @Test
    @DisplayName("null参数应该抛出异常或返回0")
    void shouldHandleNullParameters() {
        assertThatThrownBy(() -> {
            similarityUtil.calculateSimilarity(null, "hello");
        }).isInstanceOf(NullPointerException.class);
    }
}
```

### 3.2 Service 层测试

#### MedicalLiteratureService 测试

```java
package com.example.service;

import com.example.entity.MedicalLiterature;
import com.example.mapper.MedicalLiteratureMapper;
import com.example.utils.SimilarityUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MedicalLiteratureService 单元测试")
class MedicalLiteratureServiceTest {
    
    @Mock
    private MedicalLiteratureMapper mapper;
    
    @Mock
    private SimilarityUtil similarityUtil;
    
    @InjectMocks
    private MedicalLiteratureService service;
    
    private MedicalLiterature testLiterature;
    
    @BeforeEach
    void setUp() {
        testLiterature = new MedicalLiterature();
        testLiterature.setId(1);
        testLiterature.setTitle("Test Medical Paper");
        testLiterature.setAuthors("John Doe");
        testLiterature.setSource("arxiv");
        testLiterature.setStatus("active");
    }
    
    @Test
    @DisplayName("应该成功添加单个文献")
    void shouldAddSingleLiterature() {
        // Given
        doNothing().when(mapper).insert(any(MedicalLiterature.class));
        
        // When
        service.add(testLiterature);
        
        // Then
        verify(mapper, times(1)).insert(testLiterature);
        assertThat(testLiterature.getStatus()).isEqualTo("active");
        assertThat(testLiterature.getCreateTime()).isNotNull();
    }
    
    @Test
    @DisplayName("批量添加应该正确处理重复文献")
    void shouldHandleDuplicatesInBatchAdd() {
        // Given
        MedicalLiterature lit1 = createLiterature("Paper 1");
        MedicalLiterature lit2 = createLiterature("Paper 2");
        MedicalLiterature lit3 = createLiterature("Paper 1"); // 重复
        
        List<MedicalLiterature> literatures = Arrays.asList(lit1, lit2, lit3);
        
        when(mapper.selectByTitle(anyString())).thenReturn(null);
        doNothing().when(mapper).insert(any(MedicalLiterature.class));
        
        // When
        int savedCount = service.batchAdd(literatures);
        
        // Then
        verify(mapper, times(2)).insert(any(MedicalLiterature.class));
        assertThat(savedCount).isEqualTo(2);
    }
    
    @Test
    @DisplayName("批量添加空列表应该返回0")
    void shouldReturnZeroForEmptyList() {
        int savedCount = service.batchAdd(Arrays.asList());
        assertThat(savedCount).isEqualTo(0);
    }
    
    @Test
    @DisplayName("批量添加null列表应该返回0")
    void shouldReturnZeroForNullList() {
        int savedCount = service.batchAdd(null);
        assertThat(savedCount).isEqualTo(0);
    }
    
    @Test
    @DisplayName("应该跳过无标题的文献")
    void shouldSkipLiteratureWithoutTitle() {
        // Given
        MedicalLiterature lit1 = createLiterature("Valid Paper");
        MedicalLiterature lit2 = new MedicalLiterature();
        lit2.setTitle(null); // 无标题
        
        List<MedicalLiterature> literatures = Arrays.asList(lit1, lit2);
        
        when(mapper.selectByTitle(anyString())).thenReturn(null);
        doNothing().when(mapper).insert(any(MedicalLiterature.class));
        
        // When
        int savedCount = service.batchAdd(literatures);
        
        // Then
        verify(mapper, times(1)).insert(any(MedicalLiterature.class));
        assertThat(savedCount).isEqualTo(1);
    }
    
    private MedicalLiterature createLiterature(String title) {
        MedicalLiterature lit = new MedicalLiterature();
        lit.setTitle(title);
        lit.setAuthors("Author");
        lit.setSource("test");
        lit.setStatus("active");
        return lit;
    }
}
```

#### UserService 测试

```java
package com.example.service;

import com.example.entity.Account;
import com.example.entity.User;
import com.example.exception.CustomException;
import com.example.mapper.UserMapper;
import com.example.utils.TokenUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService 单元测试")
class UserServiceTest {
    
    @Mock
    private UserMapper userMapper;
    
    @Mock
    private TokenUtils tokenUtils;
    
    @InjectMocks
    private UserService userService;
    
    private User testUser;
    
    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1);
        testUser.setUsername("testuser");
        testUser.setPassword("password123");
        testUser.setRole("USER");
    }
    
    @Test
    @DisplayName("用户名和密码正确应该登录成功")
    void shouldLoginSuccessfully() {
        // Given
        Account account = new Account();
        account.setUsername("testuser");
        account.setPassword("password123");
        account.setRole("USER");
        
        when(userMapper.selectByUsername("testuser")).thenReturn(testUser);
        when(tokenUtils.createToken(any(User.class))).thenReturn("mock-token");
        
        // When
        Account result = userService.login(account);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getToken()).isEqualTo("mock-token");
        verify(tokenUtils, times(1)).createToken(testUser);
    }
    
    @Test
    @DisplayName("用户名不存在应该抛出异常")
    void shouldThrowExceptionWhenUserNotFound() {
        // Given
        Account account = new Account();
        account.setUsername("nonexistent");
        account.setPassword("password123");
        
        when(userMapper.selectByUsername("nonexistent")).thenReturn(null);
        
        // When & Then
        assertThatThrownBy(() -> userService.login(account))
            .isInstanceOf(CustomException.class)
            .hasMessageContaining("不存在");
    }
    
    @Test
    @DisplayName("密码错误应该抛出异常")
    void shouldThrowExceptionWhenPasswordIncorrect() {
        // Given
        Account account = new Account();
        account.setUsername("testuser");
        account.setPassword("wrongpassword");
        
        when(userMapper.selectByUsername("testuser")).thenReturn(testUser);
        
        // When & Then
        assertThatThrownBy(() -> userService.login(account))
            .isInstanceOf(CustomException.class);
    }
    
    @Test
    @DisplayName("应该成功添加用户")
    void shouldAddUserSuccessfully() {
        // Given
        User newUser = new User();
        newUser.setUsername("newuser");
        newUser.setPassword("password");
        
        when(userMapper.selectByUsername("newuser")).thenReturn(null);
        doNothing().when(userMapper).insert(any(User.class));
        
        // When
        userService.add(newUser);
        
        // Then
        verify(userMapper, times(1)).insert(newUser);
        assertThat(newUser.getRole()).isEqualTo("USER");
    }
    
    @Test
    @DisplayName("重复用户名应该抛出异常")
    void shouldThrowExceptionWhenUsernameExists() {
        // Given
        User newUser = new User();
        newUser.setUsername("testuser");
        
        when(userMapper.selectByUsername("testuser")).thenReturn(testUser);
        
        // When & Then
        assertThatThrownBy(() -> userService.add(newUser))
            .isInstanceOf(CustomException.class)
            .hasMessageContaining("已存在");
    }
}
```

### 3.3 Controller 层集成测试

```java
package com.example.controller;

import com.example.SpringbootApplication;
import com.example.entity.User;
import com.example.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = SpringbootApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("WebController 集成测试")
class WebControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private UserService userService;
    
    @Test
    @DisplayName("GET / 应该返回成功")
    void shouldReturnSuccessForRootPath() throws Exception {
        mockMvc.perform(get("/"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("200"));
    }
    
    @Test
    @DisplayName("POST /login 应该返回token")
    void shouldReturnTokenOnLogin() throws Exception {
        // Given
        User mockUser = new User();
        mockUser.setId(1);
        mockUser.setUsername("testuser");
        mockUser.setToken("mock-token");
        
        when(userService.login(any())).thenReturn(mockUser);
        
        String loginJson = """
            {
                "username": "testuser",
                "password": "password123",
                "role": "USER"
            }
            """;
        
        // When & Then
        mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("200"))
            .andExpect(jsonPath("$.data.token").value("mock-token"));
    }
    
    @Test
    @DisplayName("POST /register 应该成功注册")
    void shouldRegisterSuccessfully() throws Exception {
        String registerJson = """
            {
                "username": "newuser",
                "password": "password123",
                "name": "New User",
                "phone": "13800138000",
                "email": "newuser@example.com"
            }
            """;
        
        mockMvc.perform(post("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerJson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("200"));
    }
}
```

---

## 4. 爬虫模块测试

```java
package com.example.crawler.core;

import com.example.crawler.core.model.CrawlRequest;
import com.example.crawler.core.model.CrawlResult;
import com.example.crawler.pipeline.Deduplicator;
import com.example.crawler.pipeline.Normalizer;
import com.example.crawler.sources.SourceClient;
import com.example.entity.MedicalLiterature;
import com.example.service.MedicalLiteratureService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Orchestrator 单元测试")
class OrchestratorTest {
    
    @Mock
    private CrawlerProperties crawlerProperties;
    
    @Mock
    private SourceClient mockSource;
    
    @Mock
    private Normalizer normalizer;
    
    @Mock
    private Deduplicator deduplicator;
    
    @Mock
    private MedicalLiteratureService literatureService;
    
    @InjectMocks
    private Orchestrator orchestrator;
    
    private CrawlRequest request;
    private List<MedicalLiterature> mockPapers;
    
    @BeforeEach
    void setUp() {
        request = new CrawlRequest();
        request.setKeyword("covid");
        request.setMaxResults(10);
        
        mockPapers = Arrays.asList(
            createPaper("Paper 1"),
            createPaper("Paper 2")
        );
        
        when(crawlerProperties.isEnabled()).thenReturn(true);
        when(crawlerProperties.getSources()).thenReturn(Arrays.asList("mock"));
    }
    
    @Test
    @DisplayName("爬虫禁用时应该返回空结果")
    void shouldReturnEmptyWhenDisabled() {
        // Given
        when(crawlerProperties.isEnabled()).thenReturn(false);
        
        // When
        CrawlResult result = orchestrator.crawl(request);
        
        // Then
        assertThat(result.getTotalFound()).isEqualTo(0);
        assertThat(result.getSaved()).isEqualTo(0);
    }
    
    @Test
    @DisplayName("应该成功爬取并保存文献")
    void shouldCrawlAndSaveSuccessfully() {
        // Given
        when(mockSource.fetch(anyString(), anyInt())).thenReturn(mockPapers);
        when(deduplicator.deduplicate(anyList())).thenReturn(mockPapers);
        doNothing().when(literatureService).add(any(MedicalLiterature.class));
        
        // When
        CrawlResult result = orchestrator.crawl(request);
        
        // Then
        assertThat(result.getTotalFound()).isEqualTo(2);
        assertThat(result.getSaved()).isEqualTo(2);
        verify(normalizer, times(1)).normalize(anyList());
        verify(deduplicator, times(1)).deduplicate(anyList());
    }
    
    private MedicalLiterature createPaper(String title) {
        MedicalLiterature paper = new MedicalLiterature();
        paper.setTitle(title);
        paper.setSource("mock");
        return paper;
    }
}
```

---

## 5. 前端测试框架配置

### 5.1 依赖安装

```bash
cd vue/vue
npm install --save-dev vitest @vue/test-utils happy-dom @vitest/ui
```

### 5.2 配置文件

```javascript
// vite.config.js - 添加测试配置
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  test: {
    globals: true,
    environment: 'happy-dom',
    coverage: {
      provider: 'v8',
      reporter: ['text', 'json', 'html'],
      exclude: [
        'node_modules/',
        'src/main.js',
      ]
    }
  }
})
```

```json
// package.json - 添加测试脚本
{
  "scripts": {
    "dev": "vite",
    "build": "vite build",
    "preview": "vite preview",
    "test": "vitest",
    "test:ui": "vitest --ui",
    "test:coverage": "vitest --coverage"
  }
}
```

### 5.3 前端单元测试示例

```javascript
// src/utils/request.test.js
import { describe, it, expect, vi, beforeEach } from 'vitest'
import axios from 'axios'
import request from './request'

vi.mock('axios')

describe('Request Utils', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })
  
  it('should add token to request headers', async () => {
    const mockUser = { token: 'test-token' }
    localStorage.setItem('xm-user', JSON.stringify(mockUser))
    
    const config = await request.interceptors.request.handlers[0].fulfilled({
      headers: {}
    })
    
    expect(config.headers.token).toBe('test-token')
  })
  
  it('should handle 401 errors', async () => {
    const response = {
      data: { code: '401', msg: 'Unauthorized' }
    }
    
    const result = await request.interceptors.response.handlers[0].fulfilled(response)
    
    expect(result.code).toBe('401')
  })
})
```

```javascript
// src/components/Login.test.js
import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import Login from '@/views/Login.vue'
import ElementPlus from 'element-plus'

describe('Login Component', () => {
  it('should render login form', () => {
    const wrapper = mount(Login, {
      global: {
        plugins: [ElementPlus]
      }
    })
    
    expect(wrapper.find('form').exists()).toBe(true)
    expect(wrapper.find('input[type="text"]').exists()).toBe(true)
    expect(wrapper.find('input[type="password"]').exists()).toBe(true)
  })
  
  it('should validate empty username', async () => {
    const wrapper = mount(Login, {
      global: {
        plugins: [ElementPlus]
      }
    })
    
    const submitBtn = wrapper.find('button[type="submit"]')
    await submitBtn.trigger('click')
    
    // 验证错误提示
    expect(wrapper.text()).toContain('用户名不能为空')
  })
})
```

---

## 6. 测试执行计划

### Week 1-2: 基础测试（目标20%覆盖率）
- [ ] TokenUtils - 10个测试用例
- [ ] SimilarityUtil - 8个测试用例
- [ ] RedisUtils - 6个测试用例
- [ ] UserService - 12个测试用例
- [ ] AdminService - 10个测试用例
- [ ] DoctorService - 10个测试用例

### Week 3-4: 核心业务测试（目标40%覆盖率）
- [ ] MedicalLiteratureService - 15个测试用例
- [ ] AiConsultationService - 12个测试用例
- [ ] ExaminationOrderService - 15个测试用例
- [ ] CrawlerOrchestrator - 10个测试用例
- [ ] WebController集成测试 - 20个测试用例

### Week 5-8: 全面覆盖（目标70%覆盖率）
- [ ] 所有Service层测试补全
- [ ] 所有Controller集成测试
- [ ] Mapper层测试（使用H2数据库）
- [ ] 前端组件测试
- [ ] E2E测试（使用Playwright）

---

## 7. CI/CD 集成

### 7.1 GitHub Actions 配置

创建 `.github/workflows/test.yml`:

```yaml
name: Tests

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main, develop ]

jobs:
  backend-test:
    runs-on: ubuntu-latest
    
    services:
      mysql:
        image: mysql:8.0
        env:
          MYSQL_ROOT_PASSWORD: test
          MYSQL_DATABASE: xm_health_check_test
        ports:
          - 3306:3306
        options: >-
          --health-cmd="mysqladmin ping"
          --health-interval=10s
          --health-timeout=5s
          --health-retries=3
      
      redis:
        image: redis:7-alpine
        ports:
          - 6379:6379
    
    steps:
      - uses: actions/checkout@v3
      
      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
          cache: maven
      
      - name: Run tests
        run: |
          cd springboot
          mvn clean test
      
      - name: Generate coverage report
        run: |
          cd springboot
          mvn jacoco:report
      
      - name: Upload coverage to Codecov
        uses: codecov/codecov-action@v3
        with:
          file: ./springboot/target/site/jacoco/jacoco.xml
      
      - name: Check coverage threshold
        run: |
          cd springboot
          mvn jacoco:check
  
  frontend-test:
    runs-on: ubuntu-latest
    
    steps:
      - uses: actions/checkout@v3
      
      - name: Setup Node.js
        uses: actions/setup-node@v3
        with:
          node-version: '18'
          cache: 'npm'
          cache-dependency-path: vue/vue/package-lock.json
      
      - name: Install dependencies
        run: |
          cd vue/vue
          npm ci
      
      - name: Run tests
        run: |
          cd vue/vue
          npm run test:coverage
      
      - name: Upload coverage
        uses: codecov/codecov-action@v3
        with:
          file: ./vue/vue/coverage/coverage-final.json
```

---

## 8. 测试最佳实践

### 8.1 命名规范

```java
// ✅ 好的测试方法命名
@Test
@DisplayName("用户名为空时应该抛出异常")
void shouldThrowExceptionWhenUsernameIsEmpty() { }

// ❌ 不好的命名
@Test
void test1() { }
```

### 8.2 AAA 模式

```java
@Test
void exampleTest() {
    // Arrange（准备）
    User user = new User();
    user.setUsername("test");
    
    // Act（执行）
    String result = service.process(user);
    
    // Assert（断言）
    assertEquals("expected", result);
}
```

### 8.3 使用参数化测试

```java
@ParameterizedTest
@CsvSource({
    "admin, true",
    "user, false",
    "doctor, false"
})
void shouldCheckAdminRole(String role, boolean expected) {
    boolean result = roleChecker.isAdmin(role);
    assertEquals(expected, result);
}
```

---

## 9. 测试报告示例

运行测试后生成的报告：

```bash
# 后端测试
cd springboot
mvn clean test
open target/site/jacoco/index.html

# 前端测试
cd vue/vue
npm run test:coverage
open coverage/index.html
```

---

## 10. 验收标准

测试实施完成的验收标准：

- [ ] 整体测试覆盖率 ≥ 40%（30天目标）
- [ ] 核心Service层覆盖率 ≥ 60%
- [ ] Utils层覆盖率 ≥ 80%
- [ ] 所有测试通过CI/CD流水线
- [ ] 无测试代码覆盖率下降的PR被合并
- [ ] 关键业务流程有集成测试
- [ ] 测试文档完整

---

**下一步**: 完成测试实施后，继续执行性能优化和代码重构。
