#pragma once
#include "IStreamWriter.h"
#include <sstream>

class StringWriter: public IStreamWriter
{
	std::stringstream& _stream;

public:
	explicit StringWriter(std::stringstream& stream);

	bool WriteFile(void* buffer, unsigned long size) override;	
};

