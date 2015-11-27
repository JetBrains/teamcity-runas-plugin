#include "stdafx.h"
#include <ostream>
#include <iostream>

typedef BOOL(WINAPI *LPFN_ISWOW64PROCESS) (HANDLE, PBOOL);
static bool IsWow64()
{
	std::cout << "JetBrains getOSbitness tool";
#if defined(_M_X64) || defined(x86_64)
	std::cout << " x64";
#else
	std::cout << " x86";
#endif
	std::cout << std::endl << "Copyright(C) JetBrains. All rights reserved.";
	std::cout << std::endl << "Returns the exit code 32 for 32-bit OS and the exit code 64 for 64-bit OS.";
	std::cout << std::endl;	

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
	if(Is64OS())
	{
		std::cout << std::endl << "OS is 64-bit";
		return 64;
	}

	std::cout << std::endl << "OS is 32-bit";
	return 32;
}