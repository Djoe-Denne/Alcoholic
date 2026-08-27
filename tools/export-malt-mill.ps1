[CmdletBinding()]
param()

$ErrorActionPreference = 'Stop'
$repoRoot = [System.IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
$artRoot = Join-Path $repoRoot 'art/blockbench/malt_mill'
$rawModel = Join-Path $artRoot 'export/malt_mill.blockbench.json'
$finalModel = Join-Path $artRoot 'export/malt_mill.json'
$gameModelRoot = Join-Path $repoRoot 'minecraft-common/src/main/resources/assets/alcoholic/models/block'
$gameModels = [ordered]@{
    static = Join-Path $gameModelRoot 'malt_mill.json'
    rollerFront = Join-Path $gameModelRoot 'malt_mill_roller_front.json'
    rollerRear = Join-Path $gameModelRoot 'malt_mill_roller_rear.json'
    driveAxle = Join-Path $gameModelRoot 'malt_mill_drive_axle.json'
    complete = Join-Path $gameModelRoot 'malt_mill_complete.json'
}
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

$rollerFrontNames = @(
    'roller_front_core',
    'roller_front_octagon',
    'roller_front_band_left',
    'roller_front_band_mid',
    'roller_front_band_right'
)
$rollerRearNames = @(
    'roller_rear_core',
    'roller_rear_octagon',
    'roller_rear_band_left',
    'roller_rear_band_mid',
    'roller_rear_band_right'
)
$driveAxleNames = @(
    'iron_axle_shaft',
    'iron_axle_shaft_octagon'
)
$movingNames = @($rollerFrontNames + $rollerRearNames + $driveAxleNames)

$staticElements = @($model.elements | Where-Object { $_.name -notin $movingNames })
$rollerFrontElements = @($model.elements | Where-Object { $_.name -in $rollerFrontNames })
$rollerRearElements = @($model.elements | Where-Object { $_.name -in $rollerRearNames })
$driveAxleElements = @($model.elements | Where-Object { $_.name -in $driveAxleNames })

if ($staticElements.Count -ne 55) {
    throw "Expected 55 static malt mill elements, found $($staticElements.Count)"
}
if ($rollerFrontElements.Count -ne 5) {
    throw "Expected 5 front roller elements, found $($rollerFrontElements.Count)"
}
if ($rollerRearElements.Count -ne 5) {
    throw "Expected 5 rear roller elements, found $($rollerRearElements.Count)"
}
if ($driveAxleElements.Count -ne 2) {
    throw "Expected 2 drive axle elements, found $($driveAxleElements.Count)"
}

function New-JavaModel {
    param(
        [Parameter(Mandatory = $true)]
        [object[]]$Elements,
        [switch]$IncludeDisplay
    )

    $result = [ordered]@{
        parent = 'minecraft:block/block'
        textures = [ordered]@{
            '0' = 'alcoholic:block/malt_mill'
            particle = 'alcoholic:block/malt_mill'
        }
        elements = $Elements
    }
    if ($IncludeDisplay) {
        $result.display = $model.display
    }
    return $result
}

$utf8NoBom = New-Object System.Text.UTF8Encoding($false)
function Write-JsonModel {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Path,
        [Parameter(Mandatory = $true)]
        [object]$Value
    )

    New-Item -ItemType Directory -Force -Path (Split-Path -Parent $Path) | Out-Null
    $json = $Value | ConvertTo-Json -Depth 100
    [System.IO.File]::WriteAllText($Path, $json + [Environment]::NewLine, $utf8NoBom)
}

$completeModel = New-JavaModel -Elements @($model.elements) -IncludeDisplay
Write-JsonModel -Path $finalModel -Value $completeModel
Write-JsonModel -Path $gameModels.complete -Value $completeModel
Write-JsonModel -Path $gameModels.static -Value (New-JavaModel -Elements $staticElements)
Write-JsonModel -Path $gameModels.rollerFront -Value (New-JavaModel -Elements $rollerFrontElements)
Write-JsonModel -Path $gameModels.rollerRear -Value (New-JavaModel -Elements $rollerRearElements)
Write-JsonModel -Path $gameModels.driveAxle -Value (New-JavaModel -Elements $driveAxleElements)

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
