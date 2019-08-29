@echo off
echo path:%~dp0
::取得bat文件所在的当前目录
set base=%~dp0

::run app
java -jar %base%\\simple-gateway-1.0-SNAPSHOT.jar %base%\\config.properties

@pause
