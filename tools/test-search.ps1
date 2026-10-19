$ErrorActionPreference = 'Stop'
Push-Location (Join-Path $PSScriptRoot '..')
try {
    New-Item -ItemType Directory -Force '.local/tests' | Out-Null
    javac -d '.local/tests' 'app/src/main/java/com/deploydulupulangnanti/cinelayer/MediaItem.java' 'app/src/main/java/com/deploydulupulangnanti/cinelayer/SearchEngine.java' 'tests/SearchEngineTest.java'
    if ($LASTEXITCODE -ne 0) { throw 'Search test compilation failed' }
    java -cp '.local/tests' SearchEngineTest
    if ($LASTEXITCODE -ne 0) { throw 'Search contract failed' }
} finally { Pop-Location }
