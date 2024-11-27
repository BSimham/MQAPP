@echo off
:: Author : Lekireddy Sai Bharath Simha Reddy
set STARTIME=%TIME%
set "OLD_PATH=%PATH%"

set "REMOVE_PATHS=C:\Users\lekirsa\Downloads\gradle\gradle-8.7\bin;C:\Users\lekirsa\Downloads\gradle\gradle-2.7\bin;C:\Users\lekirsa\Downloads\gradle\gradle-6.3\bin;C:\Users\lekirsa\Downloads\gradle\gradle-7\bin"

set "NEWPATH="
echo Removing existing gradle versions. Please wait ....
setlocal enabledelayedexpansion

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

echo Select Gradle version :
echo 1. Gradle 2.7
echo 2. Gradle 6.3
echo 3. Gradle 7.1.1
echo 4. Gradle 8.7
set /p choice= "Enter choice: "
if %choice%==1 (
set GRADLE_HOME=C:\Users\lekirsa\Downloads\gradle\gradle-2.7\bin
) else if %choice%==2 (set GRADLE_HOME=C:\Users\lekirsa\Downloads\gradle\gradle-6.3\bin
) else if %choice%==3 (set GRADLE_HOME=C:\Users\lekirsa\Downloads\gradle\gradle-7\bin
) else if %choice%==4 (set GRADLE_HOME=C:\Users\lekirsa\Downloads\gradle\gradle-8.7\bin
) else ( echo "Enter correct choice");
 
set PATH=%GRADLE_HOME%;%PATH%
set ENDTIME=%TIME%

echo Starttime : %STARTIME%
echo EndTime : %ENDTIME%
::echo %PATH%
::echo %GRADLE_HOME%
::echo GRADLE Version 
GRADLE -version