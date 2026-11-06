@echo off
setlocal
cd /d "%~dp0"

set "JAVAFX_HOME=%~dp0javafx-sdk-25.0.5"

if not exist "%JAVAFX_HOME%\bin\prism_sw.dll" (
    echo Khong tim thay JavaFX SDK tai "%JAVAFX_HOME%".
    echo Giai nen JavaFX SDK 25.0.5 Windows x64 vao thu muc nay roi chay lai.
    exit /b 1
)

if not exist "%cd%\bin" mkdir "%cd%\bin"

javac -encoding UTF-8 -d "%cd%\bin" --module-path "%JAVAFX_HOME%\lib" --add-modules javafx.controls -sourcepath "%cd%" "%cd%\src\Main\Main.java"
if errorlevel 1 (
    echo Build that bai.
    exit /b 1
)

java --enable-native-access=javafx.graphics --module-path "%JAVAFX_HOME%\lib" --add-modules javafx.controls -Djava.library.path="%JAVAFX_HOME%\bin" -cp "%cd%\bin;%cd%" src.Main.Main
