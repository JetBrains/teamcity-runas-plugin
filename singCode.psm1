function sign_File {
[CmdletBinding()]
    param (
        [Parameter(Position=0, Mandatory=$true, ValueFromPipelineByPropertyName=$true, ValueFromPipeline=$true)] [String] $FileName,
        [Parameter(Position=1, Mandatory=$true)] [String] $SignService,		
		[Parameter(Position=2, Mandatory=$true)] [String] $UserName,
		[Parameter(Position=3, Mandatory=$true)] [String] $UserPassword
    )
    process {
		$BakFileName = "$FileName.bak"
		if (Test-Path $BakFileName) { Remove-Item $BakFileName }
		Move-Item $FileName $BakFileName
		Invoke-RestMethod "$SignService/sign?name=runAs&flavor=default" -Method Post -InFile $BakFileName -ContentType "application/x-msi" -OutFile $FileName -Credential (New-Object System.Management.Automation.PSCredential("$UserName", (ConvertTo-SecureString "$UserPassword" -AsPlainText -Force)))
        if (Test-Path $BakFileName) { Remove-Item $BakFileName }
    }
}