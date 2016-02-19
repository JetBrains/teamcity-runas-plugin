#pragma once
#include "ProcessTracker.h"
#include "ProcessAsUser.h"

class ProcessRunner
{
public:
	Result<ExitCode> Run(const Settings& settings) const;
};
