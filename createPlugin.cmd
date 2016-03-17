SET root=%~dp0
SET zip=%root%tools\7z.exe

del %root%runAs.zip /F /Q
rd %root%plugin /s /q

%zip% x %root%target\runAs.zip -o%root%plugin

md %root%plugin\agent\new\bin
copy %root%runAs-tool-win32\cmd\*.cmd %root%plugin\agent\new\bin /Y
md %root%plugin\agent\new\bin\x86
copy %root%runAs-tool-win32\bin\Release\x86\JetBrains.runAs.exe %root%plugin\agent\new\bin\x86 /Y
md %root%plugin\agent\new\bin\x64
copy %root%runAs-tool-win32\bin\Release\x64\JetBrains.runAs.exe %root%plugin\agent\new\bin\x64 /Y

pushd plugin\agent\new
%zip% a -tzip %root%plugin\agent\runAs-agent.zip *
popd

rd %root%plugin\agent\new /s /q

pushd plugin
%zip% a -tzip %root%runAs.zip *
popd

rd plugin /s /q