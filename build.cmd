call msbuild runAs-tool-win32\tools.sln /p:Configuration=Release /p:Platform="x86"
runAs-tool-win32\packages\NUnit.Console.3.0.1\tools\nunit3-console.exe C:\Projects\GitHub\JetBrains\teamcity-runas\runAs-tool-win32\bin\IntegrationTests\Release\JetBrains.runAs.IntegrationTests.dll
call msbuild runAs-tool-win32\tools.sln /p:Configuration=Release /p:Platform="x64"
runAs-tool-win32\packages\NUnit.Console.3.0.1\tools\nunit3-console.exe C:\Projects\GitHub\JetBrains\teamcity-runas\runAs-tool-win32\bin\IntegrationTests\Release\JetBrains.runAs.IntegrationTests.dll
REM call msbuild runAs-tool\runAs-tool.sln /p:Configuration=Release
call mvn package
call createPlugin.cmd