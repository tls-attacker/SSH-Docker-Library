# INFO: This container is Windows based and requires a Windows host to build and run
# You may also need to switch your Docker daemon Linux to Windows containers
# ltsc2019 and ltsc2022 will brick the build (container won't start under Windows 11)
FROM mcr.microsoft.com/windows/servercore:ltsc2016-amd64

ARG VERSION
ARG USERNAME=sshattacker
ARG PASSWORD=secret

SHELL [ "powershell", "-Command", "$ErrorActionPreference = 'Stop'; $ProgressPreference = 'SilentlyContinue';" ]
RUN New-Item -Path 'C:\' -Name 'docker' -ItemType 'directory'; \
    $versionIdentifier = $env:VERSION -replace '\.',''; \
    Invoke-WebRequest """https://dl.bitvise.com/BvSshServer-$versionIdentifier.exe""" -OutFile 'C:\docker\BvSshServer-Inst.exe'

SHELL [ "cmd", "/S", "/C" ]
RUN C:\docker\BvSshServer-Inst.exe -defaultInstance -acceptEula & exit 0

SHELL [ "powershell", "-Command", "$ErrorActionPreference = 'Stop'; $ProgressPreference = 'SilentlyContinue';" ]
COPY authorized_keys C:\\docker
RUN $versionMajor = ($env:VERSION -split '\.')[0]; \
    $comObjectId = Get-ChildItem HKLM:\Software\Classes -ErrorAction SilentlyContinue | \
                   ? { $_.PSChildName -match '^(BssCfg|Bitvise)[0-9]*\.\w+$' -and (Get-ItemProperty """$($_.PSPath)\CLSID""" -ErrorAction SilentlyContinue)} | \
                   Select-Object -ExpandProperty PSChildName -First 1; \
    Write-Host """Manipulating Bitvise settings via COM object with id: $comObjectId"""; \
    $cfg = New-Object -ComObject $comObjectId; \
    $cfg.settings.Lock(); \
    try { \
        if ($versionMajor -ge 9) { \
            $cfg.settings.Load(); \
            $cfg.settings.logging.eventLogLevel = 2; \
        } \
        else { \
            $result = $cfg.settings.Load(); \
            if ($result.failure) { Write-Error $result.Describe(); } \
            $cfg.settings.server.logging.eventLogLevel = 2; \
        } \
        $user = $cfg.settings.access.virtAccounts.new; \
        $user.virtAccount = $env:USERNAME; \
        $user.group = 'Virtual Users'; \
        $user.virtPassword.Set($env:PASSWORD); \
        $user.auth.keys.ImportFromFile('C:\docker\authorized_keys'); \
        $cfg.settings.access.virtAccounts.NewCommit(); \
        $cfg.settings.Save(); \
    } finally { $cfg.settings.Unlock(); }

LABEL ssh.implementation.name="bitvise" \
      ssh.implementation.version="${VERSION}" \
      ssh.implementation.type="server"
CMD [ "powershell", "-Command", \
        "Start-Service 'BvSshServer'; \
        $idx = (Get-EventLog -LogName 'Application' -Newest 1).Index; \
        while ($true) { \
            Start-Sleep -Seconds 1; \
            $idx2 = (Get-EventLog -LogName 'Application' -Newest 1).Index; \
            Get-EventLog -LogName 'Application' -Newest ($idx2 - $idx) | Select-Object -ExpandProperty Message; \
            $idx = $idx2; \
        }" ]
EXPOSE 22
