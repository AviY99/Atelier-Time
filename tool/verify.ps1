# Run pure-Dart Step 1.4 checks locally on Windows without GitHub Actions.
# From the repository root: powershell -ExecutionPolicy Bypass -File tool\verify.ps1
$ErrorActionPreference = 'Stop'
Set-Location (Resolve-Path (Join-Path $PSScriptRoot '..'))
$logFile = Join-Path (Get-Location).Path 'atelier-time-verification.log'
Set-Content -Path $logFile -Value 'Atelier Time - local Dart verification'

if (-not (Get-Command dart -ErrorAction SilentlyContinue)) {
    $message = 'Dart SDK is not on PATH. Install https://dart.dev/get-dart and reopen PowerShell.'
    Write-Host $message
    Add-Content -Path $logFile -Value $message
    exit 2
}

function Invoke-DartStep {
    param([string[]]$StepArguments)
    $heading = '>>> dart ' + ($StepArguments -join ' ')
    Write-Host $heading
    Add-Content -Path $logFile -Value $heading

    # The PowerShell process prints the output; the outer caller can paste it
    # back into the chat. The log retains diagnostic output for failed checks.
    # Dart may print --version and diagnostics to stderr. On Windows
    # PowerShell 5.1, redirecting stderr under Stop can otherwise trigger
    # NativeCommandError despite a successful native process exit.
    $priorErrorPreference = $ErrorActionPreference
    $ErrorActionPreference = 'Continue'
    try {
        $output = & dart @StepArguments 2>&1
        $commandExitCode = $LASTEXITCODE
    } finally {
        $ErrorActionPreference = $priorErrorPreference
    }
    $output | ForEach-Object {
        $line = "$_"
        Write-Host $line
        Add-Content -Path $logFile -Value $line
    }
    if ($commandExitCode -ne 0) {
        $message = "Verification failed: $heading (exit code $commandExitCode)"
        Write-Host $message
        Add-Content -Path $logFile -Value $message
        exit $commandExitCode
    }
}

Invoke-DartStep @('--version')
Invoke-DartStep @('tool/smoke.dart')
Invoke-DartStep @('pub', 'get')
Invoke-DartStep @('analyze')
Invoke-DartStep @('test')
Write-Host 'All locally executed Dart checks passed.'
Add-Content -Path $logFile -Value 'All locally executed Dart checks passed.'
Write-Host "Log saved to $logFile"
