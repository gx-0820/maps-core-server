@echo off
setlocal enabledelayedexpansion

:: 获取脚本所在目录的完整路径
set "script_dir=%~dp0"

:: 定义要停止的.exe文件的相对路径列表，应与启动脚本中的列表一致
set "exe_list=deception_backend\UAV_MC_System.exe;DeviceData\DeviceManager0329.exe;coresever\coresever0329.exe;large-screen-visualization\MAPS低空防御系统.exe;RDXS-XKCPU_VB3.02.13_D3\RDXS-XKCPU_VB3.02.13_D3\RDXS_XKCPU_VB3.02.13.exe;UavControlSystem_v1.7\无人机管控系统.exe"

echo 正在尝试停止以下程序...
echo.

:: 按顺序停止每个.exe文件
for %%i in (%exe_list%) do (
    set "exe_name=%%~nxi"
    echo [+] 正在停止 !exe_name!
    taskkill /im "!exe_name!" /f >nul 2>&1
    if !errorlevel! equ 0 (
        echo √ 成功停止 !exe_name!
    ) else (
        echo × 未找到或无法停止 !exe_name!
    )
)

echo.
echo 所有程序已尝试停止。
pause
endlocal
exit /b