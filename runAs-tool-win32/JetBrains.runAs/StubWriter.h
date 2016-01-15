#pragma once
#include "IStreamWriter.h"

class StubWriter : public IStreamWriter
{
public:
	bool WriteFile(void* buffer, unsigned long size) override;
};

