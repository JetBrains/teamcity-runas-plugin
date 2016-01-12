#include "stdafx.h"
#include "ProcessInfoProvider.h"
#include <tlhelp32.h>
#include "Handle.h"
#include "ErrorUtilities.h"
#include <iostream>
#include <algorithm>
#include <map>

class Handle;

ProcessInfoProvider::ProcessInfoProvider()
{
}


ProcessInfoProvider::~ProcessInfoProvider()
{
}

bool ProcessInfoProvider::IsServiceProcess() const
{
	auto processSnapshotHandle = Handle(L"Toolhelp Snapshot");
	processSnapshotHandle.Value() = CreateToolhelp32Snapshot(TH32CS_SNAPPROCESS, 0);
	if (processSnapshotHandle.IsInvalid())
	{
		std::wcerr << ErrorUtilities::GetLastErrorMessage(L"CreateToolhelp32Snapshot");
		return false;
	}

	PROCESSENTRY32 processEntry = {};
	processEntry.dwSize = sizeof(PROCESSENTRY32);
	if (!Process32First(processSnapshotHandle.Value(), &processEntry))
	{
		std::wcerr << ErrorUtilities::GetLastErrorMessage(L"Process32First");
		return false;
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
	while (Process32Next(processSnapshotHandle.Value(), &processEntry));

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
