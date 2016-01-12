#include "stdafx.h"
#include "Pipe.h"
#include "ErrorUtilities.h"
#include <iostream>

Pipe::Pipe(std::wstring name): 
	_readHandle(L"Pipe \"" + name + L"\" handle for read"),
	_writeHandle(L"Pipe \"" + name + L"\" handle for write")
{	
	_name = name;
}

void Pipe::Initialize(SECURITY_ATTRIBUTES& securityAttributes)
{
	if (!CreatePipe(&_readHandle.Value(), &_writeHandle.Value(), &securityAttributes, 0))
	{
		std::wcerr << ErrorUtilities::GetLastErrorMessage(ErrorUtilities::GetActionName(L"CreatePipe", _name));
	}	
}

Handle& Pipe::GetReader()
{
	return _readHandle;
}

Handle& Pipe::GetWriter()
{
	return _writeHandle;
}