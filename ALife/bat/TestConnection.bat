@echo off
setlocal enabledelayedexpansion
title 设备连通性测试
color 0a

echo.
echo 设备连通性测试（逐个检测）
echo ==========================
echo.

set "dev[2]=雷达设备,192.168.1.3"
set "dev[3]=光电设备,200.99.99.31"
set "dev[4]=电侦设备,192.168.0.8"
set "dev[5]=导航诱骗设备,192.168.101.101"
set dev_count=5

for /L %%i in (1,1,%dev_count%) do (
     for /f "tokens=1,2 delims=," %%a in ("!dev[%%i]!") do (
          echo.
          echo 正在检测：%%a（%%b）
          ping %%b -n 4 -w 1000 >nul
          if !errorlevel! equ 0 (
               echo 状态：[已连通] %%a（%%b）
          ) else (
               echo 状态：[未连通] %%a（%%b）
          )
     )
)

echo.
echo ==========================
echo 所有设备检测完成
pause