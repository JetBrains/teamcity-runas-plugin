#pragma once
#include <codecvt>
#include "ErrorCode.h"

class ErrorUtilities
{
	ErrorUtilities();
	
public:	
	static std::wstring GetLastErrorMessage(std::wstring targetAction);	
	static std::wstring GetActionName(std::wstring targetAction, std::wstring arg);
	static ErrorCode GetErrorCode();
};
