$ErrorActionPreference = 'Stop'
$analysisDir = $PSScriptRoot
$bundlePath = Join-Path $analysisDir 'rea-search-evidence.json'
$evidenceBundle = Get-Content -LiteralPath $bundlePath -Raw | ConvertFrom-Json
$snippetDir = Join-Path $analysisDir 'snippets'
New-Item -ItemType Directory -Path $snippetDir -Force | Out-Null
$evidenceIndex = @()
foreach ($record in $evidenceBundle.records) {
    if ($record.operation -ne 'inspect_android_method') { continue }
    $result = $record.normalized_result
    $safeMethod = $result.method.name -replace '[<>]', '_'
    $filename = $result.class_name + '.' + $safeMethod + '.' + $result.representation + '.txt'
    $header = @(
        '// Static decompiler/disassembly evidence; NOT original source or runtime observation.',
        ('// Evidence ID: ' + $record.evidence_id),
        ('// APK SHA-256: ' + $record.subject.digest.sha256),
        ('// DEX prototype: ' + $result.method.dex_descriptor),
        ('// Representation: ' + $result.representation + '; source status: ' + $result.source.status),
        ('// Fallback: ' + $result.fell_back + '; markers: ' + ($result.markers -join '; ')),
        ''
    ) -join "`n"
    [System.IO.File]::WriteAllText((Join-Path $snippetDir $filename), $header + "`n" + $result.source.text, [System.Text.UTF8Encoding]::new($false))
    $evidenceIndex += [pscustomobject]@{
        evidence_id = $record.evidence_id
        class_name = $result.class_name
        method = $result.method.name
        path = 'snippets/' + $filename
        representation = $result.representation
        status = $result.source.status
    }
}
$evidenceIndex | ConvertTo-Json -Depth 5 | Set-Content -LiteralPath (Join-Path $analysisDir 'evidence-index.json') -Encoding utf8
Write-Output ('Extracted ' + $evidenceIndex.Count + ' static method evidence files.')
