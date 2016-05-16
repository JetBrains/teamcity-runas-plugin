#include "stdafx.h"
#include "Error.h"
#include "ErrorCode.h"
#include <iomanip>

Error::Error()
	:Error(L"", ERROR_CODE_UNKOWN, L"Unkonown error")
{
}

Error::Error(const wstring& targetAction)
	:Error(targetAction, GetErrorCodeInternal(), GetLastErrorMessageInternal(targetAction))
{	
}

Error::Error(const wstring& targetAction, const wstring& arg)
	:Error(targetAction + L"(" + arg + L")")
{
}

Error::Error(const wstring& targetAction, const ErrorCode& errorCode, const wstring& description):
	_targetAction(targetAction), _errorCode(errorCode), _description(description)
{		
}

wstring Error::GetTarget() const
{
	return _targetAction;
}

ErrorCode Error::GetCode() const
{
	return _errorCode;
}

wstring Error::GetDescription() const
{
	return _description;
}

ErrorCode Error::GetErrorCodeInternal()
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

wstring Error::GetLastErrorMessageInternal(const wstring& targetAction)
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

		if (bufferLength == 0)
		{
			return L"";
		}

		auto message = static_cast<LPCWSTR>(messageBuffer);
		wstring result(message);
		LocalFree(messageBuffer);
		return result;
	}

	return L"";
}