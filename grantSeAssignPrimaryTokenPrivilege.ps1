Import-Module .\userRights.ps1

$userName = [Environment]::UserName
Grant-UserRight $userName SeAssignPrimaryTokenPrivilege