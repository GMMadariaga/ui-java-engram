[CmdletBinding()]
param(
    [ValidateSet('app-image', 'exe', 'msi')]
    [string]$Type = 'app-image',

    [ValidatePattern('^\d+(\.\d+){1,3}$')]
    [string]$Version = '1.0.1',

    [switch]$SkipTests
)

$ErrorActionPreference = 'Stop'

if ($env:OS -ne 'Windows_NT') {
    throw 'Este script solo puede ejecutarse en Windows.'
}

$projectRoot = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$targetDir = Join-Path $projectRoot 'target'
$inputDir = Join-Path $targetDir 'jpackage-input'
$outputDir = Join-Path $projectRoot 'dist'
$mainJarName = 'engram-studio.jar'
$configPath = Join-Path $env:USERPROFILE '.engram-studio\engram-studio.properties'

function Find-CommandPath([string]$name) {
    $command = Get-Command $name -ErrorAction SilentlyContinue
    if ($command) {
        return $command.Source
    }
    return $null
}

$maven = Find-CommandPath 'mvn.cmd'
if (-not $maven -and $env:MAVEN_HOME) {
    $candidate = Join-Path $env:MAVEN_HOME 'bin\mvn.cmd'
    if (Test-Path $candidate) {
        $maven = $candidate
    }
}
if (-not $maven) {
    throw 'No se encontró Maven. Instala Maven 3.9.x o define MAVEN_HOME.'
}

$jpackage = Find-CommandPath 'jpackage.exe'
if (-not $jpackage -and $env:JAVA_HOME) {
    $candidate = Join-Path $env:JAVA_HOME 'bin\jpackage.exe'
    if (Test-Path $candidate) {
        $jpackage = $candidate
    }
}
if (-not $jpackage) {
    throw 'No se encontró jpackage. Usa un JDK 25 (no un JRE) y define JAVA_HOME.'
}

if ($Type -ne 'app-image') {
    $candle = Find-CommandPath 'candle.exe'
    $light = Find-CommandPath 'light.exe'
    if (-not $candle -or -not $light) {
        throw 'Para generar EXE/MSI se requiere WiX Toolset con candle.exe y light.exe disponibles en PATH. Use -Type app-image para generar la versión portable sin WiX.'
    }
}

Push-Location $projectRoot
try {
    Write-Host 'Compilando Engram Studio...' -ForegroundColor Cyan
    $mavenArgs = @('clean', 'package')
    if ($SkipTests) {
        $mavenArgs += '-DskipTests'
    }
    & $maven @mavenArgs
    if ($LASTEXITCODE -ne 0) {
        throw "Maven terminó con código $LASTEXITCODE."
    }

    if (Test-Path $inputDir) {
        Remove-Item -LiteralPath $inputDir -Recurse -Force
    }
    New-Item -ItemType Directory -Path $inputDir -Force | Out-Null

    Write-Host 'Preparando dependencias runtime...' -ForegroundColor Cyan
    & $maven 'org.apache.maven.plugins:maven-dependency-plugin:3.8.1:copy-dependencies' `
        "-DincludeScope=runtime" `
        "-DoutputDirectory=$inputDir"
    if ($LASTEXITCODE -ne 0) {
        throw "No se pudieron copiar las dependencias runtime (código $LASTEXITCODE)."
    }

    $builtJar = Get-ChildItem -LiteralPath $targetDir -Filter 'engram-studio-*.jar' |
        Where-Object { $_.Name -notmatch '(sources|javadoc)' } |
        Select-Object -First 1
    if (-not $builtJar) {
        throw 'No se encontró el JAR compilado en target.'
    }
    Copy-Item -LiteralPath $builtJar.FullName -Destination (Join-Path $inputDir $mainJarName)

    if (Test-Path $outputDir) {
        Remove-Item -LiteralPath $outputDir -Recurse -Force
    }
    New-Item -ItemType Directory -Path $outputDir -Force | Out-Null

    $jpackageArgs = @(
        '--type', $Type,
        '--name', 'Engram Studio',
        '--app-version', $Version,
        '--vendor', 'Speed Solutions',
        '--description', 'Desktop application for Engram memory administration and visualization',
        '--input', $inputDir,
        '--dest', $outputDir,
        '--main-jar', $mainJarName,
        '--main-class', 'com.speed.engramstudio.Main',
        '--java-options', '--enable-native-access=javafx.graphics,javafx.web,javafx.media,ALL-UNNAMED',
        '--java-options', '--add-exports=javafx.web/com.sun.javafx.webkit=ALL-UNNAMED'
    )

    if ($Type -ne 'app-image') {
        # Always create a desktop shortcut and a Start menu entry for an installable package.
        $jpackageArgs += @(
            '--win-shortcut',
            '--win-menu',
            '--win-menu-group', 'Engram Studio',
            '--win-dir-chooser',
            '--win-per-user-install',
            '--win-upgrade-uuid', '9a7cbf0e-75e0-4e7b-8c7d-1c82df0c5b47'
        )
    }

    $icon = Join-Path $projectRoot 'src\main\resources\icon.ico'
    if (Test-Path $icon) {
        $jpackageArgs += @('--icon', $icon)
    } else {
        Write-Warning 'No se encontró src/main/resources/icon.ico; se usará el icono predeterminado del instalador.'
    }

    Write-Host "Generando $Type..." -ForegroundColor Cyan
    & $jpackage @jpackageArgs
    if ($LASTEXITCODE -ne 0) {
        throw "jpackage terminó con código $LASTEXITCODE."
    }

    Write-Host ''
    Write-Host "Paquete generado en: $outputDir" -ForegroundColor Green
    Write-Host "Configuración conservada en: $configPath" -ForegroundColor Green
    Write-Host 'El instalador no copia, modifica ni elimina ese archivo.' -ForegroundColor Green
} finally {
    Pop-Location
}
