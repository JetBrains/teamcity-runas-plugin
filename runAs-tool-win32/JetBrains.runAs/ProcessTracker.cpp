#include "stdafx.h"
#include "ProcessTracker.h"
#include "ErrorUtilities.h"
#include <thread>
#include <chrono>
#include "Result.h"
#include "ExitCode.h"

ProcessTracker::ProcessTracker(IStreamWriter& outputWriter, IStreamWriter& errorWriter)
	: _outputWriter(outputWriter), _errorWriter(errorWriter)
{	
}

Result<bool> ProcessTracker::Initialize(SECURITY_ATTRIBUTES& securityAttributes, STARTUPINFO& startupInfo)
{
	auto error = _stdOutPipe.Initialize(securityAttributes);
	if (error.HasError() || !error.GetResultValue())
	{
		return error;
	}
	
	error = _stdErrorOutPipe.Initialize(securityAttributes);
	if (error.HasError() || !error.GetResultValue())
	{
		return error;
	}

	error = _stdInPipe.Initialize(securityAttributes);
	if (error.HasError() || !error.GetResultValue())
	{
		return error;
	}

	startupInfo.hStdOutput = _stdOutPipe.GetWriter();
	startupInfo.hStdError = _stdErrorOutPipe.GetWriter();
	startupInfo.hStdInput = _stdInPipe.GetReader();
	startupInfo.dwFlags |= STARTF_USESTDHANDLES;

	return Result<bool>(true);
}

Result<ExitCode> ProcessTracker::WaiteForExit(HANDLE processHandle)
{
	DWORD exitCode;
	bool hasData;
	do
	{
		std::this_thread::sleep_for(std::chrono::milliseconds(0));
		auto hasData1 = RedirectStream(_stdOutPipe.GetReader(), _outputWriter);
		if(hasData1.HasError())
		{
			return Result<ExitCode>(hasData1.GetErrorCode(), hasData1.GetErrorDescription());
		}

		auto hasData2 = RedirectStream(_stdErrorOutPipe.GetReader(), _errorWriter);
		if (hasData2.HasError())
		{
			return Result<ExitCode>(hasData2.GetErrorCode(), hasData2.GetErrorDescription());
		}

		hasData = hasData1.GetResultValue() | hasData2.GetResultValue();
		if (!GetExitCodeProcess(processHandle, &exitCode))
		{
			return Result<ExitCode>(ErrorUtilities::GetErrorCode(), ErrorUtilities::GetLastErrorMessage(L"GetExitCodeProcess"));
		}
	}
	while (exitCode == STILL_ACTIVE || hasData);

	return Result<ExitCode>(exitCode);
}

Result<bool> ProcessTracker::RedirectStream(HANDLE hPipeRead, IStreamWriter& writer)
{
	CHAR buffer[0xffff];
	DWORD bytesReaded;
	DWORD totalBytesAvail;
	DWORD bytesLeftThisMessage;

	if (!PeekNamedPipe(hPipeRead, buffer, sizeof(buffer), &bytesReaded, &totalBytesAvail, &bytesLeftThisMessage))
	{
		if (GetLastError() == ERROR_BROKEN_PIPE)
		{
			// Pipe done - normal exit path.
			return Result<bool>(true);
		}

		return Result<bool>(ErrorUtilities::GetErrorCode(), ErrorUtilities::GetLastErrorMessage(L"PeekNamedPipe"));
	}

	if (totalBytesAvail == 0)
	{
		return Result<bool>(false);
	}

	if (!ReadFile(hPipeRead, buffer, bytesReaded, &bytesReaded, nullptr) || !bytesReaded)
	{
		if (GetLastError() == ERROR_BROKEN_PIPE)
		{
			// Pipe done - normal exit path.
			return Result<bool>(false);
		}

		return Result<bool>(ErrorUtilities::GetErrorCode(), ErrorUtilities::GetLastErrorMessage(L"ReadFile"));
	}

	if (!writer.WriteFile(buffer, bytesReaded))
	{
		return Result<bool>(ErrorUtilities::GetErrorCode(), ErrorUtilities::GetLastErrorMessage(L"WriteConsole"));
	}

	return Result<bool>(true);
}

