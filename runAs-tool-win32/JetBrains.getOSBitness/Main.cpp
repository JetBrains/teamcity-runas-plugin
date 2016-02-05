#include "stdafx.h"
#include <ostream>
#include <iostream>
#include "version.h"

typedef BOOL(WINAPI *LPFN_ISWOW64PROCESS) (HANDLE, PBOOL);
static bool IsWow64()
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

bool Is64OS()
{
	#if defined(_M_X64) || defined(x86_64)
		return true;
	#else
		return IsWow64() == true;
	#endif
}

int main()
{
	SetErrorMode(SEM_FAILCRITICALERRORS | SEM_NOGPFAULTERRORBOX | SEM_NOALIGNMENTFAULTEXCEPT | SEM_NOOPENFILEERRORBOX);

	std::wcout << VER_PRODUCTNAME_STR;
#if defined(_M_X64) || defined(x86_64)
	std::wcout << L" x64";
#else
	std::wcout << L" x86";
#endif
	std::wcout << L" " << VER_FILE_VERSION_STR;

	std::wcout << std::endl << VER_COPYRIGHT_STR;
	std::wcout << std::endl << VER_FILE_DESCRIPTION_STR;
	std::wcout << std::endl;

	std::wcout << std::endl << L"The current OS is ";
	if(Is64OS())
	{
		std::wcout << L"64-bit";
		return 64;
	}

	std::wcout << L"32-bit";
	return 32;
}