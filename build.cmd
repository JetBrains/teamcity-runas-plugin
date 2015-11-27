call msbuild runAs-tool-win32\tools.sln /p:Configuration=Release /p:Platform="x86"
call msbuild runAs-tool-win32\tools.sln /p:Configuration=Release /p:Platform="x64"
REM call msbuild runAs-tool\runAs-tool.sln /p:Configuration=Release
call mvn package
call createPlugin.cmd