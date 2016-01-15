#pragma once
#include "IStreamWriter.h"

class StreamWriter: public IStreamWriter
{
	HANDLE _hStream;

public:
	explicit StreamWriter(HANDLE hStream);	
	bool WriteFile(void* buffer, unsigned long size) override;	
};

