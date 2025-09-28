@echo off
chcp 65001 >nul
echo.
echo ========================================
echo 🏥 健康体检系统启动脚本
echo ========================================
echo.

:: 设置颜色
color 0A

:: 检查Java环境
echo 🔍 检查Java环境...
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Java未安装或未配置到PATH
    echo 请安装Java 17或更高版本
    pause
    exit /b 1
)
echo ✅ Java环境正常

:: 检查Node.js环境
echo.
echo 🔍 检查Node.js环境...
node --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Node.js未安装或未配置到PATH
    echo 请安装Node.js 16或更高版本
    pause
    exit /b 1
)
echo ✅ Node.js环境正常

:: 检查Maven环境
echo.
echo 🔍 检查Maven环境...
mvn --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Maven未安装或未配置到PATH
    echo 请安装Maven 3.6或更高版本
    pause
    exit /b 1
)
echo ✅ Maven环境正常

echo.
echo ========================================
echo 🚀 开始启动系统组件
echo ========================================

:: 启动后端服务
echo.
echo 📦 启动Spring Boot后端服务...
echo 端口: 9090
echo 访问地址: http://localhost:9090
echo.
start "健康体检系统-后端" cmd /k "cd /d %~dp0springboot && mvn spring-boot:run"

:: 等待后端启动
echo ⏳ 等待后端服务启动...
timeout /t 10 /nobreak >nul

:: 启动前端服务
echo.
echo 🎨 启动Vue前端服务...
echo 端口: 5173
echo 访问地址: http://localhost:5173
echo.
start "健康体检系统-前端" cmd /k "cd /d %~dp0vue\vue && npm run dev"

:: 等待前端启动
echo ⏳ 等待前端服务启动...
timeout /t 5 /nobreak >nul

:: 打开系统测试页面
echo.
echo 🧪 打开系统集成测试页面...
start "" "%~dp0system-integration-test.html"

echo.
echo ========================================
echo ✅ 系统启动完成！
echo ========================================
echo.
echo 📋 服务信息:
echo   后端API: http://localhost:9090
echo   前端界面: http://localhost:5173
echo   测试页面: system-integration-test.html
echo.
echo 📚 API文档:
echo   Swagger: http://localhost:9090/swagger-ui.html
echo   健康检查: http://localhost:9090/actuator/health
echo.
echo 🔧 主要功能:
echo   - 用户管理 (注册/登录)
echo   - 体检管理 (预约/报告)
echo   - 医疗文献 (CRUD/爬虫)
echo   - 消息队列 (RabbitMQ)
echo   - 缓存系统 (Redis)
echo   - 微服务架构 (Spring Cloud)
echo.
echo 🕷️ 爬虫测试:
echo   同步爬虫: POST /medical-literature/crawl
echo   异步爬虫: POST /medical-literature/crawl-async
echo   消息测试: POST /medical-literature/test-message
echo.
echo ⚠️ 注意事项:
echo   1. 确保MySQL数据库已启动 (端口3306)
echo   2. 如需RabbitMQ功能，请启动RabbitMQ服务 (端口5672)
echo   3. 如需Redis缓存，请启动Redis服务 (端口6379)
echo   4. 关闭窗口将停止对应服务
echo.
echo 🎯 快速测试命令:
echo   python crawler-test-tool.py --quick
echo.
echo 按任意键退出启动脚本...
pause >nul
