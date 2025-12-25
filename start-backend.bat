@echo off
echo ========================================
echo   启动抽奖系统后端服务
echo ========================================
echo.

cd lottery-backend

echo [1/2] 检查Maven...
where mvn >nul 2>&1
if %errorlevel% neq 0 (
    echo [错误] Maven未安装或未添加到PATH
    echo 请先安装Maven: https://maven.apache.org/download.cgi
    echo.
    pause
    exit /b 1
)

echo [2/2] 启动Spring Boot应用...
echo.
echo 服务将在 http://localhost:8080 启动
echo 按 Ctrl+C 可停止服务
echo.

mvn spring-boot:run

pause
