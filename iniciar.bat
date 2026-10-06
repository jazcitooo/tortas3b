@echo off
set "JAVA_HOME=C:\Program Files\JetBrains\IntelliJ IDEA 2026.2.3\jbr"
set "PATH=%JAVA_HOME%\bin;C:\Program Files\JetBrains\IntelliJ IDEA 2026.2.3\plugins\maven-plugin\lib\maven3\bin;%PATH%"

echo ========================================================
echo Iniciando Tortas y Chocolates 3B en http://localhost:8080
echo ========================================================
mvn spring-boot:run
