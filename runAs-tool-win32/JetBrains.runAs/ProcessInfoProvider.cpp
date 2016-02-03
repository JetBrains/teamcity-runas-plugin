#include "stdafx.h"
#include "ProcessInfoProvider.h"
#include <tlhelp32.h>
#include <Shlobj.h>
#include "Handle.h"
#include "ErrorUtilities.h"
#include <algorithm>
#include <map>

class Handle;

ProcessInfoProvider::ProcessInfoProvider()
{
}


ProcessInfoProvider::~ProcessInfoProvider()
{
}

Result<bool> ProcessInfoProvider::IsServiceProcess() const
{
	auto processSnapshotHandle = Handle(L"Toolhelp Snapshot");
	processSnapshotHandle = CreateToolhelp32Snapshot(TH32CS_SNAPPROCESS, 0);
	if (processSnapshotHandle.IsInvalid())
	{
		return Result<bool>(ErrorUtilities::GetErrorCode(), ErrorUtilities::GetLastErrorMessage(L"CreateToolhelp32Snapshot"));
	}

	PROCESSENTRY32 processEntry = {};
	processEntry.dwSize = sizeof(PROCESSENTRY32);
	if (!Process32First(processSnapshotHandle, &processEntry))
	{
		return Result<bool>(ErrorUtilities::GetErrorCode(), ErrorUtilities::GetLastErrorMessage(L"Process32First"));
	}

	struct ProcessInfo
	{
		DWORD ProcessId;
		DWORD ParentProcessId;
		std::wstring ExeFileName;
	};

	std::map<int, ProcessInfo> processMap {};
	do
	{
		ProcessInfo processInfo;
		processInfo.ProcessId = processEntry.th32ProcessID;
		processInfo.ParentProcessId = processEntry.th32ParentProcessID;
		processInfo.ExeFileName = std::wstring(processEntry.szExeFile);
		transform(processInfo.ExeFileName.begin(), processInfo.ExeFileName.end(), processInfo.ExeFileName.begin(), tolower);
		processMap[processInfo.ProcessId] = processInfo;			
	}
	while (Process32Next(processSnapshotHandle, &processEntry));

	auto processId = GetCurrentProcessId();
	while (processId != 0)
	{		
		auto processInfoIterrator = processMap.find(processId);
		if (processInfoIterrator != processMap.end())
		{
			auto nextProcessInfo = processInfoIterrator->second;
			if (L"services.exe" == nextProcessInfo.ExeFileName)
			{
				return true;
			}

			processId = nextProcessInfo.ParentProcessId;
		}
		else
		{
			processId = 0;
		}
	} 	

	return false;
}

bool ProcessInfoProvider::Is64OS() const
{
#if defined(_M_X64) || defined(x86_64)
	return true;
#else
	return IsWow64() == true;
#endif
}

typedef BOOL(WINAPI *LPFN_ISWOW64PROCESS) (HANDLE, PBOOL);
bool ProcessInfoProvider::IsWow64() const
{	
	int bIsWow64 = false;
	LPFN_ISWOW64PROCESS fnIsWow64Process;
	fnIsWow64Process = reinterpret_cast<LPFN_ISWOW64PROCESS>(GetProcAddress(GetModuleHandle(TEXT("kernel32")), "IsWow64Process"));
	if (NULL != fnIsWow64Process)
	{
		if (!fnIsWow64Process(GetCurrentProcess(), &bIsWow64))
		{
			return false;
		}
	}

	return bIsWow64 != 0;
}

Result<bool> ProcessInfoProvider::IsUserAnAdministrator(void) const
{	
#if _WIN32_WINNT >= 0x0600 
	bool isAdmin;
	auto currentSecurityTokenHandle = Handle(L"Current security token");
	if (!OpenProcessToken(GetCurrentProcess(), TOKEN_ADJUST_PRIVILEGES | TOKEN_QUERY, &currentSecurityTokenHandle))
	{
		return Result<bool>(ErrorUtilities::GetErrorCode(), ErrorUtilities::GetLastErrorMessage(L"OpenProcessToken"));
	}

	DWORD bytesUsed = 0;
	TOKEN_ELEVATION_TYPE tokenElevationType {};
	if (!GetTokenInformation(currentSecurityTokenHandle, TokenElevationType, &tokenElevationType, sizeof(tokenElevationType), &bytesUsed))
	{
		return Result<bool>(ErrorUtilities::GetErrorCode(), ErrorUtilities::GetLastErrorMessage(L"GetTokenInformation"));
	}

	if (tokenElevationType == TokenElevationTypeLimited)
	{
		Handle unfilteredToken(L"Unfiltered token");
		if (!GetTokenInformation(currentSecurityTokenHandle, TokenLinkedToken, unfilteredToken, sizeof(HANDLE), &bytesUsed))
		{
			return Result<bool>(ErrorUtilities::GetErrorCode(), ErrorUtilities::GetLastErrorMessage(L"GetTokenInformation"));
		}

		BYTE adminSID[SECURITY_MAX_SID_SIZE];
		DWORD sidSize = sizeof(adminSID);
		if (!CreateWellKnownSid(WinBuiltinAdministratorsSid, 0, &adminSID, &sidSize))
		{
			return Result<bool>(ErrorUtilities::GetErrorCode(), ErrorUtilities::GetLastErrorMessage(L"CreateWellKnownSid"));
		}

		auto isMember = FALSE;
		if (!CheckTokenMembership(unfilteredToken, &adminSID, &isMember))
		{
			return Result<bool>(ErrorUtilities::GetErrorCode(), ErrorUtilities::GetLastErrorMessage(L"CheckTokenMembership"));
		}

		isAdmin = isMember != FALSE;
	}
	else
	{
		isAdmin = IsUserAnAdmin() == TRUE;
	}

	return isAdmin;	
#else
	return IsUserAnAdmin() == TRUE;	
#endif
}

bool ProcessInfoProvider::IsSuitableOS() const
{
#if defined(_M_X64) || defined(x86_64)	
#else
	if (Is64OS())
	{
		return false;
	}
#endif
	
	return true;
}
