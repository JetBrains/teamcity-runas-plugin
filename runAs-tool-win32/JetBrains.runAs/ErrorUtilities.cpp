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
	if (errorCode >= 0x0000052e && errorCode <= 0x00000532)
	{
		return ERROR_CODE_ACCESS;
	}

	return ERROR_WIN32;	
}

std::wstring ErrorUtilities::GetLastErrorMessage(std::wstring targetAction)
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
		std::wstring result(message);
		LocalFree(messageBuffer);
		std::wstringstream errorStream;
		errorStream << L"" << targetAction << L" throws 0x" << std::hex << std::setw(8) << std::setfill(L'0') << errorCode << L" - " << result;
		return errorStream.str();
	}

	return L"";
}

std::wstring ErrorUtilities::GetActionName(std::wstring targetAction, std::wstring arg)
{
	return targetAction + L"(" + arg + L")";
}