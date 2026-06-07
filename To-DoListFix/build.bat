@echo off
setlocal

:: ----------------------
:: CONFIGURASI
:: ----------------------
set "APP_NAME=ToDoListApp"
set "MAIN_JAR=To-DoListFix-1.0-SNAPSHOT.jar"
set "MAIN_CLASS=com.rplbo.ukdw.todolistfix.ToDoListApplication"
set "JAVAFX_SDK=C:\Program Files\javafx-sdk-17.0.6"

:: ----------------------
:: LANGKAH 1: Build dengan Maven
:: ----------------------
echo [1/3] Building JAR dengan Maven...
call .\mvnw.cmd clean package

if errorlevel 1 (
    echo GAGAL build Maven. Pastikan tidak ada error di source code.
    exit /b 1
)

:: ----------------------
:: LANGKAH 2: Buat folder output
:: ----------------------
set "OUTPUT_DIR=installer"
if exist %OUTPUT_DIR% rmdir /s /q %OUTPUT_DIR%
mkdir %OUTPUT_DIR%

:: ----------------------
:: LANGKAH 3: Jalankan jpackage
:: ----------------------
echo [2/3] Membuat EXE installer dengan jpackage...

jpackage ^
  --type exe ^
  --name %APP_NAME% ^
  --input target ^
  --dest %OUTPUT_DIR% ^
  --main-jar %MAIN_JAR% ^
  --main-class %MAIN_CLASS% ^
  --module-path "%JAVAFX_SDK%\lib" ^
  --add-modules javafx.controls,javafx.fxml ^
  --icon %ICON_FILE%

if errorlevel 1 (
    echo GAGAL membuat .exe dengan jpackage. Pastikan path JavaFX benar.
    exit /b 1
)

echo [3/3] Build selesai! File installer ada di folder: %OUTPUT_DIR%

endlocal
pause
