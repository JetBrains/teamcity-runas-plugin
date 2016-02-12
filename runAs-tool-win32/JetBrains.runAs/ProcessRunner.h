#pragma once
#include "ProcessTracker.h"
#include "ProcessAsUser.h"

class ProcessRunner
{	
public:
	Result<ExitCode> Run(Settings settings) const;	
};
