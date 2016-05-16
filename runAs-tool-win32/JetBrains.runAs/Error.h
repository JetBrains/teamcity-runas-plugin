#pragma once
#include <string>
#include "ErrorCode.h"

class Error
{	
	const wstring _targetAction;
	const ErrorCode _errorCode;
	const wstring _description;

	static ErrorCode GetErrorCodeInternal();
	static wstring GetLastErrorMessageInternal(const wstring& targetAction);	

public:		
	Error();
	explicit Error(const wstring& targetAction);
	Error(const wstring& targetAction, const wstring& arg);
	Error(const wstring& targetAction, const ErrorCode& errorCode, const wstring& description);

	wstring GetTarget() const;
	ErrorCode GetCode() const;
	wstring GetDescription() const;	
};

