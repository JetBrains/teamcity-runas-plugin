Param(
[Parameter(Mandatory=$True)] [string] $teamCityAgentUserName,
[Parameter(Mandatory=$True)] [string] $tempDirectory)

$ErrorActionPreference = "Stop"

$builduser_name = $teamCityAgentUserName
$dist = $tempDirectory

$builduser_identity = $builduser_name
if (-not ($builduser_identity -like "*\*"))
{
  $builduser_identity = "$env:COMPUTERNAME\$builduser_name"
}

write-host "* Configuring TeamCity agent's user account '$builduser_name' for the runAs plugin" -foregroundcolor "green"
$cn = [ADSI]"WinNT://localhost"

write-host "  * Creating temp directory '$dist' ..." -foregroundcolor "green"
New-Item -Type Directory -Path $dist | Out-Null

[System.Reflection.Assembly]::LoadWithPartialName("System.IO.Compression.FileSystem") | Out-Null

write-host "  * Downloading and installing Carbon - PowerShell module for windows automation (see http://get-carbon.org/) ..." -foregroundcolor "green"
$carbonZipPath = [io.path]::combine($dist, "carbon.zip")
$carbonPath = [io.path]::combine($dist, "carbon-extracted")
$carbonModulePath = [io.path]::combine($carbonPath, "Carbon")
(New-Object System.Net.WebClient).DownloadFile("https://www.nuget.org/api/v2/package/Carbon/2.4.1", $carbonZipPath)
[System.IO.Compression.ZipFile]::ExtractToDirectory($carbonZipPath, $carbonPath)
Import-Module $carbonModulePath

write-host "  * Configuring the user account '$builduser_identity':" -foregroundcolor "green"
write-host "    * Adding to the Administrators group ..." -foregroundcolor "green"
Add-GroupMember -Name Administrators -Member $builduser_identity
write-host "    * Adding an ability to replace a process level token (SeAssignPrimaryTokenPrivilege) ..." -foregroundcolor "green"
Grant-Privilege -Identity $builduser_identity -Privilege SeAssignPrimaryTokenPrivilege
write-host "    * Adding an ability to act as a part of the operating system (SeTcbPrivilege) ..." -foregroundcolor "green"
Grant-Privilege -Identity $builduser_identity -Privilege SeTcbPrivilege

write-host "* All done." -foregroundcolor "green"
write-host "Important to note: The changes don't take effect until the next login, so you'll have to restart the TeamCity agent." -foregroundcolor "yellow"