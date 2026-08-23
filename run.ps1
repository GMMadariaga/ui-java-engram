# Ejecutar Engram Studio
# Ejecutar este script después de compilar el proyecto

$env:JAVA_HOME = "C:\Program Files\Microsoft\jdk-25.0.4.101-hotspot"
$env:MAVEN_HOME = "C:\Users\gmadariaga\maven\apache-maven-3.9.16"
$env:PATH = "$env:JAVA_HOME\bin;$env:MAVEN_HOME\bin;$env:PATH"

cd "C:\NUEVOS-proyectos\E-STUDIO\engram-studio"

Write-Host "Starting Engram Studio..."
mvn clean javafx:run
