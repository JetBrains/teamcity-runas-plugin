#include "stdafx.h"
#include "ErrorUtilities.h"
#include <string>

ErrorUtilities::ErrorUtilities()
{
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

		if (bufferLength > 0)
		{
			auto message = static_cast<LPCWSTR>(messageBuffer);
			std::wstring result(message);
			LocalFree(messageBuffer);
			return L"\"" + targetAction + L"\" causes the error " + std::to_wstring(errorCode) + L": " + result;
		}
	}

	return L"";
}

std::wstring ErrorUtilities::GetActionName(std::wstring targetAction, std::wstring arg)
{
	return targetAction + L"(" + arg + L")";
}