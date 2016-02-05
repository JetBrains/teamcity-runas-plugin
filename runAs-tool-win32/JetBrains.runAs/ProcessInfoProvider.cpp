#include "stdafx.h"
#include "ProcessInfoProvider.h"

class Handle;

ProcessInfoProvider::ProcessInfoProvider()
{
}


ProcessInfoProvider::~ProcessInfoProvider()
{
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
