#include "stdafx.h"
#include "ErrorUtilities.h"
#include <sstream>
#include <iomanip>
#include "ErrorCode.h"

ErrorUtilities::ErrorUtilities()
{
}

ErrorCode ErrorUtilities::GetErrorCode()
{	
	auto errorCode = GetLastError();
	if (errorCode >= ERROR_INVALID_ACCOUNT_NAME && errorCode <= ERROR_ACCOUNT_DISABLED)
	{
		return ERROR_CODE_ACCESS;
	}

	if (errorCode == RPC_X_BAD_STUB_DATA)
	{
		return ERROR_CODE_INVALID_USAGE;
	}

	return ERROR_WIN32;	
}

wstring ErrorUtilities::GetLastErrorMessage(wstring targetAction)
{
	auto errorCode = GetLastError();	
	if (errorCode != 0)
	{
		LPVOID messageBuffer;
		auto bufferLength = FormatMessage(
			FORMAT_MESSAGE_ALLOCATE_BUFFER | FORMAT_MESSAGE_FROM_SYSTEM | FORMAT_MESSAGE_IGNORE_INSERTS,
			nullptr,
			errorCode,
			MAKELANGID(LANG_NEUTRAL, SUBLANG_DEFAULT),
			reinterpret_cast<LPTSTR>(&messageBuffer),
			0,
			nullptr);

		if(bufferLength == 0)
		{
			return L"";
		}

		auto message = static_cast<LPCWSTR>(messageBuffer);
		wstring result(message);
		LocalFree(messageBuffer);
		wstringstream errorStream;
		errorStream << result  << targetAction << L" returns the error 0x" << hex << setw(8) << setfill(L'0') << errorCode << L".";
		return errorStream.str();
	}

	return L"";
}

wstring ErrorUtilities::GetActionName(wstring targetAction, wstring arg)
{
	return targetAction + L"(" + arg + L")";
}