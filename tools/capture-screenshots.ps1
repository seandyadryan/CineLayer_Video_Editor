param([string]$Adb = 'adb', [string]$Device = 'emulator-5554')
$ErrorActionPreference = 'Stop'
Push-Location (Join-Path $PSScriptRoot '..')
try {
    $captures = @(
        @{ Name = 'phone'; Size = '1080x1920'; Density = '360' },
        @{ Name = 'desktop'; Size = '1920x1080'; Density = '160' },
        @{ Name = 'tablet-7'; Size = '1920x1080'; Density = '240' },
        @{ Name = 'tablet-10'; Size = '1920x1080'; Density = '200' }
    )
    foreach ($capture in $captures) {
        & $Adb -s $Device shell wm size $capture.Size
        & $Adb -s $Device shell wm density $capture.Density
        & $Adb -s $Device shell input keyevent 224
        & $Adb -s $Device shell wm dismiss-keyguard
        & $Adb -s $Device shell am instrument -w -e capture $capture.Name 'com.deploydulupulangnanti.cinelayer.test/com.deploydulupulangnanti.cinelayer.SmokeInstrumentation'
        if ($LASTEXITCODE -ne 0) { throw "Capture failed for $($capture.Name)" }
        $destination = Join-Path 'store/screenshots' $capture.Name
        New-Item -ItemType Directory -Force $destination | Out-Null
        foreach ($screen in @('01-library', '02-search', '03-trim', '04-favorites')) {
            & $Adb -s $Device pull "/sdcard/Android/data/com.deploydulupulangnanti.cinelayer/files/screenshots/$($capture.Name)/$screen.png" (Join-Path $destination "$screen.png")
            if ($LASTEXITCODE -ne 0) { throw "Screenshot pull failed for $screen" }
        }
    }
} finally { Pop-Location }
