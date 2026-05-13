@echo off
setlocal enabledelayedexpansion
chcp 65001 >nul

:: 获取脚本所在目录的完整路径
set "script_dir=%~dp0"

:: 启动 UAV_MC_System
:: set "exe_path=!script_dir!deception_backend\UAV_MC_System.exe"
set "uav_dir=!script_dir!deception_backend\"
set "uav_exec=!uav_dir!UAV_MC_System.exe"
echo 正在启动 !exe_path!
start "UAV_MC_System" /D "!uav_dir!" cmd /k "!uav_exec!"
timeout /t 10 /nobreak

:: 启动 DeviceManager
set "exe_path=!script_dir!DeviceData\DeviceManager0329.exe"
echo 正在启动 !exe_path!
start "" "!exe_path!"
timeout /t 100 /nobreak

:: 启动 CoreServer
set "exe_path=!script_dir!coresever\coresever0329.exe"
echo 正在启动 !exe_path!
start "" "!exe_path!"
timeout /t 60 /nobreak

:: 启动 MAPS
:: set "exe_path=!script_dir!\large-screen-visualization\MAPS低空防御系统.exe"
:: echo 正在启动 !exe_path!
:: start "" "!exe_path!"
:: timeout /t 10 /nobreak

:: 启动 RD07
set "exe_path=!script_dir!RDXS-XKCPU_VB3.02.13_D3\RDXS-XKCPU_VB3.02.13_D3\RDXS_XKCPU_VB3.02.13.exe"
echo 正在启动 !exe_path!
start "" "!exe_path!"
timeout /t 5 /nobreak

:: 启动UavControlSystem
set "exe_path=!script_dir!UavControlSystem_v1.7\无人机管控系统.exe"
echo 正在启动 !exe_path!
start "" "!exe_path!"
timeout /t 5 /nobreak

echo.
echo 所有程序已启动。
endlocal
exit