#pragma once
#include <string>
#include "Handle.h"

class Pipe
{
	Handle _readHandle;
	Handle _writeHandle;
	std::wstring _name;

public:
	explicit Pipe(std::wstring name);
	void Initialize(SECURITY_ATTRIBUTES& securityAttributes);
	Handle& GetReader();
	Handle& GetWriter();
};