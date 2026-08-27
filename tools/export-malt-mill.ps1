[CmdletBinding()]
param()

$ErrorActionPreference = 'Stop'
$repoRoot = [System.IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
$artRoot = Join-Path $repoRoot 'art/blockbench/malt_mill'
$rawModel = Join-Path $artRoot 'export/malt_mill.blockbench.json'
$gameModel = Join-Path $repoRoot 'minecraft-common/src/main/resources/assets/alcoholic/models/block/malt_mill.json'
$derivedRoot = Join-Path $artRoot 'textures/derived'

if (-not (Test-Path -LiteralPath $rawModel)) {
    throw "Missing Blockbench export: $rawModel"
}

$model = Get-Content -LiteralPath $rawModel -Raw | ConvertFrom-Json
if ($model.elements.Count -ne 67) {
    throw "Expected 67 malt mill elements, found $($model.elements.Count)"
}

$names = @($model.elements | ForEach-Object { $_.name })
if ($names | Where-Object { $_ -match 'crank|handle' }) {
    throw 'The malt mill export still contains a manual crank or handle'
}
foreach ($required in @(
    'oak_hopper_wall_front',
    'roller_front_core',
    'roller_rear_core',
    'iron_axle_shaft',
    'oak_chute_floor',
    'dark_grist_throat'
)) {
    if ($required -notin $names) {
        throw "Missing required malt mill element: $required"
    }
}

$allowedAngles = @(-45.0, -22.5, 0.0, 22.5, 45.0)
foreach ($element in $model.elements) {
    if ($null -ne $element.rotation -and $element.rotation.angle -notin $allowedAngles) {
        throw "Unsupported Java rotation on $($element.name): $($element.rotation.angle)"
    }
    if ($element.name -like 'oak_chute_*' -or $element.name -eq 'dark_grist_throat') {
        if (
            $element.from[0] -lt 0 -or $element.to[0] -gt 16 -or
            $element.from[2] -lt 0 -or $element.to[2] -gt 16
        ) {
            throw "The lower receptacle exceeds the block footprint: $($element.name)"
        }
    }
}

$javaModel = [ordered]@{
    parent = 'minecraft:block/block'
    textures = [ordered]@{
        '0' = 'alcoholic:block/malt_mill'
        particle = 'alcoholic:block/malt_mill'
    }
    elements = $model.elements
    display = $model.display
}

$modelDirectory = Split-Path -Parent $gameModel
New-Item -ItemType Directory -Force -Path $modelDirectory | Out-Null
$json = $javaModel | ConvertTo-Json -Depth 100
$utf8NoBom = New-Object System.Text.UTF8Encoding($false)
[System.IO.File]::WriteAllText($gameModel, $json + [Environment]::NewLine, $utf8NoBom)

$textureTargets = [ordered]@{
    32 = Join-Path $repoRoot 'minecraft-common/src/main/resources/assets/alcoholic/textures/block/malt_mill.png'
    64 = Join-Path $repoRoot 'resourcepacks/Alcoholic-64x/assets/alcoholic/textures/block/malt_mill.png'
    128 = Join-Path $repoRoot 'resourcepacks/Alcoholic-128x/assets/alcoholic/textures/block/malt_mill.png'
    256 = Join-Path $repoRoot 'resourcepacks/Alcoholic-256x/assets/alcoholic/textures/block/malt_mill.png'
    512 = Join-Path $repoRoot 'resourcepacks/Alcoholic-512x/assets/alcoholic/textures/block/malt_mill.png'
}

Add-Type -AssemblyName System.Drawing
foreach ($entry in $textureTargets.GetEnumerator()) {
    $size = [int]$entry.Key
    $source = if ($size -eq 512) {
        Join-Path $artRoot 'textures/master-512/malt_mill.png'
    } else {
        Join-Path $derivedRoot "malt_mill_$size.png"
    }
    if (-not (Test-Path -LiteralPath $source)) {
        throw "Missing $size x $size source atlas: $source"
    }

    $image = [System.Drawing.Image]::FromFile($source)
    try {
        if ($image.Width -ne $size -or $image.Height -ne $size) {
            throw "Expected $size x $size atlas at $source, found $($image.Width) x $($image.Height)"
        }
    } finally {
        $image.Dispose()
    }

    $destination = $entry.Value
    New-Item -ItemType Directory -Force -Path (Split-Path -Parent $destination) | Out-Null
    Copy-Item -LiteralPath $source -Destination $destination -Force
}

$staleGenerated = @(
    (Join-Path $repoRoot 'minecraft-common/src/generated/resources/assets/alcoholic/models/block/malt_mill.json'),
    (Join-Path $repoRoot 'minecraft-common/src/generated/resources/assets/alcoholic/textures/block/malt_mill.png')
)
foreach ($stale in $staleGenerated) {
    if (Test-Path -LiteralPath $stale) {
        Remove-Item -LiteralPath $stale
    }
}

Write-Output 'Exported the Minecraft Java 1.19.2 malt mill model and texture variants.'
