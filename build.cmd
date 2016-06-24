set Version=1.0.6
call tools\nuget.exe install JetBrains.runAs -Version %Version% -o win32
copy win32\JetBrains.runAs.%Version%\tools\x64\JetBrains.runAs.exe win32\x64 /Y
copy win32\JetBrains.runAs.%Version%\tools\x86\JetBrains.runAs.exe win32\x86 /Y
call mvn package
call createPlugin.cmd