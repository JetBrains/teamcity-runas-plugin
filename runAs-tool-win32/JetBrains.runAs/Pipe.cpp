#include "stdafx.h"
#include "Pipe.h"
#include "ErrorUtilities.h"
#include "Result.h"

Pipe::Pipe(wstring name): 
	_readHandle(L"Pipe \"" + name + L"\" handle for read"),
	_writeHandle(L"Pipe \"" + name + L"\" handle for write")
{	
	_name = name;
}

Result<bool> Pipe::Initialize(SECURITY_ATTRIBUTES& securityAttributes)
{
	if (!CreatePipe(&_readHandle, &_writeHandle, &securityAttributes, 0))
	{
		return Result<bool>(ErrorUtilities::GetErrorCode(), ErrorUtilities::GetActionName(L"CreatePipe", _name));
	}	

	return true;
}

Handle& Pipe::GetReader()
{
	return _readHandle;
}

Handle& Pipe::GetWriter()
{
	return _writeHandle;
}