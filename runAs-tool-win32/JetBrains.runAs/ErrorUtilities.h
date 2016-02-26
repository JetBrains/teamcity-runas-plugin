#pragma once
#include <codecvt>
#include "ErrorCode.h"

class ErrorUtilities
{
	ErrorUtilities();
	
public:	
	static wstring GetLastErrorMessage(const wstring& targetAction);
	static wstring GetActionName(const wstring& targetAction, const wstring& arg);
	static ErrorCode GetErrorCode();
};
