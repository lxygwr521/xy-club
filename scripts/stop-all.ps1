[CmdletBinding()]
param()

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$ProjectRoot = Split-Path -Parent $PSScriptRoot
$RunRoot = Join-Path $ProjectRoot '.run'
$PidFile = Join-Path $RunRoot 'pids.json'

function Stop-ProcessTree {
    param([Parameter(Mandatory = $true)][int]$TargetProcessId)

    $children = @(Get-CimInstance Win32_Process -Filter "ParentProcessId=$TargetProcessId" -ErrorAction SilentlyContinue)
    foreach ($child in $children) {
        Stop-ProcessTree -TargetProcessId ([int]$child.ProcessId)
    }
    Stop-Process -Id $TargetProcessId -Force -ErrorAction SilentlyContinue
}

if (-not (Test-Path -LiteralPath $PidFile)) {
    Write-Host '没有找到运行记录，无需停止。' -ForegroundColor Yellow
    exit 0
}

try {
    $state = Get-Content -Raw -LiteralPath $PidFile | ConvertFrom-Json
    $processes = @($state.Processes)

    for ($index = $processes.Count - 1; $index -ge 0; $index--) {
        $record = $processes[$index]
        $process = Get-Process -Id $record.Id -ErrorAction SilentlyContinue
        if ($null -eq $process) {
            Write-Host "[跳过] $($record.DisplayName) 已停止"
            continue
        }

        # 同时校验 PID 与启动时间，防止 PID 被系统复用后误停其他进程。
        $recordedStartTime = [DateTime]::Parse($record.StartedAt)
        if ([Math]::Abs(($process.StartTime - $recordedStartTime).TotalSeconds) -gt 2) {
            Write-Warning "$($record.DisplayName) 的 PID 已被其他进程复用，已跳过：$($record.Id)"
            continue
        }

        Stop-ProcessTree -TargetProcessId ([int]$record.Id)
        Write-Host "[停止] $($record.DisplayName)，PID=$($record.Id)" -ForegroundColor Green
    }

    Remove-Item -LiteralPath $PidFile -Force
    Write-Host '全部已记录服务均已处理，历史日志已保留。' -ForegroundColor Green
}
catch {
    Write-Host "停止服务失败：$($_.Exception.Message)" -ForegroundColor Red
    exit 1
}
