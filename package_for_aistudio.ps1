<#
.SYNOPSIS
    Mengemas proyek FiksFaham menjadi arsip ZIP bersih siap compile & test di AI Studio.
.DESCRIPTION
    Menyertakan source code, Gradle scripts, Firebase rules, google-services.json (untuk AI Studio),
    dan dokumentasi sinkronisasi; mengecualikan cache build (.gradle, build, app/build),
    skrip sisa (app/replace_quiz.py), file lokal (.env, local.properties), dan output ZIP sebelumnya.
#>
param(
    [string]$OutputDir = "."
)

$ErrorActionPreference = "Stop"
Add-Type -AssemblyName System.IO.Compression
Add-Type -AssemblyName System.IO.Compression.FileSystem

$rootPath = (Get-Location).Path
$outFolder = if ([string]::IsNullOrWhiteSpace($OutputDir) -or $OutputDir -eq ".") {
    $rootPath
} else {
    Join-Path $rootPath $OutputDir
}
if (-not (Test-Path $outFolder)) {
    New-Item -ItemType Directory -Path $outFolder | Out-Null
}

$timestamp = Get-Date -Format "yyyyMMdd_HHmm"
$zipFileName = "FiksFaham_AIStudio_$timestamp.zip"
$zipPath = Join-Path $outFolder $zipFileName

# Pastikan selalu hanya tersisa 1 ZIP terbaru di folder output
Get-ChildItem -Path $outFolder -Filter "FiksFaham_AIStudio_*.zip" -File -ErrorAction SilentlyContinue |
    Remove-Item -Force

$excludedDirNames = @(
    ".git", ".github", ".aistudio", ".gradle", ".kotlin", ".idea", "build", "dist", "docs", "public", "web-portal", "node_modules"
)

$excludedRelativeDirs = @(
    "app/build",
    "docs",
    "public",
    "web-portal"
)

$excludedRelativeFiles = @(
    "app/replace_quiz.py",
    ".env",
    "local.properties"
)

$excludedExtensions = @(
    ".apk", ".aab", ".jks", ".keystore", ".zip", ".log"
)

function Should-Exclude([string]$relPath) {
    $norm = ($relPath -replace "\\", "/").TrimStart("/")
    $segments = $norm.Split("/")

    if ($segments.Length -gt 1) {
        foreach ($seg in $segments[0..($segments.Length - 2)]) {
            if ($excludedDirNames -contains $seg) {
                return $true
            }
        }
    }

    foreach ($dirPrefix in $excludedRelativeDirs) {
        if ($norm.StartsWith("$dirPrefix/", [System.StringComparison]::OrdinalIgnoreCase) -or
            $norm.Equals($dirPrefix, [System.StringComparison]::OrdinalIgnoreCase)) {
            return $true
        }
    }

    if ($excludedRelativeFiles -contains $norm) {
        return $true
    }

    $ext = [System.IO.Path]::GetExtension($norm).ToLowerInvariant()
    if ($excludedExtensions -contains $ext) {
        return $true
    }

    return $false
}

$allFiles = Get-ChildItem -Path $rootPath -Recurse -File -Force
$includedCount = 0

$zipArchive = [System.IO.Compression.ZipFile]::Open($zipPath, [System.IO.Compression.ZipArchiveMode]::Create)
try {
    foreach ($file in $allFiles) {
        $rel = $file.FullName.Substring($rootPath.Length).TrimStart("\", "/")
        if (Should-Exclude $rel) {
            continue
        }
        $entryName = $rel -replace "\\", "/"
        [System.IO.Compression.ZipFileExtensions]::CreateEntryFromFile(
            $zipArchive,
            $file.FullName,
            $entryName,
            [System.IO.Compression.CompressionLevel]::Optimal
        ) | Out-Null
        $includedCount++
    }
}
finally {
    $zipArchive.Dispose()
}

$zipInfo = Get-Item $zipPath
$sizeMb = [Math]::Round($zipInfo.Length / 1MB, 2)

Write-Host "ZIP AI Studio berhasil dibuat:"
Write-Host "  Path : $zipPath"
Write-Host "  File : $includedCount file disertakan"
Write-Host "  Size : $sizeMb MB"
