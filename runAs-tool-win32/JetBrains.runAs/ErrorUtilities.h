#pragma once
#include <codecvt>
#include "ErrorCode.h"

class ErrorUtilities
{
	ErrorUtilities();
	
public:	
	static wstring GetLastErrorMessage(wstring targetAction);	
	static wstring GetActionName(wstring targetAction, wstring arg);
	static ErrorCode GetErrorCode();
};
