@echo off
:: Author : Lekireddy Sai Bharath Simha Reddy

set STARTIME=%TIME%
set "OLD_PATH=%PATH%"

set "REMOVE_PATHS=C:\Users\lekirsa\Downloads\jdk\jdk1.8\bin;C:\Users\lekirsa\Downloads\jdk\jdk1.8.0_202\bin;C:\Users\lekirsa\Downloads\jdk\jdk17\bin;C:\Users\lekirsa\Downloads\jdk\jdk21\bin"

setlocal enabledelayedexpansion

set "NEWPATH="
echo Removing existing java versions. Please wait ....


for %%i in ("%OLD_PATH:;=" "%") do (
	set "EXCLUDE=0"
	for %%j in (%REMOVE_PATHS%) do (
		
		echo %%~i  | find /i "%%j">nul && set "EXCLUDE=1"
	)
	if !EXCLUDE!==0 (
		set "NEWPATH=!NEWPATH!;%%~i"
	)
)

set PATH=%NEWPATH:~1%
::echo updated PATH : %PATH%
endlocal

echo Select Java version :
echo 1. Java 8 corretto
echo 2. Java 8 java hotspot 202
echo 3. Java 17 corretto
echo 4. Java 21 corretto
set /p choice= "Enter choice: "
if %choice%==1 (

set JAVA_HOME=C:\Users\lekirsa\Downloads\jdk\jdk1.8
) else if %choice%==2 (set JAVA_HOME=C:\Users\lekirsa\Downloads\jdk\jdk1.8.0_202
) else if %choice%==3 (set JAVA_HOME=C:\Users\lekirsa\Downloads\jdk\jdk17
) else if %choice%==4 (set JAVA_HOME=C:\Users\lekirsa\Downloads\jdk\jdk21
) else ( echo "Enter correct choice");
 
set PATH=%JAVA_HOME%\bin;%PATH%
set ENDTIME=%TIME%

echo Starttime : %STARTIME%
echo EndTime : %ENDTIME%
::echo %PATH%
::echo %JAVA_HOME%
::echo Java Version 
java -version