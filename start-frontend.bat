@echo off
echo ========================================
echo   启动抽奖系统前端服务
echo ========================================
echo.

cd lottery-frontend

echo [1/3] 检查Node.js...
where node >nul 2>&1
if %errorlevel% neq 0 (
    echo [错误] Node.js未安装
    echo 请先安装Node.js: https://nodejs.org/
    echo.
    pause
    exit /b 1
)

echo Node版本:
node --version
echo.

echo [2/3] 检查依赖...
if not exist "node_modules" (
    echo node_modules不存在，开始安装依赖...
    echo 这可能需要2-3分钟，请耐心等待...
    echo.
    call npm install
    if %errorlevel% neq 0 (
        echo [错误] 依赖安装失败
        pause
        exit /b 1
    )
) else (
    echo 依赖已安装，跳过安装步骤
)

echo.
echo [3/3] 启动Vite开发服务器...
echo.
echo 服务将在以下地址启动:
echo   - 主屏幕:     http://localhost:3000/
echo   - 手机控制端: http://localhost:3000/control.html
echo   - 管理后台:   http://localhost:3000/admin.html
echo.
echo 按 Ctrl+C 可停止服务
echo.

npm run dev

pause
