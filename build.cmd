msbuild runAs-tool-win32\build.proj
REM call msbuild runAs-tool\runAs-tool.sln /p:Configuration=Release

call mvn package

call createPlugin.cmd