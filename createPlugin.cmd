SET root=%~dp0
SET zip=%root%tools\7z.exe

set Version=1.0.61
call %root%tools\nuget.exe install JetBrains.runAs -Version %Version% -o win32
copy %root%win32\JetBrains.runAs.%Version%\tools\x64\JetBrains.runAs.exe win32\x64 /Y
copy %root%win32\JetBrains.runAs.%Version%\tools\x86\JetBrains.runAs.exe win32\x86 /Y

del %root%runAs.zip /F /Q
rd %root%plugin /s /q

%zip% x %root%target\runAs.zip -o%root%plugin

md %root%plugin\agent\new\bin

copy %root%cmd\*.cmd %root%plugin\agent\new\bin /Y
%root%tools\unix2dos.exe %root%plugin\agent\new\bin\runAs.cmd

copy %root%cmd\*.sh %root%plugin\agent\new\bin /Y
%root%tools\dos2unix.exe %root%plugin\agent\new\bin\runAs.sh

md %root%plugin\agent\new\bin\x86
copy %root%win32\x64\JetBrains.runAs.exe %root%plugin\agent\new\bin\x86 /Y
md %root%plugin\agent\new\bin\x64
copy %root%win32\x64\JetBrains.runAs.exe %root%plugin\agent\new\bin\x64 /Y

pushd plugin\agent\new
%zip% a -tzip %root%plugin\agent\runAs-agent.zip *
popd

rd %root%plugin\agent\new /s /q

pushd plugin
%zip% a -tzip %root%runAs.zip *
popd

rd plugin /s /q