[CmdletBinding()]
param(
    [switch]$SkipBuild,
    [switch]$SkipFrontend,
    [switch]$SkipInfrastructureCheck,
    [switch]$Quiet,
    [string]$NatappScriptPath = 'E:\ideacode\javaProjects\run_natapp.bat',
    [ValidateRange(10, 300)]
    [int]$StartupTimeoutSeconds = 120
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$ProjectRoot = Split-Path -Parent $PSScriptRoot
$FrontendRoot = Join-Path (Split-Path -Parent $ProjectRoot) 'xy-club-front'
$RunRoot = Join-Path $ProjectRoot '.run'
$PidFile = Join-Path $RunRoot 'pids.json'
$LogRoot = Join-Path $RunRoot (Get-Date -Format 'yyyyMMdd-HHmmss')
$StartedProcesses = [System.Collections.Generic.List[object]]::new()

# OSS 按当前开发需求不参与一键启动。
$BackendServices = @(
    [pscustomobject]@{
        Name = 'auth'
        DisplayName = '认证服务'
        BuildDirectory = Join-Path $ProjectRoot 'xyclub-auth'
        JarPath = Join-Path $ProjectRoot 'xyclub-auth\xyclub-auth-starter\target\xyclub-auth-starter.jar'
        BuildGoal = 'install'
        Port = 3011
    },
    [pscustomobject]@{
        Name = 'subject'
        DisplayName = '题目服务'
        BuildDirectory = Join-Path $ProjectRoot 'xyclub-subject'
        JarPath = Join-Path $ProjectRoot 'xyclub-subject\xyclub-starter\target\xyclub-starter.jar'
        BuildGoal = 'package'
        Port = 3000
    },
    [pscustomobject]@{
        Name = 'practice'
        DisplayName = '练习服务'
        BuildDirectory = Join-Path $ProjectRoot 'xyclub-practice'
        JarPath = Join-Path $ProjectRoot 'xyclub-practice\xyclub-practice-server\target\xyclub-practice-server.jar'
        BuildGoal = 'package'
        Port = 3013
    },
    [pscustomobject]@{
        Name = 'wx'
        DisplayName = '微信服务'
        BuildDirectory = Join-Path $ProjectRoot 'xyclub-wx'
        JarPath = Join-Path $ProjectRoot 'xyclub-wx\target\xyclub-wx.jar'
        BuildGoal = 'package'
        Port = 3012
    }
)

$GatewayService = [pscustomobject]@{
    Name = 'gateway'
    DisplayName = '网关服务'
    BuildDirectory = Join-Path $ProjectRoot 'xyclub-gateway'
    JarPath = Join-Path $ProjectRoot 'xyclub-gateway\target\xyclub-gateway.jar'
    BuildGoal = 'package'
    Port = 5000
}

function Get-RequiredCommand {
    param([Parameter(Mandatory = $true)][string]$Name)

    $command = Get-Command $Name -ErrorAction SilentlyContinue
    if ($null -eq $command) {
        throw "未找到命令：$Name，请先安装并配置到 PATH。"
    }
    return $command.Source
}

function Test-TcpEndpoint {
    param(
        [Parameter(Mandatory = $true)][string]$HostName,
        [Parameter(Mandatory = $true)][int]$Port,
        [int]$TimeoutMilliseconds = 800
    )

    $client = [System.Net.Sockets.TcpClient]::new()
    try {
        $task = $client.ConnectAsync($HostName, $Port)
        return $task.Wait($TimeoutMilliseconds) -and $client.Connected
    }
    catch {
        return $false
    }
    finally {
        $client.Dispose()
    }
}

function Wait-TcpEndpoint {
    param(
        [Parameter(Mandatory = $true)][string]$DisplayName,
        [Parameter(Mandatory = $true)][int]$Port,
        [Parameter(Mandatory = $true)][int]$TimeoutSeconds
    )

    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
    while ((Get-Date) -lt $deadline) {
        if (Test-TcpEndpoint -HostName '127.0.0.1' -Port $Port -TimeoutMilliseconds 500) {
            Write-Host "[成功] $DisplayName 已监听端口 $Port" -ForegroundColor Green
            return
        }
        Start-Sleep -Seconds 1
    }
    throw "$DisplayName 未在 $TimeoutSeconds 秒内监听端口 $Port，请检查日志目录：$LogRoot"
}

function Save-ProcessState {
    $state = [pscustomobject]@{
        ProjectRoot = $ProjectRoot
        LogDirectory = $LogRoot
        CreatedAt = (Get-Date).ToString('o')
        Processes = @($StartedProcesses)
    }
    $state | ConvertTo-Json -Depth 5 | Set-Content -LiteralPath $PidFile -Encoding UTF8
}

function Stop-ProcessTree {
    param([Parameter(Mandatory = $true)][int]$TargetProcessId)

    $children = @(Get-CimInstance Win32_Process -Filter "ParentProcessId=$TargetProcessId" -ErrorAction SilentlyContinue)
    foreach ($child in $children) {
        Stop-ProcessTree -TargetProcessId ([int]$child.ProcessId)
    }
    Stop-Process -Id $TargetProcessId -Force -ErrorAction SilentlyContinue
}

function Stop-StartedProcesses {
    for ($index = $StartedProcesses.Count - 1; $index -ge 0; $index--) {
        Stop-ProcessTree -TargetProcessId ([int]$StartedProcesses[$index].Id)
    }
}

function Invoke-MavenBuild {
    param([Parameter(Mandatory = $true)]$Service)

    Write-Host "[构建] $($Service.DisplayName)：$($Service.BuildDirectory)" -ForegroundColor Cyan
    Push-Location $Service.BuildDirectory
    try {
        & $script:MavenCommand '-DskipTests' $Service.BuildGoal
        if ($LASTEXITCODE -ne 0) {
            throw "$($Service.DisplayName) 构建失败，Maven 退出码：$LASTEXITCODE"
        }
    }
    finally {
        Pop-Location
    }
}

function Start-ManagedProcess {
    param(
        [Parameter(Mandatory = $true)][string]$Name,
        [Parameter(Mandatory = $true)][string]$DisplayName,
        [Parameter(Mandatory = $true)][string]$FilePath,
        [Parameter(Mandatory = $true)][string[]]$ArgumentList,
        [Parameter(Mandatory = $true)][string]$WorkingDirectory,
        [Parameter(Mandatory = $true)][int]$Port
    )

    $stdoutPath = Join-Path $LogRoot "$Name.out.log"
    $stderrPath = Join-Path $LogRoot "$Name.err.log"
    if ($Quiet) {
        # 静默模式：隐藏窗口并将标准输出、错误输出保存到日志文件。
        $process = Start-Process `
            -FilePath $FilePath `
            -ArgumentList $ArgumentList `
            -WorkingDirectory $WorkingDirectory `
            -RedirectStandardOutput $stdoutPath `
            -RedirectStandardError $stderrPath `
            -WindowStyle Hidden `
            -PassThru
    }
    else {
        # 默认模式：复用当前控制台，直接显示各服务的启动日志和业务日志。
        $process = Start-Process `
            -FilePath $FilePath `
            -ArgumentList $ArgumentList `
            -WorkingDirectory $WorkingDirectory `
            -NoNewWindow `
            -PassThru
    }

    $record = [pscustomobject]@{
        Name = $Name
        DisplayName = $DisplayName
        Id = $process.Id
        StartedAt = $process.StartTime.ToString('o')
        Port = $Port
        StandardOutput = if ($Quiet) { $stdoutPath } else { $null }
        StandardError = if ($Quiet) { $stderrPath } else { $null }
    }
    $StartedProcesses.Add($record)
    Save-ProcessState
    Write-Host "[启动] $DisplayName，PID=$($process.Id)" -ForegroundColor Yellow
}

try {
    New-Item -ItemType Directory -Path $RunRoot -Force | Out-Null

    # 若上次记录中的进程仍存在，则拒绝重复启动，避免端口冲突。
    if (Test-Path -LiteralPath $PidFile) {
        $oldState = Get-Content -Raw -LiteralPath $PidFile | ConvertFrom-Json
        $runningRecords = @($oldState.Processes | Where-Object { Get-Process -Id $_.Id -ErrorAction SilentlyContinue })
        if ($runningRecords.Count -gt 0) {
            throw '检测到上一次启动的进程仍在运行，请先执行 scripts\stop-all.ps1。'
        }
        Remove-Item -LiteralPath $PidFile -Force
    }

    New-Item -ItemType Directory -Path $LogRoot -Force | Out-Null
    $script:JavaCommand = Get-RequiredCommand -Name 'java.exe'
    $script:MavenCommand = Get-RequiredCommand -Name 'mvn.cmd'
    $script:CommandPrompt = Get-RequiredCommand -Name 'cmd.exe'

    if (-not $SkipFrontend) {
        $script:PnpmCommand = Get-RequiredCommand -Name 'pnpm.cmd'
        if (-not (Test-Path -LiteralPath $FrontendRoot)) {
            throw "未找到前端目录：$FrontendRoot"
        }
    }
    if (-not (Test-Path -LiteralPath $NatappScriptPath)) {
        throw "未找到 natapp 启动脚本：$NatappScriptPath"
    }

    $localPorts = @($BackendServices.Port) + @($GatewayService.Port)
    if (-not $SkipFrontend) {
        $localPorts += 5173
    }
    foreach ($port in $localPorts) {
        if (Test-TcpEndpoint -HostName '127.0.0.1' -Port $port -TimeoutMilliseconds 300) {
            throw "本机端口 $port 已被占用，请先关闭对应程序。"
        }
    }

    if (-not $SkipInfrastructureCheck) {
        $dependencies = @(
            [pscustomobject]@{ Name = '本地 MySQL'; HostName = '127.0.0.1'; Port = 3306 },
            [pscustomobject]@{ Name = 'Nacos'; HostName = '182.92.176.188'; Port = 8848 },
            [pscustomobject]@{ Name = 'Redis'; HostName = '182.92.176.188'; Port = 6379 }
        )
        foreach ($dependency in $dependencies) {
            Write-Host "[检查] $($dependency.Name) $($dependency.HostName):$($dependency.Port)"
            if (-not (Test-TcpEndpoint -HostName $dependency.HostName -Port $dependency.Port -TimeoutMilliseconds 1500)) {
                throw "$($dependency.Name) 不可访问；如需跳过检查，可使用 -SkipInfrastructureCheck。"
            }
        }
    }

    # 启动内网穿透脚本；停止脚本会根据 PID 记录一并停止它及其子进程。
    Start-ManagedProcess `
        -Name 'natapp' `
        -DisplayName 'natapp 内网穿透' `
        -FilePath $script:CommandPrompt `
        -ArgumentList @('/d', '/s', '/c', ('call "{0}"' -f $NatappScriptPath)) `
        -WorkingDirectory (Split-Path -Parent $NatappScriptPath) `
        -Port 0

    $allBackendServices = @($BackendServices) + @($GatewayService)
    if (-not $SkipBuild) {
        foreach ($service in $allBackendServices) {
            Invoke-MavenBuild -Service $service
        }
    }

    foreach ($service in $allBackendServices) {
        if (-not (Test-Path -LiteralPath $service.JarPath)) {
            throw "未找到 $($service.DisplayName) 的 JAR：$($service.JarPath)"
        }
    }

    # 先启动业务服务并等待注册完成，再启动网关。
    foreach ($service in $BackendServices) {
        Start-ManagedProcess `
            -Name $service.Name `
            -DisplayName $service.DisplayName `
            -FilePath $script:JavaCommand `
            -ArgumentList @('-jar', ('"{0}"' -f $service.JarPath)) `
            -WorkingDirectory $service.BuildDirectory `
            -Port $service.Port
    }
    foreach ($service in $BackendServices) {
        Wait-TcpEndpoint -DisplayName $service.DisplayName -Port $service.Port -TimeoutSeconds $StartupTimeoutSeconds
    }

    Start-ManagedProcess `
        -Name $GatewayService.Name `
        -DisplayName $GatewayService.DisplayName `
        -FilePath $script:JavaCommand `
        -ArgumentList @('-jar', ('"{0}"' -f $GatewayService.JarPath)) `
        -WorkingDirectory $GatewayService.BuildDirectory `
        -Port $GatewayService.Port
    Wait-TcpEndpoint -DisplayName $GatewayService.DisplayName -Port $GatewayService.Port -TimeoutSeconds $StartupTimeoutSeconds

    if (-not $SkipFrontend) {
        $viteEntry = Join-Path $FrontendRoot 'node_modules\vite\bin\vite.js'
        if (-not (Test-Path -LiteralPath $viteEntry)) {
            Write-Host '[安装] 前端依赖' -ForegroundColor Cyan
            Push-Location $FrontendRoot
            $oldCi = $env:CI
            try {
                # 后台脚本没有交互式终端，使用 CI 模式允许 pnpm 重建不完整的依赖目录。
                $env:CI = 'true'
                & $script:PnpmCommand 'install' '--frozen-lockfile'
                if ($LASTEXITCODE -ne 0) {
                    throw "前端依赖安装失败，pnpm 退出码：$LASTEXITCODE"
                }
            }
            finally {
                if ($null -eq $oldCi) {
                    Remove-Item Env:CI -ErrorAction SilentlyContinue
                }
                else {
                    $env:CI = $oldCi
                }
                Pop-Location
            }
        }

        $frontendCommand = ('"{0}" run dev --host 127.0.0.1 --port 5173 --strictPort' -f $script:PnpmCommand)
        Start-ManagedProcess `
            -Name 'frontend' `
            -DisplayName '前端服务' `
            -FilePath $script:CommandPrompt `
            -ArgumentList @('/d', '/s', '/c', ('"{0}"' -f $frontendCommand)) `
            -WorkingDirectory $FrontendRoot `
            -Port 5173
        Wait-TcpEndpoint -DisplayName '前端服务' -Port 5173 -TimeoutSeconds $StartupTimeoutSeconds
    }

    Write-Host ''
    Write-Host '全部服务启动成功（OSS 未启动）。' -ForegroundColor Green
    Write-Host '前端地址：http://127.0.0.1:5173'
    Write-Host '网关地址：http://127.0.0.1:5000'
    if ($Quiet) {
        Write-Host "日志目录：$LogRoot"
    }
    else {
        Write-Host '服务日志已输出到当前控制台；如需后台运行并写入日志文件，请使用 -Quiet。'
    }
    Write-Host '停止命令：powershell -ExecutionPolicy Bypass -File .\scripts\stop-all.ps1'
}
catch {
    Write-Host "[失败] $($_.Exception.Message)" -ForegroundColor Red
    if ($StartedProcesses.Count -gt 0) {
        Write-Host '正在停止本次已启动的进程……' -ForegroundColor Yellow
        Stop-StartedProcesses
    }
    if (Test-Path -LiteralPath $PidFile) {
        Remove-Item -LiteralPath $PidFile -Force
    }
    exit 1
}
